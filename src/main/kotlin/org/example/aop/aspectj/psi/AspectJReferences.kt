package org.example.aop.aspectj.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil
import org.example.aop.aspectj.AspectJReferenceSupport

/**
 * Reference for pointcut designator references (e.g., in "myPointcut()" within a pointcut expression)
 *
 * Example: @pointcut("execution(...) && myPointcut()")
 *                                     ^^^^^^^^^^
 *                                     This is a designator reference
 *
 * Enables:
 * - Go to Definition (Ctrl+Click)
 * - Find Usages
 * - Rename refactoring
 */
class AspectJPointcutReference(
    element: PsiElement,
    val referenceName: String
) : PsiReferenceBase<PsiElement>(element, true) {

    override fun resolve(): PsiElement? {
        val file = element.containingFile
        AspectJReferenceSupport.findPointcutDeclaration(file, referenceName)
            ?.nameIdentifier
            ?.let { return it }

        AspectJReferenceSupport.findPointcutDeclarationInProject(file.project, referenceName)
            ?.nameIdentifier
            ?.let { return it }

        return file.let {
            PsiTreeUtil.findChildrenOfType(it, PointcutDeclaration::class.java)
                .firstOrNull { decl -> decl.getPointcutName() == referenceName }
                ?.nameIdentifier
        }
    }

    override fun getVariants(): Array<Any> {
        val local = element.containingFile.let {
            PsiTreeUtil.findChildrenOfType(it, PointcutDeclaration::class.java)
                .mapNotNull { decl -> decl.getPointcutName() }
                .distinct()
        }
        val projectNames = AspectJReferenceSupport.collectPointcutNamesInProject(element.containingFile.project)
        return (local + projectNames).distinct().toTypedArray()
    }

    override fun getCanonicalText(): String = referenceName
}


