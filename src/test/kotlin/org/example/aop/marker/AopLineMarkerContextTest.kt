package org.example.aop.marker

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AopLineMarkerContextTest {

    @Test
    fun `detects advice annotations and ignores pointcut-only methods`() {
        val pointcutOnly = setOf("org.aspectj.lang.annotation.Pointcut")
        val advice = setOf("org.aspectj.lang.annotation.After")

        assertFalse(AopLineMarkerContext.hasAdviceAnnotation(pointcutOnly))
        assertTrue(AopLineMarkerContext.hasAdviceAnnotation(advice))
    }

    @Test
    fun `returns advice annotations in preferred stable order`() {
        val annotations = setOf(
            "org.aspectj.lang.annotation.AfterThrowing",
            "org.aspectj.lang.annotation.Before",
            "org.aspectj.lang.annotation.After",
            "org.aspectj.lang.annotation.Pointcut"
        )

        assertEquals(
            listOf(
                "org.aspectj.lang.annotation.Before",
                "org.aspectj.lang.annotation.After",
                "org.aspectj.lang.annotation.AfterThrowing"
            ),
            AopLineMarkerContext.adviceAnnotationsInPreferredOrder(annotations)
        )
    }

    @Test
    fun `builds deterministic class tooltip`() {
        assertEquals("AOP aspect - 3 advice method(s)", AopLineMarkerContext.classTooltip(3))
    }

    @Test
    fun `builds advice tooltip and handles anonymous class name`() {
        assertEquals(
            "@Before - aspect: AuditAspect",
            AopLineMarkerContext.adviceMethodTooltip("org.aspectj.lang.annotation.Before", "AuditAspect")
        )
        assertEquals(
            "@After - aspect: <anonymous>",
            AopLineMarkerContext.adviceMethodTooltip("org.aspectj.lang.annotation.After", null)
        )
    }
}

