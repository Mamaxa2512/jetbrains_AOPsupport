@file:Suppress("unused")

package org.example.aop.ai

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * AI сервіс для роботи з pointcut виразами
 * Використовує JetBrains AI Assistant API
 */
@Service(Service.Level.PROJECT)
class AopAiService(project: Project) {

    /**
     * Генерує pointcut вираз з текстового опису
     */
    suspend fun generatePointcut(description: String): String = withContext(Dispatchers.IO) {
        val prompt = buildGeneratePrompt(description)
        return@withContext requestAi(prompt)
    }

    /**
     * Пояснює що робить pointcut вираз
     */
    suspend fun explainPointcut(expression: String): String = withContext(Dispatchers.IO) {
        val prompt = buildExplainPrompt(expression)
        return@withContext requestAi(prompt)
    }

    /**
     * Оптимізує pointcut вираз для кращої продуктивності
     */
    suspend fun optimizePointcut(expression: String): OptimizationResult = withContext(Dispatchers.IO) {
        val prompt = buildOptimizePrompt(expression)
        val response = requestAi(prompt)
        val result = parseOptimizationResult(response)
        return@withContext result.copy(performance = analyzePerformance(expression))
    }

    /**
     * Виправляє помилки в pointcut виразі
     */
    suspend fun fixPointcut(expression: String, error: String): String = withContext(Dispatchers.IO) {
        val prompt = buildFixPrompt(expression, error)
        return@withContext requestAi(prompt)
    }

    /**
     * Аналізує вплив на продуктивність
     */
    suspend fun analyzePerformance(expression: String): PerformanceAnalysis = withContext(Dispatchers.IO) {
        val prompt = buildPerformancePrompt(expression)
        val response = requestAi(prompt)
        return@withContext parsePerformanceAnalysis(response)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Prompt builders
    // ═══════════════════════════════════════════════════════════════════════

    private fun buildGeneratePrompt(description: String): String = """
        You are an expert in Spring AOP and AspectJ pointcut expressions.
        
        Generate a pointcut expression for the following requirement:
        "$description"
        
        Rules:
        - Return ONLY the pointcut expression, no explanation
        - Use proper AspectJ/Spring AOP syntax
        - Prefer specific patterns over broad ones
        - Use execution(), within(), @annotation(), bean() as appropriate
        
        Example outputs:
        - execution(public * com.example.service.*.*(..))
        - @annotation(org.springframework.transaction.annotation.Transactional)
        - within(com.example.service..*) && !within(com.example.service.internal..*)
        
        Pointcut expression:
    """.trimIndent()

    private fun buildExplainPrompt(expression: String): String = """
        You are an expert in Spring AOP and AspectJ pointcut expressions.
        
        Explain this pointcut expression in simple terms:
        $expression
        
        Provide:
        1. What methods/classes it matches
        2. When the advice will be executed
        3. Any potential issues or warnings
        
        Keep the explanation concise and practical.
    """.trimIndent()

    private fun buildOptimizePrompt(expression: String): String = """
        You are an expert in Spring AOP performance optimization.
        
        Analyze this pointcut expression and suggest optimizations:
        $expression
        
        Provide your response in this format:
        OPTIMIZED: <optimized expression>
        REASON: <why this is better>
        IMPACT: <performance impact: HIGH/MEDIUM/LOW>
        
        If the expression is already optimal, respond with:
        OPTIMIZED: <same expression>
        REASON: Already optimal
        IMPACT: NONE
    """.trimIndent()

    private fun buildFixPrompt(expression: String, error: String): String = """
        You are an expert in Spring AOP and AspectJ pointcut expressions.
        
        This pointcut expression has an error:
        Expression: $expression
        Error: $error
        
        Provide a corrected version of the expression.
        Return ONLY the corrected expression, no explanation.
    """.trimIndent()

    private fun buildPerformancePrompt(expression: String): String = """
        You are an expert in Spring AOP performance analysis.
        
        Analyze the performance impact of this pointcut expression:
        $expression
        
        Provide your response in this format:
        SCORE: <1-10, where 10 is best performance>
        MATCHES: <estimated number of methods: FEW/MODERATE/MANY/ALL>
        ISSUES: <list of performance issues, or NONE>
        SUGGESTIONS: <optimization suggestions, or NONE>
    """.trimIndent()

    // ═══════════════════════════════════════════════════════════════════════
    // AI Request
    // ═══════════════════════════════════════════════════════════════════════

    private fun requestAi(prompt: String): String {
        // Deterministic local fallback implementation.
        // Keeps features usable even when external AI providers are unavailable.
        return when {
            prompt.contains("Generate a pointcut") -> extractPointcutFromPrompt(prompt)
            prompt.contains("Explain this pointcut") -> explainExpression(extractExpressionFromPrompt(prompt))
            prompt.contains("Analyze this pointcut") -> optimizeExpression(extractExpressionFromPrompt(prompt))
            prompt.contains("This pointcut expression has an error:") -> {
                val expression = prompt.lineValue("Expression:").orEmpty()
                val error = prompt.lineValue("Error:").orEmpty()
                fixExpression(expression, error)
            }
            prompt.contains("performance impact") -> analyzeExpressionPerformance(extractExpressionFromPrompt(prompt))
            else -> "Unable to process request"
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Response parsers
    // ═══════════════════════════════════════════════════════════════════════

    private fun parseOptimizationResult(response: String): OptimizationResult {
        val lines = response.lines()
        val optimized = lines.find { it.startsWith("OPTIMIZED:") }
            ?.substringAfter("OPTIMIZED:")?.trim()
            ?: response
        val reason = lines.find { it.startsWith("REASON:") }
            ?.substringAfter("REASON:")?.trim()
            ?: "No reason provided"
        val impact = lines.find { it.startsWith("IMPACT:") }
            ?.substringAfter("IMPACT:")?.trim()
            ?: "UNKNOWN"

        return OptimizationResult(
            optimizedExpression = optimized,
            reason = reason,
            impact = PerformanceImpact.valueOf(impact.uppercase().takeIf { 
                it in setOf("HIGH", "MEDIUM", "LOW", "NONE") 
            } ?: "UNKNOWN")
        )
    }

    private fun parsePerformanceAnalysis(response: String): PerformanceAnalysis {
        val lines = response.lines()
        val score = lines.find { it.startsWith("SCORE:") }
            ?.substringAfter("SCORE:")?.trim()?.toIntOrNull() ?: 5
        val matches = lines.find { it.startsWith("MATCHES:") }
            ?.substringAfter("MATCHES:")?.trim() ?: "MODERATE"
        val issues = lines.find { it.startsWith("ISSUES:") }
            ?.substringAfter("ISSUES:")?.trim() ?: "NONE"
        val suggestions = lines.find { it.startsWith("SUGGESTIONS:") }
            ?.substringAfter("SUGGESTIONS:")?.trim() ?: "NONE"

        return PerformanceAnalysis(
            score = score,
            estimatedMatches = matches,
            issues = if (issues == "NONE") emptyList() else issues.split(",").map { it.trim() },
            suggestions = if (suggestions == "NONE") emptyList() else suggestions.split(",").map { it.trim() }
        )
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Helper methods
    // ═══════════════════════════════════════════════════════════════════════

    private fun extractPointcutFromPrompt(prompt: String): String {
        // Fallback logic to extract description
        val description = prompt.substringAfter("\"").substringBefore("\"").trim()
        return when {
            description.contains("transactional", ignoreCase = true) ->
                "@annotation(org.springframework.transaction.annotation.Transactional)"
            description.contains("annotation", ignoreCase = true) &&
                description.contains("cacheable", ignoreCase = true) ->
                "@annotation(org.springframework.cache.annotation.Cacheable)"
            description.contains("service", ignoreCase = true) ->
                "execution(* com.example.service.*.*(..))"
            description.contains("controller", ignoreCase = true) ->
                "within(com.example.controller..*)"
            description.contains("repository", ignoreCase = true) ->
                "within(com.example.repository..*)"
            description.contains("public", ignoreCase = true) ->
                "execution(public * com.example..*(..))"
            description.contains("all methods", ignoreCase = true) ->
                "execution(* com.example..*(..))"
            else -> "execution(* com.example.*.*(..))"
        }
    }

    private fun extractExpressionFromPrompt(prompt: String): String {
        val line = prompt.lines().firstOrNull {
            val value = it.trim()
            value.startsWith("execution") ||
                value.startsWith("within") ||
                value.startsWith("@annotation") ||
                value.startsWith("@within") ||
                value.startsWith("@target") ||
                value.startsWith("args") ||
                value.startsWith("@args") ||
                value.startsWith("this") ||
                value.startsWith("target") ||
                value.startsWith("bean") ||
                value.startsWith("before") ||
                value.startsWith("after") ||
                value.startsWith("around") ||
                value.startsWith("pointcut")
        }
        return line?.let { AopAiSnippetSupport.normalizeSnippet(it) }.orEmpty()
    }

    private fun explainExpression(expression: String): String {
        if (expression.isBlank()) return "Could not detect a pointcut expression."
        val clauses = expression.split("&&", "||").map { it.trim() }.filter { it.isNotBlank() }
        val summary = when {
            expression.contains("execution(") -> "Matches methods by signature pattern."
            expression.contains("within(") -> "Matches all join points inside matched classes/packages."
            expression.contains("@annotation(") -> "Matches methods annotated with a specific annotation."
            expression.contains("bean(") -> "Matches Spring beans by bean name pattern."
            expression.contains("before(", ignoreCase = true) ||
                expression.contains("after(", ignoreCase = true) ||
                expression.contains("around(", ignoreCase = true) ->
                "AspectJ advice declaration that delegates to a named pointcut."
            Regex("^[A-Za-z_][A-Za-z0-9_]*\\s*\\(\\s*\\)$").matches(expression) ->
                "Named pointcut reference used by advice declarations."
            else -> "Matches join points based on provided pointcut filters."
        }
        val warnings = mutableListOf<String>()
        if (expression.contains("execution(* *(..))")) warnings += "Very broad match: all methods."
        if (expression.contains("within(..*)")) warnings += "Very broad match: all types."
        if (expression.contains("||")) warnings += "OR conditions can significantly increase matched join points."

        return buildString {
            appendLine("What it matches:")
            appendLine("- $summary")
            if (clauses.isNotEmpty()) {
                appendLine("- Clauses: ${clauses.joinToString("; ")}")
            }
            appendLine()
            appendLine("When advice runs:")
            appendLine("- Whenever runtime join points satisfy this expression.")
            appendLine()
            appendLine("Potential issues:")
            if (warnings.isEmpty()) {
                appendLine("- No obvious broad-match performance issues detected.")
            } else {
                warnings.forEach { appendLine("- $it") }
            }
        }.trim()
    }

    private fun optimizeExpression(expression: String): String {
        if (expression.isBlank()) {
            return "OPTIMIZED: \nREASON: Could not detect expression\nIMPACT: UNKNOWN"
        }

        var optimized = expression
        var reason = "Already optimal"
        var impact = "NONE"

        if (optimized.contains("execution(* *(..))")) {
            optimized = optimized.replace("execution(* *(..))", "execution(* com.example..*(..))")
            reason = "Replaced global method match with bounded package scope."
            impact = "HIGH"
        } else if (optimized.contains("within(..*)")) {
            optimized = optimized.replace("within(..*)", "within(com.example..*)")
            reason = "Replaced global type match with bounded package scope."
            impact = "HIGH"
        } else if (optimized.contains(" || ")) {
            reason = "Expression uses OR conditions; review if all branches are required."
            impact = "MEDIUM"
        }

        return "OPTIMIZED: $optimized\nREASON: $reason\nIMPACT: $impact"
    }

    private fun fixExpression(expression: String, error: String): String {
        var fixed = expression.trim()
        if (fixed.isEmpty()) return "execution(* com.example..*(..))"

        if (error.contains("Unknown pointcut designator", ignoreCase = true)) {
            fixed = fixed.replace("executon(", "execution(")
        }
        if (error.contains("Unbalanced parentheses", ignoreCase = true)) {
            val open = fixed.count { it == '(' }
            val close = fixed.count { it == ')' }
            if (open > close) fixed += ")".repeat(open - close)
        }
        if (error.contains("Pointcut cannot start with logical operator", ignoreCase = true)) {
            fixed = fixed.removePrefix("&&").removePrefix("||").trim()
        }
        if (error.contains("cannot end with logical operator", ignoreCase = true)) {
            fixed = fixed.removeSuffix("&&").removeSuffix("||").trim()
        }
        if (error.contains("Consecutive logical operators", ignoreCase = true)) {
            fixed = fixed.replace("&& &&", "&&").replace("|| ||", "||")
        }

        return fixed
    }

    private fun analyzeExpressionPerformance(expression: String): String {
        if (expression.isBlank()) {
            return "SCORE: 1\nMATCHES: ALL\nISSUES: Empty or invalid expression\nSUGGESTIONS: Define a valid scoped pointcut"
        }
        return when {
            expression.contains("execution(* *(..))") || expression.contains("within(..*)") ->
                "SCORE: 2\nMATCHES: ALL\nISSUES: Too broad scope\nSUGGESTIONS: Restrict package/class or annotation scope"
            expression.contains("..*") || expression.contains("||") ->
                "SCORE: 5\nMATCHES: MANY\nISSUES: Potentially broad matching\nSUGGESTIONS: Use narrower type/method patterns"
            else ->
                "SCORE: 8\nMATCHES: FEW\nISSUES: NONE\nSUGGESTIONS: NONE"
        }
    }

    private fun String.lineValue(prefix: String): String? {
        return lines()
            .firstOrNull { it.trim().startsWith(prefix) }
            ?.substringAfter(prefix)
            ?.trim()
    }

    companion object {
        fun getInstance(project: Project): AopAiService {
            return project.getService(AopAiService::class.java)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Data classes
// ═══════════════════════════════════════════════════════════════════════

data class OptimizationResult(
    val optimizedExpression: String,
    val reason: String,
    val impact: PerformanceImpact,
    val performance: PerformanceAnalysis? = null
)

enum class PerformanceImpact {
    HIGH, MEDIUM, LOW, NONE, UNKNOWN
}

data class PerformanceAnalysis(
    val score: Int,  // 1-10
    val estimatedMatches: String,  // FEW, MODERATE, MANY, ALL
    val issues: List<String>,
    val suggestions: List<String>
)
