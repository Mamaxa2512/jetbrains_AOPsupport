@file:Suppress("unused")

package org.example.aop.ai

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.util.PsiTreeUtil
import org.example.aop.inspection.AopInspectionRules

internal data class ResolvedAopSnippet(
    val text: String,
    val literal: PsiLiteralExpression? = null,
    val selectionStart: Int? = null,
    val selectionEnd: Int? = null
) {
    val shouldWrapInQuotes: Boolean = literal != null
}

internal object AopAiSnippetSupport {

    private val adviceDeclarationPattern = Regex(
        """^\s*(before|after|around)\s*\([^)]*\)\s*(?:returning(?:\s*\([^)]*\))?|throwing(?:\s*\([^)]*\))?)?\s*:\s*(.+?)\s*[;{]?\s*$""",
        RegexOption.IGNORE_CASE
    )

    private val pointcutDeclarationPattern = Regex(
        """^\s*pointcut\s+[A-Za-z_][A-Za-z0-9_]*\s*\([^)]*\)\s*:\s*(.+?)\s*[;{]?\s*$""",
        RegexOption.IGNORE_CASE
    )

    private val namedPointcutPattern = Regex("""^[A-Za-z_][A-Za-z0-9_]*\s*\(\s*\)$""")

    private val quotedSnippetPattern = Regex("""^(['"])(.*)\1$""", setOf(RegexOption.DOT_MATCHES_ALL))

    fun resolveSnippet(editor: Editor, file: PsiFile): ResolvedAopSnippet? {
        val offset = editor.caretModel.offset.coerceAtMost((file.textLength - 1).coerceAtLeast(0))
        val literal = PsiTreeUtil.getParentOfType(file.findElementAt(offset), PsiLiteralExpression::class.java)
        if (literal != null) {
            val value = literal.value as? String ?: return null
            val normalized = normalizeSnippet(value)
            return if (looksLikeAopSnippet(normalized)) {
                ResolvedAopSnippet(text = normalized, literal = literal)
            } else {
                null
            }
        }

        val selection = editor.selectionModel.selectedText?.takeIf { it.isNotBlank() } ?: return null
        val normalized = normalizeSnippet(selection)
        return if (looksLikeAopSnippet(normalized)) {
            ResolvedAopSnippet(
                text = normalized,
                selectionStart = editor.selectionModel.selectionStart,
                selectionEnd = editor.selectionModel.selectionEnd
            )
        } else {
            null
        }
    }

    fun normalizeSnippet(rawText: String): String {
        val trimmed = rawText.trim()
        val firstLine = trimmed.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
        if (firstLine.isEmpty()) return trimmed

        adviceDeclarationPattern.matchEntire(firstLine)?.let { match ->
            return match.groupValues[2].trim().trimEnd(';', '{', '}')
        }

        pointcutDeclarationPattern.matchEntire(firstLine)?.let { match ->
            return match.groupValues[1].trim().trimEnd(';', '{', '}')
        }

        quotedSnippetPattern.matchEntire(firstLine)?.let { match ->
            return match.groupValues[2].trim()
        }

        return firstLine.trimEnd(';', '{', '}')
    }

    fun looksLikeAopSnippet(rawText: String): Boolean {
        val snippet = normalizeSnippet(rawText)
        if (snippet.isBlank()) return false

        if (AopInspectionRules.supportedPointcutDesignators.any { snippet.contains("$it(") }) return true
        if (namedPointcutPattern.matches(snippet)) return true
        if (adviceDeclarationPattern.containsMatchIn(rawText)) return true
        if (pointcutDeclarationPattern.containsMatchIn(rawText)) return true
        if (snippet.contains("before(", ignoreCase = true) ||
            snippet.contains("after(", ignoreCase = true) ||
            snippet.contains("around(", ignoreCase = true)
        ) return true

        return snippet.contains("@") && snippet.contains("(")
    }

    fun shouldInsertRawSnippet(fileName: String?): Boolean = fileName?.endsWith(".aj", ignoreCase = true) == true

    fun formatGeneratedSnippet(snippet: String, fileName: String?): String {
        return if (shouldInsertRawSnippet(fileName)) {
            snippet
        } else {
            "\"$snippet\""
        }
    }
}




