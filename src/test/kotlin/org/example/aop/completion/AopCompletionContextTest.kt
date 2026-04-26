package org.example.aop.completion

import com.intellij.codeInsight.completion.CompletionUtilCore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AopCompletionContextTest {

    @Test
    fun `supports only aop advice annotations`() {
        assertTrue(AopCompletionContext.isSupportedAopAdviceAnnotation("org.aspectj.lang.annotation.Before"))
        assertFalse(AopCompletionContext.isSupportedAopAdviceAnnotation("org.aspectj.lang.annotation.Aspect"))
        assertFalse(AopCompletionContext.isSupportedAopAdviceAnnotation("com.example.Other"))
    }

    @Test
    fun `supports default and named pointcut attributes`() {
        assertTrue(AopCompletionContext.isSupportedPointcutAttribute("value"))
        assertTrue(AopCompletionContext.isSupportedPointcutAttribute("pointcut"))
        assertTrue(AopCompletionContext.isSupportedPointcutAttribute(null))
        assertFalse(AopCompletionContext.isSupportedPointcutAttribute("argNames"))
    }

    @Test
    fun `extracts prefix inside quoted literal`() {
        val tokenText = "\"execution${CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED}\""

        assertEquals("execution", AopCompletionContext.extractPrefix(tokenText))
    }

    @Test
    fun `extracts prefix when token has no quote`() {
        val tokenText = "within${CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED}"

        assertEquals("within", AopCompletionContext.extractPrefix(tokenText))
    }

    @Test
    fun `returns empty prefix when dummy marker is absent`() {
        assertEquals("", AopCompletionContext.extractPrefix("\"execution\""))
    }
}


