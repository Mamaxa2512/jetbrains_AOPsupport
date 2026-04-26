package org.example.aop.annotator

import org.example.aop.inspection.AopInspectionRules

internal object AopAnnotationHighlightingContext {
    fun shouldHighlightAopAnnotation(referenceName: String?, qualifiedName: String?): Boolean {
        if (referenceName !in AopInspectionRules.aopAnnotationShortNames) return false
        val safeQualifiedName = qualifiedName ?: return false
        val matchesQualified = safeQualifiedName in AopInspectionRules.aopAnnotationQualifiedNames
        val matchesShort = safeQualifiedName in AopInspectionRules.aopAnnotationShortNames
        if (!matchesQualified && !matchesShort) return false
        val normalizedShortName = if (matchesQualified) {
            safeQualifiedName.substringAfterLast('.')
        } else {
            safeQualifiedName
        }
        return referenceName == normalizedShortName
    }
}



