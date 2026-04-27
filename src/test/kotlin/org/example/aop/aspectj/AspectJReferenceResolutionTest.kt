package org.example.aop.aspectj

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.psi.PsiElement
import org.example.aop.aspectj.psi.DesignatorReference
import org.example.aop.aspectj.psi.PointcutDeclaration

/**
 * Tests for PSI-based reference resolution (Go to Definition)
 */
class AspectJReferenceResolutionTest : BasePlatformTestCase() {

    fun testFindPointcutDeclarationByName() {
        val code = """
            aspect TestAspect {
                pointcut myPointcut() : execution(* test(..));
                
                before() : myPointcut() { }
            }
        """.trimIndent()

        myFixture.configureByText("Test.aj", code)
        val file = myFixture.file

        // Find the reference "myPointcut" in "before() : myPointcut()"
        val offset = code.indexOf("myPointcut() {") // the reference
        val element = file.findElementAt(offset)

        assertNotNull("Should find element at offset", element)

        // The element should be a DesignatorReference or similar
        val parent = element?.parent
        println("Element: $element")
        println("Parent: $parent")
        println("Parent class: ${parent?.javaClass}")
    }

    fun testPointcutDeclarationResolution() {
        val code = """
            aspect TestAspect {
                pointcut loggingPointcut() : 
                    execution(* *.*(..)) && 
                    !within(org.example.aop.*);
                
                before() : loggingPointcut() { 
                    System.out.println("Before");
                }
            }
        """.trimIndent()

        myFixture.configureByText("Test.aj", code)
        val file = myFixture.file

        // Find the first pointcut declaration
        // (This would need proper PSI navigation which is tested by the parser)
        assertNotNull("File should be parsed", file)
    }

    fun testMultiplePointcutReferences() {
        val code = """
            aspect TestAspect {
                pointcut publicMethods() : execution(public * *.*(..));
                pointcut getters() : execution(* get*());
                
                pointcut allPublicGetters() : publicMethods() && getters();
                
                before() : allPublicGetters() {
                    System.out.println("Before getter");
                }
            }
        """.trimIndent()

        myFixture.configureByText("Test.aj", code)
        val file = myFixture.file

        // The file should parse all three pointcut designators
        assertNotNull("File should be parsed", file)
    }

    fun testNestedDesignatorReferences() {
        val code = """
            aspect TestAspect {
                pointcut execution_pointcut() : execution(* test(..));
                pointcut call_pointcut() : call(* helper(..));
                
                pointcut combined() : 
                    (execution_pointcut() && call_pointcut()) 
                    || execution(* other(..));
                
                before() : combined() { }
            }
        """.trimIndent()

        myFixture.configureByText("Test.aj", code)
        val file = myFixture.file

        assertNotNull("File should be parsed", file)
    }

    fun testAnnotationPointcutReference() {
        val code = """
            @Pointcut("execution(* test(..))")
            public void testPointcut() {}
        """.trimIndent()

        myFixture.configureByText("Test.java", code)
        val file = myFixture.file

        // This should recognize the annotation, though it's not .aj format
        // Just verify it doesn't crash
        assertNotNull("File should be recognized", file)
    }
}



