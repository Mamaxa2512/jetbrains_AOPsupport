package org.example.aop.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiElement

class AopAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is PsiAnnotation) return

        val nameRef = element.nameReferenceElement ?: return
        if (!AopAnnotationHighlightingContext.shouldHighlightAopAnnotation(nameRef.referenceName, element.qualifiedName)) return

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(nameRef)
            .textAttributes(AopTextAttributes.AOP_ANNOTATION)
            .create()
    }
}
