package org.example.aop.annotator

import org.example.aop.inspection.AopInspectionRules

internal object AopAnnotationHighlightingContext {
    fun shouldHighlightAopAnnotation(referenceName: String?, qualifiedName: String?): Boolean {
        if (referenceName !in AopInspectionRules.aopAnnotationShortNames) return false
        val safeQualifiedName = qualifiedName ?: return false
        if (safeQualifiedName !in AopInspectionRules.aopAnnotationQualifiedNames) return false
        return referenceName == safeQualifiedName.substringAfterLast('.')
    }
}



