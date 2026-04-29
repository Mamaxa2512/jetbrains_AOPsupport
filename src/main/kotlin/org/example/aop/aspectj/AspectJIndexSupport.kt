package org.example.aop.aspectj

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.VoidDataExternalizer
import org.example.aop.aspectj.psi.AspectDeclaration
import org.example.aop.aspectj.psi.DeclareErrorDeclaration
import org.example.aop.aspectj.psi.DeclareParentsDeclaration
import org.example.aop.aspectj.psi.DeclarePrecedenceDeclaration
import org.example.aop.aspectj.psi.DeclareSoftDeclaration
import org.example.aop.aspectj.psi.DeclareStatement
import org.example.aop.aspectj.psi.DeclareWarningDeclaration
import org.example.aop.aspectj.psi.InterTypeDeclaration
import org.example.aop.aspectj.psi.PointcutDeclaration
import org.example.aop.aspectj.psi.TypeReferenceElement

internal object AspectJIndexSupport {
    private val primitiveTypeNames = setOf(
        "byte", "short", "int", "long", "float", "double", "boolean", "char", "void"
    )

    fun pointcutNames(file: PsiFile): Set<String> =
        PsiTreeUtil.findChildrenOfType(file, PointcutDeclaration::class.java)
            .mapNotNull { it.getPointcutName() }
            .filter { it.isNotBlank() }
            .toSet()

    fun aspectNames(file: PsiFile): Set<String> =
        PsiTreeUtil.findChildrenOfType(file, AspectDeclaration::class.java)
            .mapNotNull { it.getAspectName() }
            .filter { it.isNotBlank() }
            .toSet()

    fun adviceTypes(file: PsiFile): Set<String> =
        PsiTreeUtil.findChildrenOfType(file, org.example.aop.aspectj.psi.AdviceDeclaration::class.java)
            .mapNotNull { it.getAdviceType() }
            .filter { it.isNotBlank() }
            .toSet()

    fun declareKeys(file: PsiFile): Set<String> =
        PsiTreeUtil.findChildrenOfType(file, DeclareStatement::class.java)
            .mapNotNull { declarationKind(it) }
            .toSet()

    fun interTypeTargets(file: PsiFile): Set<String> =
        PsiTreeUtil.findChildrenOfType(file, InterTypeDeclaration::class.java)
            .flatMap { declaration ->
                declaration.getTargetTypeReferences().mapNotNull { normalizeQualifiedTypeName(it.text) }
            }
            .filter { it.isNotBlank() && it !in primitiveTypeNames }
            .toSet()

    fun normalizeQualifiedTypeName(rawText: String?): String? {
        if (rawText.isNullOrBlank()) return null
        val compact = rawText.trim().removeSuffix("[]")
        if (compact.any { it == '*' || it == '+' || it == '@' }) return null
        val direct = compact.removeSuffix("...")
        if (direct in primitiveTypeNames) return null
        if (' ' in direct) {
            val trailingCandidate = direct.substringAfterLast(' ').trim()
            return normalizeQualifiedTypeName(trailingCandidate)
        }
        if (direct.count { it == '.' } >= 1) {
            val segments = direct.split('.').filter { it.isNotBlank() }
            if (segments.size >= 2 && segments.last().firstOrNull()?.isLowerCase() == true) {
                return segments.dropLast(1).joinToString(".")
            }
        }
        return direct
    }

    private fun declarationKind(statement: DeclareStatement): String? = when (statement) {
        is DeclareParentsDeclaration -> "parents"
        is DeclareWarningDeclaration -> "warning"
        is DeclareErrorDeclaration -> "error"
        is DeclareSoftDeclaration -> "soft"
        is DeclarePrecedenceDeclaration -> "precedence"
        else -> null
    }
}

abstract class AbstractAspectJIndex : FileBasedIndexExtension<String, Void>() {
    protected fun psiIndexer(extractor: (PsiFile) -> Set<String>): DataIndexer<String, Void, FileContent> =
        DataIndexer { inputData ->
            val psiFile = inputData.psiFile
            val keys = extractor(psiFile)
            keys.associateWith { null }
        }

    override fun getKeyDescriptor() = EnumeratorStringDescriptor.INSTANCE

    override fun getValueExternalizer() = VoidDataExternalizer.INSTANCE

    override fun getInputFilter() = com.intellij.util.indexing.FileBasedIndex.InputFilter { file ->
        file.extension == "aj"
    }

    override fun dependsOnFileContent(): Boolean = true

    override fun getCacheSize() = 1024
}
