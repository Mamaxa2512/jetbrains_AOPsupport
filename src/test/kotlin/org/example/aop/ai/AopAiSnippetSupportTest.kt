package org.example.aop.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AopAiSnippetSupportTest {

    @Test
    fun `normalizes native advice declaration to pointcut reference`() {
        assertEquals(
            "transactionalMethod()",
            AopAiSnippetSupport.normalizeSnippet("before() : transactionalMethod()")
        )
        assertEquals(
            "transactionalMethod()",
            AopAiSnippetSupport.normalizeSnippet("after() returning : transactionalMethod()")
        )
        assertEquals(
            "transactionalMethod()",
            AopAiSnippetSupport.normalizeSnippet("after() throwing (Throwable ex) : transactionalMethod() {")
        )
    }

    @Test
    fun `normalizes quoted pointcut snippets`() {
        assertEquals(
            "execution(* com.example..*(..))",
            AopAiSnippetSupport.normalizeSnippet("\"execution(* com.example..*(..))\"")
        )
    }

    @Test
    fun `recognizes pointcut and aspectj snippets`() {
        assertTrue(AopAiSnippetSupport.looksLikeAopSnippet("execution(@AtomicPersistenceOperation * *(..))"))
        assertTrue(AopAiSnippetSupport.looksLikeAopSnippet("transactionalMethod()"))
        assertTrue(AopAiSnippetSupport.looksLikeAopSnippet("before() : transactionalMethod()"))
        assertTrue(AopAiSnippetSupport.looksLikeAopSnippet("pointcut transactionalMethod() : execution(* *(..))"))
        assertFalse(AopAiSnippetSupport.looksLikeAopSnippet("println(\"hello\")"))
    }

    @Test
    fun `uses raw insertion for aj files and quoted insertion for java files`() {
        assertTrue(AopAiSnippetSupport.shouldInsertRawSnippet("TransactionAspect.aj"))
        assertFalse(AopAiSnippetSupport.shouldInsertRawSnippet("TransactionAspect.java"))

        assertEquals(
            "execution(* com.example..*(..))",
            AopAiSnippetSupport.formatGeneratedSnippet("execution(* com.example..*(..))", "TransactionAspect.aj")
        )
        assertEquals(
            "\"execution(* com.example..*(..))\"",
            AopAiSnippetSupport.formatGeneratedSnippet("execution(* com.example..*(..))", "TransactionAspect.java")
        )
    }
}

