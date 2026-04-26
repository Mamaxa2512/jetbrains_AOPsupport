package org.example.aop.inspection

/**
 * Детальний парсер pointcut виразів для AspectJ/Spring AOP
 * 
 * Підтримує:
 * - Валідацію синтаксису execution, within, args, та інших designators
 * - Перевірку type patterns
 * - Перевірку method signatures
 * - Валідацію wildcards
 * - Аналіз складних виразів з логічними операторами
 */
object PointcutParser {

    data class ParseResult(
        val isValid: Boolean,
        val errors: List<PointcutError> = emptyList(),
        val warnings: List<PointcutWarning> = emptyList()
    )

    data class PointcutError(
        val message: String,
        val position: Int? = null,
        val kind: ErrorKind
    )

    data class PointcutWarning(
        val message: String,
        val position: Int? = null,
        val kind: WarningKind
    )

    enum class ErrorKind {
        INVALID_EXECUTION_PATTERN,
        INVALID_TYPE_PATTERN,
        INVALID_METHOD_PATTERN,
        INVALID_ARGS_PATTERN,
        INVALID_WILDCARD,
        MISSING_PARENTHESES,
        INVALID_MODIFIER,
        INVALID_RETURN_TYPE,
        INVALID_THROWS_CLAUSE,
        EMPTY_DESIGNATOR_ARGS,
        INVALID_BEAN_NAME_PATTERN,
        INVALID_ANNOTATION_TYPE
    }

    enum class WarningKind {
        OVERLY_BROAD_PATTERN,
        DEPRECATED_DESIGNATOR,
        PERFORMANCE_CONCERN,
        REDUNDANT_WILDCARD
    }

    /**
     * Парсить та валідує pointcut вираз
     */
    fun parse(expression: String): ParseResult {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) {
            return ParseResult(false, listOf(
                PointcutError("Pointcut expression cannot be empty", 0, ErrorKind.INVALID_EXECUTION_PATTERN)
            ))
        }

        val errors = mutableListOf<PointcutError>()
        val warnings = mutableListOf<PointcutWarning>()

        // Розбиваємо на окремі designator вирази
        val designators = extractDesignators(trimmed)
        
        for (designator in designators) {
            when (designator.name) {
                "execution" -> validateExecution(designator, errors, warnings)
                "within" -> validateWithin(designator, errors, warnings)
                "this", "target" -> validateTypeReference(designator, errors, warnings)
                "args" -> validateArgs(designator, errors, warnings)
                "@annotation", "@within", "@target" -> validateAnnotationReference(designator, errors, warnings)
                "@args" -> validateAnnotationArgs(designator, errors, warnings)
                "bean" -> validateBean(designator, errors, warnings)
            }
        }

        return ParseResult(errors.isEmpty(), errors, warnings)
    }

    private data class Designator(
        val name: String,
        val args: String,
        val position: Int
    )

    private fun extractDesignators(expression: String): List<Designator> {
        val designators = mutableListOf<Designator>()
        val pattern = Regex("([@]?[a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(")
        
        var searchFrom = 0
        while (searchFrom < expression.length) {
            val match = pattern.find(expression, searchFrom) ?: break
            val name = match.groupValues[1]
            val startPos = match.range.last + 1
            
            // Перевіряємо, чи це дійсно designator (не просто метод в pointcut виразі)
            if (name !in AopInspectionRules.supportedPointcutDesignators && 
                !name.startsWith("@")) {
                searchFrom = match.range.last + 1
                continue
            }
            
            // Знаходимо відповідну закриваючу дужку
            val endPos = findMatchingParen(expression, startPos - 1)
            if (endPos != -1) {
                val args = expression.substring(startPos, endPos)
                designators.add(Designator(name, args, match.range.first))
                searchFrom = endPos + 1
            } else {
                searchFrom = match.range.last + 1
            }
        }
        
        return designators
    }

    private fun findMatchingParen(expr: String, openPos: Int): Int {
        var depth = 1
        for (i in openPos + 1 until expr.length) {
            when (expr[i]) {
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
        return -1
    }

    /**
     * Валідує execution designator
     * Формат: execution(modifiers? return-type declaring-type? method-name(params) throws?)
     */
    private fun validateExecution(designator: Designator, errors: MutableList<PointcutError>, warnings: MutableList<PointcutWarning>) {
        val args = designator.args.trim()
        
        if (args.isEmpty()) {
            errors.add(PointcutError(
                "execution() requires a method signature pattern",
                designator.position,
                ErrorKind.EMPTY_DESIGNATOR_ARGS
            ))
            return
        }

        // Перевірка на занадто широкий pattern
        if (args == "* *(..)" || args == "* *(..)") {
            warnings.add(PointcutWarning(
                "execution(* *(..)) matches ALL methods - this may cause performance issues",
                designator.position,
                WarningKind.OVERLY_BROAD_PATTERN
            ))
        }

        // Базова перевірка: має бути хоча б дужки для параметрів
        if (!args.contains('(') || !args.contains(')')) {
            errors.add(PointcutError(
                "Invalid method signature pattern in execution() - missing parentheses",
                designator.position,
                ErrorKind.INVALID_EXECUTION_PATTERN
            ))
            return
        }

        // Перевіряємо баланс дужок
        if (!hasBalancedParentheses(args)) {
            errors.add(PointcutError(
                "Invalid method signature pattern in execution() - unbalanced parentheses",
                designator.position,
                ErrorKind.INVALID_EXECUTION_PATTERN
            ))
        }
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

    /**
     * Валідує within designator
     * Формат: within(type-pattern)
     */
    private fun validateWithin(designator: Designator, errors: MutableList<PointcutError>, warnings: MutableList<PointcutWarning>) {
        val args = designator.args.trim()
        
        if (args.isEmpty()) {
            errors.add(PointcutError(
                "within() requires a type pattern",
                designator.position,
                ErrorKind.EMPTY_DESIGNATOR_ARGS
            ))
            return
        }

        if (!isValidTypePattern(args)) {
            errors.add(PointcutError(
                "Invalid type pattern in within(): '$args'",
                designator.position,
                ErrorKind.INVALID_TYPE_PATTERN
            ))
        }

        // Попередження про занадто широкий pattern
        if (args == "*" || args == "..*") {
            warnings.add(PointcutWarning(
                "within($args) matches ALL types - consider narrowing the scope",
                designator.position,
                WarningKind.OVERLY_BROAD_PATTERN
            ))
        }
    }

    /**
     * Валідує this/target designators
     */
    private fun validateTypeReference(designator: Designator, errors: MutableList<PointcutError>, warnings: MutableList<PointcutWarning>) {
        val args = designator.args.trim()
        
        if (args.isEmpty()) {
            errors.add(PointcutError(
                "${designator.name}() requires a type",
                designator.position,
                ErrorKind.EMPTY_DESIGNATOR_ARGS
            ))
            return
        }

        if (!isValidTypePattern(args)) {
            errors.add(PointcutError(
                "Invalid type in ${designator.name}(): '$args'",
                designator.position,
                ErrorKind.INVALID_TYPE_PATTERN
            ))
        }
    }

    /**
     * Валідує args designator
     */
    private fun validateArgs(designator: Designator, errors: MutableList<PointcutError>, warnings: MutableList<PointcutWarning>) {
        val args = designator.args.trim()
        
        if (!isValidParameterPattern(args)) {
            errors.add(PointcutError(
                "Invalid parameter pattern in args(): '$args'",
                designator.position,
                ErrorKind.INVALID_ARGS_PATTERN
            ))
        }
    }

    /**
     * Валідує @annotation, @within, @target
     */
    private fun validateAnnotationReference(designator: Designator, errors: MutableList<PointcutError>, warnings: MutableList<PointcutWarning>) {
        val args = designator.args.trim()
        
        if (args.isEmpty()) {
            errors.add(PointcutError(
                "${designator.name}() requires an annotation type",
                designator.position,
                ErrorKind.EMPTY_DESIGNATOR_ARGS
            ))
            return
        }

        if (!isValidAnnotationType(args)) {
            errors.add(PointcutError(
                "Invalid annotation type in ${designator.name}(): '$args'",
                designator.position,
                ErrorKind.INVALID_ANNOTATION_TYPE
            ))
        }
    }

    /**
     * Валідує @args designator
     */
    private fun validateAnnotationArgs(designator: Designator, errors: MutableList<PointcutError>, warnings: MutableList<PointcutWarning>) {
        val args = designator.args.trim()
        
        if (args.isEmpty()) {
            errors.add(PointcutError(
                "@args() requires annotation types",
                designator.position,
                ErrorKind.EMPTY_DESIGNATOR_ARGS
            ))
            return
        }

        // Розбиваємо по комах
        val types = args.split(',').map { it.trim() }
        for (type in types) {
            if (type != ".." && !isValidAnnotationType(type)) {
                errors.add(PointcutError(
                    "Invalid annotation type in @args(): '$type'",
                    designator.position,
                    ErrorKind.INVALID_ANNOTATION_TYPE
                ))
            }
        }
    }

    /**
     * Валідує bean designator (Spring-specific)
     */
    private fun validateBean(designator: Designator, errors: MutableList<PointcutError>, warnings: MutableList<PointcutWarning>) {
        val args = designator.args.trim()
        
        if (args.isEmpty()) {
            errors.add(PointcutError(
                "bean() requires a bean name or pattern",
                designator.position,
                ErrorKind.EMPTY_DESIGNATOR_ARGS
            ))
            return
        }

        if (!isValidBeanNamePattern(args)) {
            errors.add(PointcutError(
                "Invalid bean name pattern: '$args'",
                designator.position,
                ErrorKind.INVALID_BEAN_NAME_PATTERN
            ))
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Допоміжні функції валідації
    // ═══════════════════════════════════════════════════════════════════════

    private fun isValidTypePattern(pattern: String): Boolean {
        if (pattern.isEmpty()) return false
        
        // Дозволяємо wildcards
        if (pattern == "*" || pattern == "..") return true
        
        // Перевіряємо package pattern: com.example..*
        if (pattern.endsWith("..*")) {
            val packagePart = pattern.dropLast(3)
            return isValidPackageName(packagePart)
        }
        
        // Перевіряємо package pattern: com.example.*
        if (pattern.endsWith(".*")) {
            val packagePart = pattern.dropLast(2)
            return isValidPackageName(packagePart)
        }
        
        // Wildcards в імені: com.example.*Service
        if (pattern.contains('*')) {
            val parts = pattern.split('.')
            return parts.all { part ->
                part.isEmpty() || part == "*" || part == ".." || isValidIdentifierWithWildcard(part)
            }
        }
        
        // Звичайне qualified name
        return isValidQualifiedName(pattern)
    }

    private fun isValidMethodNamePattern(pattern: String): Boolean {
        if (pattern.isEmpty()) return false
        if (pattern == "*") return true
        
        // Wildcards: get*, *Service, *
        if (pattern.contains('*')) {
            return isValidIdentifierWithWildcard(pattern)
        }
        
        return isValidIdentifier(pattern)
    }

    private fun isValidParameterPattern(pattern: String): Boolean {
        if (pattern == ".." || pattern.isEmpty()) return true
        
        // Розбиваємо по комах
        val params = pattern.split(',').map { it.trim() }
        
        for (param in params) {
            if (param == ".." || param == "*") continue
            if (!isValidTypePattern(param)) return false
        }
        
        return true
    }

    private fun isValidAnnotationType(pattern: String): Boolean {
        if (pattern.isEmpty()) return false
        
        // Може бути qualified name або просто ім'я
        return isValidQualifiedName(pattern) || isValidIdentifier(pattern)
    }

    private fun isValidBeanNamePattern(pattern: String): Boolean {
        if (pattern.isEmpty()) return false
        
        // Bean names можуть містити wildcards
        if (pattern.contains('*')) {
            return pattern.all { it.isLetterOrDigit() || it in setOf('*', '-', '_') }
        }
        
        return pattern.all { it.isLetterOrDigit() || it in setOf('-', '_') }
    }

    private fun isValidPackageName(name: String): Boolean {
        if (name.isEmpty()) return true
        
        val parts = name.split('.')
        return parts.all { it.isEmpty() || isValidIdentifier(it) }
    }

    private fun isValidQualifiedName(name: String): Boolean {
        if (name.isEmpty()) return false
        
        val parts = name.split('.')
        return parts.all { isValidIdentifier(it) }
    }

    private fun isValidIdentifier(name: String): Boolean {
        if (name.isEmpty()) return false
        if (!name[0].isJavaIdentifierStart()) return false
        return name.all { it.isJavaIdentifierPart() }
    }

    private fun isValidIdentifierWithWildcard(name: String): Boolean {
        if (name.isEmpty()) return false
        if (name == "*") return true
        
        // Перевіряємо, що всі символи або identifier parts або *
        return name.all { it.isJavaIdentifierPart() || it == '*' }
    }
}
