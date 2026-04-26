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

    @Test
    fun `offers only negation operator for empty prefix`() {
        assertEquals(listOf("!"), AopCompletionContext.logicalOperatorCompletionItems(""))
        assertEquals(listOf("!"), AopCompletionContext.logicalOperatorCompletionItems("   "))
    }

    @Test
    fun `offers all logical operators for non-empty prefix`() {
        assertEquals(
            listOf("&&", "||", "!"),
            AopCompletionContext.logicalOperatorCompletionItems("execution")
        )
    }

    @Test
    fun `offers expression templates only for empty prefix`() {
        assertTrue(AopCompletionContext.expressionTemplateCompletionItems("").isNotEmpty())
        assertTrue(AopCompletionContext.expressionTemplateCompletionItems(" ").isNotEmpty())
        assertTrue(AopCompletionContext.expressionTemplateCompletionItems("exec").isEmpty())
    }

    @Test
    fun `ranks designators above templates for designator-like prefix`() {
        val designatorPriority = AopCompletionContext.designatorPriority("exec")
        val templatePriority = AopCompletionContext.templatePriority("exec")

        assertTrue(designatorPriority > templatePriority)
    }

    @Test
    fun `deprioritizes operators outside continuation context`() {
        val operatorPriority = AopCompletionContext.operatorPriority("exec")
        val designatorPriority = AopCompletionContext.designatorPriority("exec")

        assertTrue(operatorPriority < designatorPriority)
    }

    @Test
    fun `raises operator priority in continuation context`() {
        val operatorPriority = AopCompletionContext.operatorPriority("execution(* com.example..*(..))")
        val designatorPriority = AopCompletionContext.designatorPriority("execution(* com.example..*(..))")

        assertTrue(operatorPriority > designatorPriority)
    }
}




