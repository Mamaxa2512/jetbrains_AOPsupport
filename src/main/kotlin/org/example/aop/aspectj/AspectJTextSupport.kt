@file:Suppress("unused")

package org.example.aop.aspectj

import com.intellij.openapi.util.TextRange

internal data class AspectJPointcutOccurrence(
    val expression: String,
    val range: TextRange
)

internal object AspectJTextSupport {
    private val declareKinds = listOf("parents", "warning", "error", "soft", "precedence")
    private val perClauses = listOf("perthis", "pertarget", "percflow", "percflowbelow", "pertypewithin")


    private val advicePattern = Regex(
        """(?m)^\s*(before|after|around)\s*\([^)]*\)\s*(?:returning(?:\s*\([^)]*\))?|throwing(?:\s*\([^)]*\))?)?\s*:\s*(.+?)(?:\s*[;{]\s*|\s*$)""",
        RegexOption.IGNORE_CASE
    )

    private val pointcutPattern = Regex(
        """(?m)^\s*(?:public|protected|private)?\s*pointcut\s+([A-Za-z_][A-Za-z0-9_]*)\s*\([^)]*\)\s*:\s*(.+?)(?:\s*[;{]\s*|\s*$)""",
        RegexOption.IGNORE_CASE
    )

    private val pointcutNamePattern = Regex(
        """(?m)^\s*(?:(?:public|protected|private)\s+)?pointcut\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(""",
        RegexOption.IGNORE_CASE
    )

    private val pointcutContextLinePattern = Regex(
        """(?i)\b(pointcut|before|after|around)\b[^\n]*:\s*[^\n]*$"""
    )

    private val declareContextLinePattern = Regex(
        """(?i)\bdeclare\b(?:\s+\w*)?$"""
    )

    private val aspectHeaderLinePattern = Regex(
        """(?i)^\s*(?:privileged\s+)?aspect\b[^\n]*$"""
    )

    fun collectPointcutOccurrences(fileText: String): List<AspectJPointcutOccurrence> {
        val results = mutableListOf<AspectJPointcutOccurrence>()
        advicePattern.findAll(fileText).forEach { match ->
            addOccurrence(results, match, 2)
        }
        pointcutPattern.findAll(fileText).forEach { match ->
            addOccurrence(results, match, 2)
        }
        return results.distinctBy { it.range }
    }

    fun collectDeclaredPointcutNames(fileText: String): List<String> {
        return pointcutNamePattern.findAll(fileText)
            .mapNotNull { it.groups[1]?.value?.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .toList()
    }

    private fun addOccurrence(results: MutableList<AspectJPointcutOccurrence>, match: MatchResult, groupIndex: Int) {
        val expressionGroup = match.groups[groupIndex] ?: return
        val expression = expressionGroup.value.trim()
        if (expression.isBlank()) return
        results += AspectJPointcutOccurrence(expression, TextRange(expressionGroup.range.first, expressionGroup.range.last + 1))
    }

    fun isPointcutContext(fileText: String, offset: Int): Boolean {
        val linePrefix = linePrefix(fileText, offset)
        return pointcutContextLinePattern.containsMatchIn(linePrefix)
    }

    fun isDeclareContext(fileText: String, offset: Int): Boolean {
        return declareContextLinePattern.containsMatchIn(linePrefix(fileText, offset))
    }

    fun isAspectHeaderContext(fileText: String, offset: Int): Boolean {
        val prefix = linePrefix(fileText, offset)
        return aspectHeaderLinePattern.containsMatchIn(prefix) && !prefix.contains('{')
    }

    fun declareKeywordCompletions(): List<String> = declareKinds

    fun perClauseCompletions(): List<String> = perClauses

    fun extractCompletionPrefix(fileText: String, offset: Int): String {
        val safeOffset = offset.coerceIn(0, fileText.length)
        var start = safeOffset
        while (start > 0) {
            val ch = fileText[start - 1]
            if (ch.isLetterOrDigit() || ch == '_' || ch == '@') {
                start--
            } else {
                break
            }
        }
        return fileText.substring(start, safeOffset)
    }

    private fun linePrefix(fileText: String, offset: Int): String {
        val safeOffset = offset.coerceIn(0, fileText.length)
        val lineStart = fileText.lastIndexOf('\n', safeOffset - 1).let { if (it == -1) 0 else it + 1 }
        return fileText.substring(lineStart, safeOffset)
    }
}





