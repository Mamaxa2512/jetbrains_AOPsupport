package org.example.aop.inspection

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Fixture-based integration tests for AOP inspections.
 *
 * Stub annotations are declared inline so the tests have no external classpath
 * dependency on AspectJ or Spring jars. The inspections match by qualified name,
 * so the stubs must live in the correct packages.
 */
class AopInspectionFixtureTest : BasePlatformTestCase() {

    // ── helpers ───────────────────────────────────────────────────────────────

    /** Adds minimal stub annotations so the Java source under test can compile. */
    private fun addAopStubs() {
        myFixture.addFileToProject(
            "org/aspectj/lang/annotation/Aspect.java",
            "package org.aspectj.lang.annotation; public @interface Aspect {}"
        )
        myFixture.addFileToProject(
            "org/aspectj/lang/annotation/Before.java",
            "package org.aspectj.lang.annotation; public @interface Before { String value() default \"\"; }"
        )
        myFixture.addFileToProject(
            "org/aspectj/lang/annotation/After.java",
            "package org.aspectj.lang.annotation; public @interface After { String value() default \"\"; }"
        )
        myFixture.addFileToProject(
            "org/aspectj/lang/annotation/Around.java",
            "package org.aspectj.lang.annotation; public @interface Around { String value() default \"\"; }"
        )
        myFixture.addFileToProject(
            "org/aspectj/lang/annotation/Pointcut.java",
            "package org.aspectj.lang.annotation; public @interface Pointcut { String value() default \"\"; }"
        )
        myFixture.addFileToProject(
            "org/springframework/stereotype/Component.java",
            "package org.springframework.stereotype; public @interface Component {}"
        )
        myFixture.addFileToProject(
            "org/springframework/stereotype/Service.java",
            "package org.springframework.stereotype; public @interface Service {}"
        )
    }

    /** Adds minimal Kotlin annotation stubs for Kotlin source tests. */
    private fun addKotlinAopStubs() {
        myFixture.addFileToProject(
            "org/aspectj/lang/annotation/Aspect.kt",
            "package org.aspectj.lang.annotation\nannotation class Aspect"
        )
        myFixture.addFileToProject(
            "org/aspectj/lang/annotation/Before.kt",
            "package org.aspectj.lang.annotation\nannotation class Before(val value: String = \"\")"
        )
        myFixture.addFileToProject(
            "org/aspectj/lang/annotation/After.kt",
            "package org.aspectj.lang.annotation\nannotation class After(val value: String = \"\")"
        )
        myFixture.addFileToProject(
            "org/aspectj/lang/annotation/Around.kt",
            "package org.aspectj.lang.annotation\nannotation class Around(val value: String = \"\")"
        )
        myFixture.addFileToProject(
            "org/aspectj/lang/annotation/Pointcut.kt",
            "package org.aspectj.lang.annotation\nannotation class Pointcut(val value: String = \"\")"
        )
        myFixture.addFileToProject(
            "org/springframework/stereotype/Component.kt",
            "package org.springframework.stereotype\nannotation class Component"
        )
        myFixture.addFileToProject(
            "org/springframework/stereotype/Service.kt",
            "package org.springframework.stereotype\nannotation class Service"
        )
    }

    // ── PointcutSyntaxInspection ──────────────────────────────────────────────

    fun `test valid pointcut expression produces no warning`() {
        addAopStubs()
        myFixture.enableInspections(PointcutSyntaxInspection())
        myFixture.configureByText(
            "ValidAspect.java",
            """
            import org.aspectj.lang.annotation.Aspect;
            import org.aspectj.lang.annotation.Before;
            import org.springframework.stereotype.Component;
            @Aspect @Component
            class ValidAspect {
                @Before("execution(* com.example..*(..))")
                public void beforeAdvice() {}
            }
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, false)
    }

    fun `test empty pointcut expression is flagged`() {
        addAopStubs()
        myFixture.enableInspections(PointcutSyntaxInspection())
        myFixture.configureByText(
            "EmptyPointcut.java",
            """
            import org.aspectj.lang.annotation.Aspect;
            import org.aspectj.lang.annotation.Pointcut;
            import org.springframework.stereotype.Component;
            @Aspect @Component
            class EmptyPointcut {
                @Pointcut(<warning descr="Pointcut expression cannot be empty">""</warning>)
                public void empty() {}
            }
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, false)
    }

    fun `test trailing operator in pointcut is flagged`() {
        addAopStubs()
        myFixture.enableInspections(PointcutSyntaxInspection())
        myFixture.configureByText(
            "TrailingOp.java",
            """
            import org.aspectj.lang.annotation.Aspect;
            import org.aspectj.lang.annotation.Before;
            import org.springframework.stereotype.Component;
            @Aspect @Component
            class TrailingOp {
                @Before(<warning descr="Pointcut expression cannot end with a logical operator">"execution(* *(..)) ||"</warning>)
                public void advice() {}
            }
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, false)
    }

    fun `test unknown designator in pointcut is flagged`() {
        addAopStubs()
        myFixture.enableInspections(PointcutSyntaxInspection())
        myFixture.configureByText(
            "UnknownDesignator.java",
            """
            import org.aspectj.lang.annotation.Aspect;
            import org.aspectj.lang.annotation.Before;
            import org.springframework.stereotype.Component;
            @Aspect @Component
            class UnknownDesignator {
                @Before(<warning descr="Unknown pointcut designator: 'unknown'">"unknown(* *(..))"</warning>)
                public void advice() {}
            }
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, false)
    }

    fun `test combined valid expression with negation produces no warning`() {
        addAopStubs()
        myFixture.enableInspections(PointcutSyntaxInspection())
        myFixture.configureByText(
            "CombinedValid.java",
            """
            import org.aspectj.lang.annotation.Aspect;
            import org.aspectj.lang.annotation.Before;
            import org.springframework.stereotype.Component;
            @Aspect @Component
            class CombinedValid {
                @Before("execution(* com.example..*(..)) && !within(com.example.internal..*)") 
                public void advice() {}
            }
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, false)
    }

    // ── AspectNotBeanInspection ───────────────────────────────────────────────

    fun `test aspect with Component annotation produces no warning`() {
        addAopStubs()
        myFixture.enableInspections(AspectNotBeanInspection())
        myFixture.configureByText(
            "ValidAspect.java",
            """
            import org.aspectj.lang.annotation.Aspect;
            import org.aspectj.lang.annotation.Before;
            import org.springframework.stereotype.Component;
            @Aspect @Component
            class ValidAspect {
                @Before("execution(* com.example..*(..))")
                public void beforeAdvice() {}
            }
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, false)
    }

    fun `test aspect without Spring bean annotation is flagged`() {
        addAopStubs()
        myFixture.enableInspections(AspectNotBeanInspection())
        myFixture.configureByText(
            "BareAspect.java",
            """
            import org.aspectj.lang.annotation.Aspect;
            import org.aspectj.lang.annotation.Before;
            <warning descr="@Aspect class 'BareAspect' is not a Spring Bean — Spring AOP will not apply it">@Aspect</warning>
            class BareAspect {
                @Before("execution(* com.example..*(..))")
                public void advice() {}
            }
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, false)
    }

    fun `test aspect with Service annotation produces no warning`() {
        addAopStubs()
        myFixture.enableInspections(AspectNotBeanInspection())
        myFixture.configureByText(
            "ServiceAspect.java",
            """
            import org.aspectj.lang.annotation.Aspect;
            import org.springframework.stereotype.Service;
            @Aspect @Service
            class ServiceAspect {}
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, false)
    }

    // ── Kotlin inspections ────────────────────────────────────────────────────

    fun `test kotlin pointcut trailing operator is flagged`() {
        addKotlinAopStubs()
        myFixture.enableInspections(KotlinPointcutSyntaxInspection())
        myFixture.configureByText(
            "KotlinTrailingPointcut.kt",
            """
            import org.aspectj.lang.annotation.Aspect
            import org.aspectj.lang.annotation.Before
            import org.springframework.stereotype.Component
            
            @Aspect
            @Component
            class KotlinTrailingPointcut {
                @Before(<warning descr="Pointcut expression cannot end with a logical operator">"execution(* *(..)) ||"</warning>)
                fun advice() {}
            }
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, false)
    }

    fun `test kotlin pointcut unknown designator is flagged`() {
        addKotlinAopStubs()
        myFixture.enableInspections(KotlinPointcutSyntaxInspection())
        myFixture.configureByText(
            "KotlinUnknownDesignator.kt",
            """
            import org.aspectj.lang.annotation.Aspect
            import org.aspectj.lang.annotation.Before
            import org.springframework.stereotype.Component
            
            @Aspect
            @Component
            class KotlinUnknownDesignator {
                @Before(<warning descr="Unknown pointcut designator: 'unknown'">"unknown(* *(..))"</warning>)
                fun advice() {}
            }
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, false)
    }

    fun `test kotlin aspect without spring bean is flagged`() {
        addKotlinAopStubs()
        myFixture.enableInspections(KotlinAspectNotBeanInspection())
        myFixture.configureByText(
            "BareKotlinAspect.kt",
            """
            import org.aspectj.lang.annotation.Aspect
            
            <warning descr="@Aspect class 'BareKotlinAspect' is not a Spring Bean — Spring AOP will not apply it">@Aspect</warning>
            class BareKotlinAspect
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, false)
    }

    fun `test kotlin aspect with service annotation produces no warning`() {
        addKotlinAopStubs()
        myFixture.enableInspections(KotlinAspectNotBeanInspection())
        myFixture.configureByText(
            "ServiceKotlinAspect.kt",
            """
            import org.aspectj.lang.annotation.Aspect
            import org.springframework.stereotype.Service
            
            @Aspect
            @Service
            class ServiceKotlinAspect
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, false)
    }
}
