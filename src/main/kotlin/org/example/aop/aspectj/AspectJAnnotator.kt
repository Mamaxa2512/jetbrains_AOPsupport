package org.example.aop.aspectj

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.example.aop.inspection.AopInspectionRules
import org.example.aop.inspection.PointcutParser

class AspectJAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Pointcut expression validation is handled by the PSI-based AspectJInspection,
        // which correctly distinguishes user-defined pointcut references from unknown designators.
        // The text-based validation here would generate false positives for user-defined pointcuts
        // like serviceMethods() in: before() : serviceMethods() { }
    }
}
