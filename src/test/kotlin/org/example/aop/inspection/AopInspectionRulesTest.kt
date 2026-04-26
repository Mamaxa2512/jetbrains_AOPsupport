package org.example.aop.inspection

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AopInspectionRulesTest {

	@Test
	fun `accepts simple valid execution expression`() {
		val error = AopInspectionRules.validatePointcutExpressionDetailed("execution(* com.example..*(..))")

		assertNull(error)
	}

	@Test
	fun `rejects empty expression`() {
		val error = AopInspectionRules.validatePointcutExpressionDetailed("   ")

		assertEquals(AopInspectionRules.PointcutValidationErrorKind.EMPTY_EXPRESSION, error?.kind)
	}

	@Test
	fun `rejects empty negation expression`() {
		val error = AopInspectionRules.validatePointcutExpressionDetailed("!!!")

		assertEquals(AopInspectionRules.PointcutValidationErrorKind.EMPTY_NEGATION, error?.kind)
	}

	@Test
	fun `rejects unbalanced parentheses`() {
		val error = AopInspectionRules.validatePointcutExpressionDetailed("execution(* com.example..*(..)")

		assertEquals(AopInspectionRules.PointcutValidationErrorKind.UNBALANCED_PARENTHESES, error?.kind)
	}

	@Test
	fun `rejects leading logical operator`() {
		val error = AopInspectionRules.validatePointcutExpressionDetailed("&& execution(* *(..))")

		assertEquals(AopInspectionRules.PointcutValidationErrorKind.LEADING_OPERATOR, error?.kind)
	}

	@Test
	fun `rejects trailing logical operator`() {
		val error = AopInspectionRules.validatePointcutExpressionDetailed("execution(* *(..)) ||")

		assertEquals(AopInspectionRules.PointcutValidationErrorKind.TRAILING_OPERATOR, error?.kind)
	}

	@Test
	fun `rejects consecutive logical operators`() {
		val error = AopInspectionRules.validatePointcutExpressionDetailed("execution(* *(..)) && || within(com.example..*)")

		assertEquals(AopInspectionRules.PointcutValidationErrorKind.CONSECUTIVE_LOGICAL_OPERATORS, error?.kind)
	}

	@Test
	fun `rejects missing operator between clauses`() {
		val error = AopInspectionRules.validatePointcutExpressionDetailed("execution(* *(..)) within(com.example..*)")

		assertEquals(AopInspectionRules.PointcutValidationErrorKind.MISSING_OPERATOR_BETWEEN_CLAUSES, error?.kind)
	}

	@Test
	fun `rejects unknown designator`() {
		val error = AopInspectionRules.validatePointcutExpressionDetailed("unknown(* *(..))")

		assertEquals(AopInspectionRules.PointcutValidationErrorKind.UNKNOWN_DESIGNATOR, error?.kind)
		assertTrue(error?.message?.contains("unknown") == true)
	}

	@Test
	fun `allows reference style token with package qualifier`() {
		val error = AopInspectionRules.validatePointcutExpressionDetailed("com.example.MyAspect.somePointcut()")

		assertNull(error)
	}

	@Test
	fun `accepts valid combined designator expression`() {
		val error = AopInspectionRules.validatePointcutExpressionDetailed(
			"execution(* com.example..*(..)) && !within(com.example.internal..*)"
		)

		assertNull(error)
	}

	@Test
	fun `validatePointcutExpression returns message for invalid expression`() {
		val error = AopInspectionRules.validatePointcutExpression("execution(* *(..)) && || within(com.example..*)")

		assertTrue(error?.contains("consecutive logical operators") == true)
	}

	@Test
	fun `detects known Spring bean annotation names`() {
		val annotationNames = setOf(
			"org.aspectj.lang.annotation.Aspect",
			"org.springframework.stereotype.Service"
		)

		assertTrue(AopInspectionRules.hasSpringBeanAnnotation(annotationNames))
	}

	@Test
	fun `aop annotation short names stay aligned with qualified names`() {
		val recomputed = AopInspectionRules.aopAnnotationQualifiedNames
			.map { it.substringAfterLast('.') }
			.toSet()

		assertEquals(recomputed, AopInspectionRules.aopAnnotationShortNames)
	}

	@Test
	fun `completion designators are supported by validator`() {
		val completionDesignators = AopInspectionRules.pointcutCompletionItems.map { it.first }.toSet()

		assertTrue(completionDesignators.all { it in AopInspectionRules.supportedPointcutDesignators })
	}
}



