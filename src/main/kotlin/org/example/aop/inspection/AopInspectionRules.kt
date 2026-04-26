package org.example.aop.inspection

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass

object AopInspectionRules {
    const val AOP_ANNOTATION_PACKAGE = "org.aspectj.lang.annotation"
    const val ASPECT_ANNOTATION = "org.aspectj.lang.annotation.Aspect"

    val adviceAndPointcutAnnotations = setOf(
        "org.aspectj.lang.annotation.Before",
        "org.aspectj.lang.annotation.After",
        "org.aspectj.lang.annotation.Around",
        "org.aspectj.lang.annotation.AfterReturning",
        "org.aspectj.lang.annotation.AfterThrowing",
        "org.aspectj.lang.annotation.Pointcut"
    )

    val aopAnnotationQualifiedNames = setOf(ASPECT_ANNOTATION) + adviceAndPointcutAnnotations

    val aopAnnotationShortNames = aopAnnotationQualifiedNames
        .map { it.substringAfterLast('.') }
        .toSet()

    val springBeanAnnotations = setOf(
        "org.springframework.stereotype.Component",
        "org.springframework.stereotype.Service",
        "org.springframework.stereotype.Repository",
        "org.springframework.stereotype.Controller",
        "org.springframework.web.bind.annotation.RestController",
        "org.springframework.context.annotation.Configuration",
        "org.springframework.web.bind.annotation.ControllerAdvice",
        "org.springframework.web.bind.annotation.RestControllerAdvice"
    )

    val supportedPointcutDesignators = setOf(
        "execution", "within", "this", "target", "args",
        "@target", "@within", "@annotation", "@args", "bean",
        "cflow", "cflowbelow", "initialization", "preinitialization",
        "staticinitialization", "handler", "adviceexecution"
    )

    val pointcutCompletionItems = listOf(
        "execution" to "execution(modifiers? return-type declaring-type? method-name(params) throws?)",
        "within" to "within(type-pattern)",
        "@annotation" to "@annotation(annotation-type)",
        "@within" to "@within(annotation-type)",
        "this" to "this(type)",
        "target" to "target(type)",
        "args" to "args(param-types)",
        "@args" to "@args(annotation-types)",
        "@target" to "@target(annotation-type)",
        "bean" to "bean(name-or-pattern)"
    )

    private val functionLikeTokenPattern = Regex("([@]?[A-Za-z_][A-Za-z0-9_.]*)\\s*\\(")
    private val trailingOperatorPattern = Regex("(&&|\\|\\|)$")
    private val consecutiveOperatorPattern = Regex("(&&|\\|\\|)\\s*(&&|\\|\\|)")
    private val missingOperatorBetweenClausesPattern = Regex("\\)\\s*(!?[@A-Za-z_])")

    data class PointcutValidationError(
        val kind: PointcutValidationErrorKind,
        val message: String
    )

    enum class PointcutValidationErrorKind {
        EMPTY_EXPRESSION,
        EMPTY_NEGATION,
        UNBALANCED_PARENTHESES,
        LEADING_OPERATOR,
        TRAILING_OPERATOR,
        CONSECUTIVE_LOGICAL_OPERATORS,
        MISSING_OPERATOR_BETWEEN_CLAUSES,
        UNKNOWN_DESIGNATOR,
    }

    fun hasSpringBeanAnnotation(annotations: Set<String>): Boolean {
        return annotations.any { it in springBeanAnnotations }
    }

    fun isSpringBeanClass(psiClass: PsiClass): Boolean {
        val annotations = psiClass.modifierList?.annotations.orEmpty()
        val visited = mutableSetOf<String>()
        return annotations.any { isSpringBeanAnnotation(it, visited) }
    }

    fun validatePointcutExpression(rawExpression: String): String? {
        return validatePointcutExpressionDetailed(rawExpression)?.message
    }

    fun validatePointcutExpressionDetailed(rawExpression: String): PointcutValidationError? {
        val expression = rawExpression.trim()

        if (expression.isEmpty()) {
            return PointcutValidationError(
                PointcutValidationErrorKind.EMPTY_EXPRESSION,
                "Pointcut expression cannot be empty"
            )
        }

        if (expression.all { it == '!' }) {
            return PointcutValidationError(
                PointcutValidationErrorKind.EMPTY_NEGATION,
                "Pointcut expression cannot contain only negation operator(s)"
            )
        }

        if (!hasBalancedParentheses(expression)) {
            return PointcutValidationError(
                PointcutValidationErrorKind.UNBALANCED_PARENTHESES,
                "Pointcut expression has unbalanced parentheses"
            )
        }

        if (expression.startsWith("&&") || expression.startsWith("||")) {
            return PointcutValidationError(
                PointcutValidationErrorKind.LEADING_OPERATOR,
                "Pointcut expression cannot start with a logical operator"
            )
        }

        if (trailingOperatorPattern.containsMatchIn(expression)) {
            return PointcutValidationError(
                PointcutValidationErrorKind.TRAILING_OPERATOR,
                "Pointcut expression cannot end with a logical operator"
            )
        }

        if (consecutiveOperatorPattern.containsMatchIn(expression)) {
            return PointcutValidationError(
                PointcutValidationErrorKind.CONSECUTIVE_LOGICAL_OPERATORS,
                "Pointcut expression has consecutive logical operators"
            )
        }

        if (missingOperatorBetweenClausesPattern.containsMatchIn(expression)) {
            return PointcutValidationError(
                PointcutValidationErrorKind.MISSING_OPERATOR_BETWEEN_CLAUSES,
                "Pointcut expression is missing a logical operator between clauses"
            )
        }

        val functionLikeTokens = functionLikeTokenPattern.findAll(expression).map { it.groupValues[1] }
        for (token in functionLikeTokens) {
            if ('.' !in token && token !in supportedPointcutDesignators) {
                return PointcutValidationError(
                    PointcutValidationErrorKind.UNKNOWN_DESIGNATOR,
                    "Unknown pointcut designator: '$token'"
                )
            }
        }

        return null
    }

    private fun isSpringBeanAnnotation(annotation: PsiAnnotation, visited: MutableSet<String>): Boolean {
        val qualifiedName = annotation.qualifiedName ?: return false
        if (qualifiedName in springBeanAnnotations) return true
        if (!visited.add(qualifiedName)) return false

        val annotationClass = annotation.nameReferenceElement?.resolve() as? PsiClass ?: return false
        return annotationClass.modifierList
            ?.annotations
            .orEmpty()
            .any { isSpringBeanAnnotation(it, visited) }
    }

    private fun hasBalancedParentheses(expr: String): Boolean {
        var depth = 0
        for (ch in expr) {
            when (ch) {
                '(' -> depth++
                ')' -> if (--depth < 0) return false
            }
        }
        return depth == 0
    }
}





