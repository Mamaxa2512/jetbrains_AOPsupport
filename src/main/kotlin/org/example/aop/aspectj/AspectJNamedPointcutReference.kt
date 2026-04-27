@file:Suppress("unused")

package org.example.aop.aspectj

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

internal class AspectJNamedPointcutReference(
    element: PsiElement
) : PsiReferenceBase<PsiElement>(element, true) {

    override fun resolve(): PsiElement? {
        val file = element.containingFile
        val pointcutName = element.text

        val psiDecl = AspectJReferenceSupport.findPointcutDeclaration(file, pointcutName)
        if (psiDecl != null) {
            return psiDecl.nameIdentifier
        }

        val projectDecl = AspectJReferenceSupport.findPointcutDeclarationInProject(file.project, pointcutName)
        if (projectDecl != null) return projectDecl.nameIdentifier

        // Fallback to regex-based approach for compatibility
        val declarationOffset = AspectJReferenceSupport.findPointcutDeclarationOffset(file.text, pointcutName) ?: return null
        return file.findElementAt(declarationOffset)
    }

    override fun getVariants(): Array<Any> = emptyArray()
}


