package org.example.aop.aspectj

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.example.aop.aspectj.psi.TypeReferenceElement

class AspectJTypeReference(
    element: TypeReferenceElement
) : PsiReferenceBase<TypeReferenceElement>(element, true) {

    override fun resolve(): PsiElement? = AspectJReferenceSupport.resolveTypeReference(element)

    override fun getVariants(): Array<Any> = emptyArray()
}
