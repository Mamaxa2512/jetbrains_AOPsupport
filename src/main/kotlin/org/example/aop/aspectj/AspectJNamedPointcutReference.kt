@file:Suppress("unused")

package org.example.aop.aspectj

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

internal class AspectJNamedPointcutReference(
    element: PsiElement
) : PsiReferenceBase<PsiElement>(element, true) {

    override fun resolve(): PsiElement? {
        val file = element.containingFile
        val declarationOffset = AspectJReferenceSupport.findPointcutDeclarationOffset(file.text, element.text) ?: return null
        return file.findElementAt(declarationOffset)
    }

    override fun getVariants(): Array<Any> = emptyArray()
}


