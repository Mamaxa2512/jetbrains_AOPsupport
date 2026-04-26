package org.example.aop.annotator

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AopAnnotationHighlightingContextTest {

    @Test
    fun `highlights supported aop annotation`() {
        assertTrue(
            AopAnnotationHighlightingContext.shouldHighlightAopAnnotation(
                referenceName = "Before",
                qualifiedName = "org.aspectj.lang.annotation.Before"
            )
        )
    }

    @Test
    fun `highlights Aspect annotation`() {
        assertTrue(
            AopAnnotationHighlightingContext.shouldHighlightAopAnnotation(
                referenceName = "Aspect",
                qualifiedName = "org.aspectj.lang.annotation.Aspect"
            )
        )
    }

    @Test
    fun `highlights Pointcut annotation`() {
        assertTrue(
            AopAnnotationHighlightingContext.shouldHighlightAopAnnotation(
                referenceName = "Pointcut",
                qualifiedName = "org.aspectj.lang.annotation.Pointcut"
            )
        )
    }

    @Test
    fun `highlights kotlin short annotation names`() {
        assertTrue(
            AopAnnotationHighlightingContext.shouldHighlightAopAnnotation(
                referenceName = "Before",
                qualifiedName = "Before"
            )
        )
    }

    @Test
    fun `does not highlight when package is not supported`() {
        assertFalse(
            AopAnnotationHighlightingContext.shouldHighlightAopAnnotation(
                referenceName = "Before",
                qualifiedName = "com.example.Before"
            )
        )
    }

    @Test
    fun `does not highlight when short name and qualified name mismatch`() {
        assertFalse(
            AopAnnotationHighlightingContext.shouldHighlightAopAnnotation(
                referenceName = "Around",
                qualifiedName = "org.aspectj.lang.annotation.Before"
            )
        )
    }

    @Test
    fun `does not highlight unknown annotations`() {
        assertFalse(
            AopAnnotationHighlightingContext.shouldHighlightAopAnnotation(
                referenceName = "Transactional",
                qualifiedName = "org.springframework.transaction.annotation.Transactional"
            )
        )
    }
}

