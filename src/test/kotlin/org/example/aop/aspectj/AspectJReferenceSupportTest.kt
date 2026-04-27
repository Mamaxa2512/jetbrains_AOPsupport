package org.example.aop.aspectj

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AspectJReferenceSupportTest {

    @Test
    fun `detects annotation reference in pointcut`() {
        val fileText = "execution(@AtomicPersistenceOperation * *(..))"
        assertTrue(AspectJReferenceSupport.isAnnotationReference("@AtomicPersistenceOperation", fileText, 10))
        assertFalse(AspectJReferenceSupport.isAnnotationReference("execution", fileText, 0))
    }

    @Test
    fun `detects named pointcut reference after colon`() {
        val fileText = "before() : transactionalMethod() {"
        assertTrue(AspectJReferenceSupport.isNamedPointcutReference("transactionalMethod", fileText, 11))
        assertFalse(AspectJReferenceSupport.isNamedPointcutReference("before", fileText, 0))
    }

    @Test
    fun `finds pointcut declaration offset`() {
        val fileText = "pointcut transactionalMethod() : execution(* *(..));"
        assertEquals(9, AspectJReferenceSupport.findPointcutDeclarationOffset(fileText, "transactionalMethod"))
    }

    @Test
    fun `finds imported qualified name by short annotation`() {
        val fileText = """
            import sk.tuke.meta.persistence.annotations.AtomicPersistenceOperation;
            aspect TxAspect {}
        """.trimIndent()

        assertEquals(
            "sk.tuke.meta.persistence.annotations.AtomicPersistenceOperation",
            AspectJReferenceSupport.findImportedQualifiedName(fileText, "AtomicPersistenceOperation")
        )
        assertNull(AspectJReferenceSupport.findImportedQualifiedName(fileText, "UnknownAnno"))
    }
}



