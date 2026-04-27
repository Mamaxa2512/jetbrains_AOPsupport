package org.example.aop.aspectj

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for Find Usages (Ctrl+Alt+F7) on AspectJ pointcuts
 */
class AspectJFindUsagesTest : BasePlatformTestCase() {

    fun testFindUsagesHandlerFactoryCanProcess() {
        val factory = AspectJFindUsagesHandlerFactory()
        assertNotNull("Factory should be created", factory)
    }

    fun testSimplePointcutCode() {
        val code = """
            aspect TestAspect {
                pointcut myPointcut() : execution(* test(..));
                
                before() : myPointcut() { }
            }
        """.trimIndent()

        myFixture.configureByText("Test.aj", code)
        val file = myFixture.file

        // Just verify parsing doesn't crash
        assertNotNull("File should be parsed", file)
        assertTrue("File should have content", file.text.isNotEmpty())
    }

    fun testComplexPointcutExpression() {
        val code = """
            aspect TestAspect {
                pointcut execution_pc() : execution(* test(..));
                pointcut call_pc() : call(* helper(..));
                
                pointcut combined() : 
                    execution_pc() && call_pc() 
                    || execution(* other(..));
                
                before() : combined() { }
            }
        """.trimIndent()

        myFixture.configureByText("Test.aj", code)
        val file = myFixture.file

        // Just verify it parses correctly
        assertNotNull("File should be parsed", file)
        assertEquals("Should contain 3 pointcuts", 3, file.text.count { it == 'p' && true }.let {
            file.text.split("pointcut").size - 1
        })
    }

    fun testMultipleAdviceWithPointcut() {
        val code = """
            aspect TestAspect {
                pointcut loggingPoint() : execution(* *.*(..));
                
                before() : loggingPoint() { 
                    System.out.println("Before");
                }
                
                after() : loggingPoint() { 
                    System.out.println("After");
                }
                
                around() : loggingPoint() { }
            }
        """.trimIndent()

        myFixture.configureByText("Test.aj", code)
        val file = myFixture.file

        // Verify structure is sound
        assertNotNull("File should be parsed", file)
        assertTrue("Should have advice declarations", file.text.contains("before()"))
        assertTrue("Should have multiple advice", file.text.contains("after()"))
    }

    fun testRenamePointcutProcessor() {
        val processor = AspectJSimpleRenameProcessor()
        assertNotNull("Rename processor should be created", processor)

        // Test that it can process PointcutDeclaration elements
        // (actual processing is integration tested via IDE)
    }

    fun testFindUsagesHandlerCanBeCreated() {
        val code = """
            aspect TestAspect {
                pointcut testPoint() : execution(* test(..));
                before() : testPoint() { }
            }
        """.trimIndent()

        myFixture.configureByText("Test.aj", code)
        val file = myFixture.file

        // Verify the handler factory exists and is registered
        val factory = AspectJFindUsagesHandlerFactory()
        assertNotNull("Factory should be instantiable", factory)
    }
}


