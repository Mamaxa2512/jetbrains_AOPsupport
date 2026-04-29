@file:Suppress("unused")

package org.example.aop.aspectj

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.psi.tree.IElementType

class AspectJSyntaxHighlighter : SyntaxHighlighter {
    override fun getHighlightingLexer() = AspectJLexer()

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> {
        return when (tokenType) {
            // AspectJ-specific keywords
            AspectJTokenTypes.ASPECT_KEYWORD -> arrayOf(ASPECT_KEYWORD)
            AspectJTokenTypes.POINTCUT_KEYWORD -> arrayOf(POINTCUT_KEYWORD)
            AspectJTokenTypes.ADVICE_KEYWORD -> arrayOf(ADVICE_KEYWORD)
            AspectJTokenTypes.DESIGNATOR_KEYWORD -> arrayOf(DESIGNATOR_KEYWORD)
            AspectJTokenTypes.DECLARE_KEYWORD, AspectJTokenTypes.DECLARE_KIND_KEYWORD -> arrayOf(DECLARE_KEYWORD)
            AspectJTokenTypes.PER_CLAUSE_KEYWORD -> arrayOf(PER_CLAUSE_KEYWORD)
            AspectJTokenTypes.MODIFIER_KEYWORD, AspectJTokenTypes.KEYWORD -> arrayOf(MODIFIER_KEYWORD)

            // Java keywords and types
            AspectJTokenTypes.JAVA_KEYWORD -> arrayOf(JAVA_KEYWORD)
            AspectJTokenTypes.PRIMITIVE_TYPE -> arrayOf(PRIMITIVE_TYPE)

            // Annotations (@Something)
            AspectJTokenTypes.ANNOTATION -> arrayOf(ANNOTATION)

            // Literals
            AspectJTokenTypes.STRING -> arrayOf(STRING)
            AspectJTokenTypes.NUMBER -> arrayOf(NUMBER)
            AspectJTokenTypes.COMMENT -> arrayOf(COMMENT)

            // Operators and punctuation
            AspectJTokenTypes.OPERATOR -> arrayOf(OPERATOR)
            AspectJTokenTypes.SEMICOLON -> arrayOf(SEMICOLON)
            AspectJTokenTypes.COMMA -> arrayOf(COMMA)
            AspectJTokenTypes.DOT -> arrayOf(DOT)
            AspectJTokenTypes.LBRACE, AspectJTokenTypes.RBRACE -> arrayOf(BRACES)
            AspectJTokenTypes.LPAREN, AspectJTokenTypes.RPAREN -> arrayOf(PARENTHESES)
            AspectJTokenTypes.PUNCTUATION -> arrayOf(PUNCTUATION)

            // Identifiers — no explicit coloring, theme decides
            AspectJTokenTypes.IDENTIFIER -> arrayOf(IDENTIFIER)

            // Class references (uppercase-starting identifiers like Object, String, Account)
            AspectJTokenTypes.CLASS_REFERENCE -> arrayOf(CLASS_REFERENCE)

            else -> emptyArray()
        }
    }

    companion object {
        // AspectJ-specific
        private val ASPECT_KEYWORD = TextAttributesKey.createTextAttributesKey("AJ_ASPECT_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        private val POINTCUT_KEYWORD = TextAttributesKey.createTextAttributesKey("AJ_POINTCUT_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        private val ADVICE_KEYWORD = TextAttributesKey.createTextAttributesKey("AJ_ADVICE_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        private val DESIGNATOR_KEYWORD = TextAttributesKey.createTextAttributesKey("AJ_DESIGNATOR_KEYWORD", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
        private val DECLARE_KEYWORD = TextAttributesKey.createTextAttributesKey("AJ_DECLARE_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        private val PER_CLAUSE_KEYWORD = TextAttributesKey.createTextAttributesKey("AJ_PER_CLAUSE_KEYWORD", DefaultLanguageHighlighterColors.METADATA)
        private val MODIFIER_KEYWORD = TextAttributesKey.createTextAttributesKey("AJ_MODIFIER_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)

        // Java keywords and types
        private val JAVA_KEYWORD = TextAttributesKey.createTextAttributesKey("AJ_JAVA_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        private val PRIMITIVE_TYPE = TextAttributesKey.createTextAttributesKey("AJ_PRIMITIVE_TYPE", DefaultLanguageHighlighterColors.KEYWORD)

        // Annotations
        private val ANNOTATION = TextAttributesKey.createTextAttributesKey("AJ_ANNOTATION", DefaultLanguageHighlighterColors.METADATA)

        // Literals
        private val STRING = TextAttributesKey.createTextAttributesKey("AJ_STRING", DefaultLanguageHighlighterColors.STRING)
        private val COMMENT = TextAttributesKey.createTextAttributesKey("AJ_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        private val NUMBER = TextAttributesKey.createTextAttributesKey("AJ_NUMBER", DefaultLanguageHighlighterColors.NUMBER)

        // Operators and punctuation
        private val OPERATOR = TextAttributesKey.createTextAttributesKey("AJ_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        private val SEMICOLON = TextAttributesKey.createTextAttributesKey("AJ_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON)
        private val COMMA = TextAttributesKey.createTextAttributesKey("AJ_COMMA", DefaultLanguageHighlighterColors.COMMA)
        private val DOT = TextAttributesKey.createTextAttributesKey("AJ_DOT", DefaultLanguageHighlighterColors.DOT)
        private val BRACES = TextAttributesKey.createTextAttributesKey("AJ_BRACES", DefaultLanguageHighlighterColors.BRACES)
        private val PARENTHESES = TextAttributesKey.createTextAttributesKey("AJ_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
        private val PUNCTUATION = TextAttributesKey.createTextAttributesKey("AJ_PUNCTUATION", DefaultLanguageHighlighterColors.OPERATION_SIGN)

        // Identifiers
        private val IDENTIFIER = TextAttributesKey.createTextAttributesKey("AJ_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)
        private val CLASS_REFERENCE = TextAttributesKey.createTextAttributesKey("AJ_CLASS_REFERENCE", DefaultLanguageHighlighterColors.CLASS_REFERENCE)
    }
}
