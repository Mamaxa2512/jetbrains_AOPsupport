@file:Suppress("unused")

package org.example.aop.aspectj

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.PsiManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.FileTypeIndex
import com.intellij.util.indexing.FileBasedIndex
import org.example.aop.aspectj.AspectJFileType
import org.example.aop.aspectj.psi.AspectDeclaration
import org.example.aop.aspectj.psi.PointcutDeclaration
import org.example.aop.aspectj.psi.TypeReferenceElement

internal object AspectJReferenceSupport {

    private val pointcutDeclarationRegex = Regex("""(?m)^\s*pointcut\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(""")
    private val importRegex = Regex("""(?m)^\s*import\s+([A-Za-z_][A-Za-z0-9_.]*)\s*;\s*$""")

    fun isAnnotationReference(text: String, fileText: String, startOffset: Int): Boolean {
        return text.startsWith('@') && text.length > 1 && previousNonWhitespaceChar(fileText, startOffset) == '('
    }

    fun isNamedPointcutReference(text: String, fileText: String, startOffset: Int): Boolean {
        if (text.isBlank()) return false
        val previous = previousNonWhitespaceChar(fileText, startOffset)
        return previous == ':' && fileText.indexOf(text, startOffset) == startOffset
    }

    /**
     * Find pointcut declaration by name using PSI tree (new approach)
     */
    fun findPointcutDeclaration(file: PsiFile, pointcutName: String): PointcutDeclaration? {
        return PsiTreeUtil.findChildrenOfType(file, PointcutDeclaration::class.java)
            .firstOrNull { it.getPointcutName() == pointcutName }
    }

    /**
     * Search for a pointcut declaration across the project by scanning all .aj files.
     * This is a fallback solution (no index). It is sufficient for Phase 5 prototype.
     */
    fun findPointcutDeclarationInProject(project: Project, pointcutName: String): PointcutDeclaration? {
        val scope = GlobalSearchScope.projectScope(project)
        val broadScope = GlobalSearchScope.everythingScope(project)
        val psiManager = PsiManager.getInstance(project)

        fun scanFiles(files: Iterable<com.intellij.openapi.vfs.VirtualFile>): PointcutDeclaration? {
            for (vf in files) {
                val psi = psiManager.findFile(vf) ?: continue
                val decl = findPointcutDeclaration(psi, pointcutName)
                if (decl != null) return decl
            }
            return null
        }

        // Fast path: use the project index of named pointcuts.
        try {
            val indexedFiles = FileBasedIndex.getInstance().getContainingFiles(AspectJPointcutIndex.NAME, pointcutName, scope)
            scanFiles(indexedFiles)?.let { found -> return found }
        } catch (_: Throwable) {
            // ignore and continue with broader scans
        }

        // Fallback 1: all .aj files by extension.
        try {
            val filesByExt = FilenameIndex.getAllFilesByExt(project, "aj", broadScope)
            scanFiles(filesByExt)?.let { found -> return found }
        } catch (_: Throwable) {
            // ignore and continue
        }

        // Fallback 2: all files associated with AspectJFileType.
        try {
            val filesByType = FileTypeIndex.getFiles(AspectJFileType, broadScope)
            scanFiles(filesByType)?.let { found -> return found }
        } catch (_: Throwable) {
            // ignore and continue
        }

        // Last resort: walk the project VFS and inspect any .aj files we find.
        val basePath = project.basePath ?: return null
        val base = LocalFileSystem.getInstance().findFileByPath(basePath) ?: return null
        fun visit(vf: com.intellij.openapi.vfs.VirtualFile): PointcutDeclaration? {
            if (vf.isDirectory) {
                for (child in vf.children) {
                    val found = visit(child)
                    if (found != null) return found
                }
            } else if (vf.extension == "aj") {
                val psi = psiManager.findFile(vf) ?: return null
                val decl = findPointcutDeclaration(psi, pointcutName)
                if (decl != null) return decl
            }
            return null
        }
        return visit(base)
    }

    fun collectPointcutNamesInProject(project: Project): Set<String> {
        return collectIndexedKeys(project, AspectJPointcutIndex.NAME)
    }

    fun collectAspectNamesInProject(project: Project): Set<String> =
        collectIndexedKeys(project, AspectJAspectIndex.NAME)

    fun collectDeclareKindsInProject(project: Project): Set<String> =
        collectIndexedKeys(project, AspectJDeclareIndex.NAME)

    fun collectInterTypeTargetsInProject(project: Project): Set<String> =
        collectIndexedKeys(project, AspectJInterTypeIndex.NAME)

    fun findAspectDeclarationInProject(project: Project, aspectName: String): AspectDeclaration? {
        val scope = GlobalSearchScope.projectScope(project)
        val psiManager = PsiManager.getInstance(project)
        return FileBasedIndex.getInstance()
            .getContainingFiles(AspectJAspectIndex.NAME, aspectName, scope)
            .asSequence()
            .mapNotNull { psiManager.findFile(it) }
            .flatMap { file -> PsiTreeUtil.findChildrenOfType(file, AspectDeclaration::class.java).asSequence() }
            .firstOrNull { it.getAspectName() == aspectName }
    }

    fun resolveTypeReference(typeReference: TypeReferenceElement): PsiElement? {
        val normalized = AspectJIndexSupport.normalizeQualifiedTypeName(typeReference.text) ?: return null
        val project = typeReference.project
        val scope = GlobalSearchScope.allScope(project)
        val facade = com.intellij.psi.JavaPsiFacade.getInstance(project)
        if ('.' in normalized) {
            facade.findClasses(normalized, scope).firstOrNull()?.let { return it }
        }
        return com.intellij.psi.search.PsiShortNamesCache.getInstance(project)
            .getClassesByName(normalized.substringAfterLast('.'), scope)
            .firstOrNull()
    }

    /**
     * Find pointcut declaration offset using regex (legacy approach, kept for compatibility)
     */
    fun findPointcutDeclarationOffset(fileText: String, pointcutName: String): Int? {
        return pointcutDeclarationRegex.findAll(fileText).firstOrNull { match ->
            match.groupValues[1] == pointcutName
        }?.groups?.get(1)?.range?.first
    }

    fun findImportedQualifiedName(fileText: String, shortName: String): String? {
        return importRegex.findAll(fileText)
            .map { it.groupValues[1] }
            .firstOrNull { it.substringAfterLast('.') == shortName }
    }

    private fun previousNonWhitespaceChar(text: String, offset: Int): Char? {
        var i = offset - 1
        while (i >= 0 && text[i].isWhitespace()) i--
        return if (i >= 0) text[i] else null
    }

    private fun collectIndexedKeys(project: Project, indexName: com.intellij.util.indexing.ID<String, Void>): Set<String> {
        val names = linkedSetOf<String>()
        try {
            FileBasedIndex.getInstance().processAllKeys(
                indexName,
                { key: String ->
                    names += key
                    true
                },
                GlobalSearchScope.projectScope(project),
                null
            )
        } catch (_: Throwable) {
            // ignore and return what we have
        }
        return names
    }
}
