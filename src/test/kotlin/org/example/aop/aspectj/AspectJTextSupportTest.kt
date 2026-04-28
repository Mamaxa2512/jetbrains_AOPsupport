package org.example.aop.aspectj

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AspectJTextSupportTest {

    @Test
    fun `collects pointcut expressions from advice and pointcut declarations`() {
        val text = """
            aspect TransactionAspect {
                private pointcut transactionalMethod() : execution(@AtomicPersistenceOperation * *(..));
                before() : transactionalMethod() {
                }
            }
        """.trimIndent()

        val occurrences = AspectJTextSupport.collectPointcutOccurrences(text)
        assertEquals(2, occurrences.size)
        assertTrue(occurrences.any { it.expression.contains("execution(@AtomicPersistenceOperation") })
        assertTrue(occurrences.any { it.expression == "transactionalMethod()" })
    }

    @Test
    fun `collects declared pointcut names`() {
        val text = """
            aspect TxAspect {
                pointcut txMethod() : execution(* *(..));
                private pointcut internalPointcut() : within(com.example..*);
            }
        """.trimIndent()

        val names = AspectJTextSupport.collectDeclaredPointcutNames(text)
        assertEquals(listOf("txMethod", "internalPointcut"), names)
    }

    @Test
    fun `detects pointcut completion context`() {
        val text = "before() : execution(* com.example..*(..))"
        val insideExpressionOffset = text.indexOf("execution") + 4
        val outsideOffset = text.indexOf("before")

        assertTrue(AspectJTextSupport.isPointcutContext(text, insideExpressionOffset))
        assertFalse(AspectJTextSupport.isPointcutContext(text, outsideOffset))
    }

    @Test
    fun `extracts completion prefix around caret`() {
        val text = "before() : exec"
        val offset = text.length
        assertEquals("exec", AspectJTextSupport.extractCompletionPrefix(text, offset))
    }

    @Test
    fun `detects declare completion context`() {
        val text = "declare war"
        assertTrue(AspectJTextSupport.isDeclareContext(text, text.length))
        assertFalse(AspectJTextSupport.isDeclareContext("before() : execution(* *(..))", 5))
    }

    @Test
    fun `detects aspect header context for per clauses`() {
        val text = "privileged aspect AuditAspect per"
        assertTrue(AspectJTextSupport.isAspectHeaderContext(text, text.length))
        assertFalse(AspectJTextSupport.isAspectHeaderContext("aspect AuditAspect {", "aspect AuditAspect {".length))
    }
}


