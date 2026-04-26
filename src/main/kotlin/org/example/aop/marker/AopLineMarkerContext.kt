package org.example.aop.marker

import org.example.aop.inspection.AopInspectionRules

internal object AopLineMarkerContext {
    fun hasAdviceAnnotation(annotationQualifiedNames: Set<String>): Boolean {
        return adviceAnnotationsInPreferredOrder(annotationQualifiedNames).isNotEmpty()
    }

    fun adviceAnnotationsInPreferredOrder(annotationQualifiedNames: Set<String>): List<String> {
        return AopInspectionRules.adviceAnnotations.filter { it in annotationQualifiedNames }
    }

    fun classTooltip(adviceMethodCount: Int): String {
        return "AOP aspect - $adviceMethodCount advice method(s)"
    }

    fun adviceMethodTooltip(annotationQualifiedName: String, aspectName: String?): String {
        val shortName = annotationQualifiedName.substringAfterLast('.')
        val displayName = aspectName ?: "<anonymous>"
        return "@$shortName - aspect: $displayName"
    }
}

