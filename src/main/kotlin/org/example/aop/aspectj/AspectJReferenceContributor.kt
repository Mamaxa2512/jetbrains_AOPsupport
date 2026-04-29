package org.example.aop.aspectj

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.intellij.psi.PsiReference
import org.example.aop.aspectj.psi.AspectJPointcutReference
import org.example.aop.aspectj.psi.DesignatorReference
import org.example.aop.aspectj.psi.TypeReferenceElement

class AspectJReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement().withLanguage(AspectJLanguage),
            AspectJReferenceProvider()
        )
    }
}

private class AspectJReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val text = element.text
        if (text.isBlank()) return emptyArray()

        val fileText = element.containingFile.text
        val startOffset = element.textRange.startOffset

        return buildList {
            // PSI-based references (priority)
            when (element) {
                is DesignatorReference -> {
                    // Pointcut reference in designator chain
                    element.referenceName?.let { name ->
                        add(AspectJPointcutReference(element, name))
                    }
                }
                is TypeReferenceElement -> {
                    add(AspectJTypeReference(element))
                    if ('.' in element.text) {
                        add(AspectJMemberReference(element))
                    }
                }
            }

            // Fallback to regex-based references for compatibility
            if (AspectJReferenceSupport.isAnnotationReference(text, fileText, startOffset)) {
                add(AspectJAnnotationReference(element))
            }
            if (AspectJReferenceSupport.isNamedPointcutReference(text, fileText, startOffset)) {
                // Use legacy reference for now
                add(AspectJNamedPointcutReference(element))
            }
        }.toTypedArray()
    }
}
