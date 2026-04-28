@file:Suppress("unused")

package org.example.aop.aspectj

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.PsiManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.FileTypeIndex
import com.intellij.util.indexing.FileBasedIndex
import org.example.aop.aspectj.AspectJFileType
import org.example.aop.aspectj.psi.PointcutDeclaration

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
            println("[AspectJRef] indexedFiles=${indexedFiles.count()} for $pointcutName")
            scanFiles(indexedFiles)?.let { found ->
                println("[AspectJRef] found via index in ${found.containingFile.name}")
                return found
            }
        } catch (_: Throwable) {
            // ignore and continue with broader scans
        }

        // Fallback 1: all .aj files by extension.
        try {
            val filesByExt = FilenameIndex.getAllFilesByExt(project, "aj", broadScope)
            println("[AspectJRef] filesByExt=${filesByExt.count()} for $pointcutName")
            scanFiles(filesByExt)?.let { found ->
                println("[AspectJRef] found via ext in ${found.containingFile.name}")
                return found
            }
        } catch (_: Throwable) {
            // ignore and continue
        }

        // Fallback 2: all files associated with AspectJFileType.
        try {
            val filesByType = FileTypeIndex.getFiles(AspectJFileType, broadScope)
            println("[AspectJRef] filesByType=${filesByType.count()} for $pointcutName")
            scanFiles(filesByType)?.let { found ->
                println("[AspectJRef] found via filetype in ${found.containingFile.name}")
                return found
            }
        } catch (_: Throwable) {
            // ignore and continue
        }

        // Last resort: walk the project VFS and inspect any .aj files we find.
        val base = project.baseDir ?: return null
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
        val names = linkedSetOf<String>()
        try {
            FileBasedIndex.getInstance().processAllKeys(
                AspectJPointcutIndex.NAME,
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
}




