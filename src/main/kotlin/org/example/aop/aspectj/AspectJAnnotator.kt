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
        val file = element as? PsiFile ?: return
        if (file.language != AspectJLanguage) return
        AspectJTextSupport.collectPointcutOccurrences(file.text).forEach { occurrence ->
            val basicError = AopInspectionRules.validatePointcutExpressionDetailed(occurrence.expression)
            if (basicError != null) {
                holder.newAnnotation(HighlightSeverity.WARNING, basicError.message)
                    .range(occurrence.range)
                    .create()
                return@forEach
            }
            val parseResult = PointcutParser.parse(occurrence.expression)
            parseResult.errors.forEach { error ->
                holder.newAnnotation(HighlightSeverity.ERROR, error.message)
                    .range(occurrence.range)
                    .create()
            }
            parseResult.warnings.forEach { warning ->
                holder.newAnnotation(HighlightSeverity.WEAK_WARNING, warning.message)
                    .range(occurrence.range)
                    .create()
            }
        }
    }
}
