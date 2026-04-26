package org.example.aop.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtAnnotationEntry

class AopAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is PsiAnnotation -> annotateJavaAnnotation(element, holder)
            is KtAnnotationEntry -> annotateKotlinAnnotation(element, holder)
        }
    }

    private fun annotateJavaAnnotation(element: PsiAnnotation, holder: AnnotationHolder) {
        val nameRef = element.nameReferenceElement ?: return
        if (!AopAnnotationHighlightingContext.shouldHighlightAopAnnotation(nameRef.referenceName, element.qualifiedName)) return

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(nameRef)
            .textAttributes(AopTextAttributes.AOP_ANNOTATION)
            .create()
    }

    private fun annotateKotlinAnnotation(element: KtAnnotationEntry, holder: AnnotationHolder) {
        val shortName = element.shortName?.asString() ?: return
        val nameRef = element.typeReference ?: return
        val qualifiedName = nameRef.text
        if (!AopAnnotationHighlightingContext.shouldHighlightAopAnnotation(shortName, qualifiedName)) return

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(nameRef)
            .textAttributes(AopTextAttributes.AOP_ANNOTATION)
            .create()
    }
}
