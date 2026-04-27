package org.example.aop.aspectj

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.intellij.psi.PsiReference

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
            if (AspectJReferenceSupport.isAnnotationReference(text, fileText, startOffset)) {
                add(AspectJAnnotationReference(element))
            }
            if (AspectJReferenceSupport.isNamedPointcutReference(text, fileText, startOffset)) {
                add(AspectJNamedPointcutReference(element))
            }
        }.toTypedArray()
    }
}

