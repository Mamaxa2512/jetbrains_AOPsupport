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
            AspectJTokenTypes.ASPECT_KEYWORD -> arrayOf(ASPECT_KEYWORD)
            AspectJTokenTypes.POINTCUT_KEYWORD -> arrayOf(POINTCUT_KEYWORD)
            AspectJTokenTypes.ADVICE_KEYWORD -> arrayOf(ADVICE_KEYWORD)
            AspectJTokenTypes.DESIGNATOR_KEYWORD -> arrayOf(DESIGNATOR_KEYWORD)
            AspectJTokenTypes.DECLARE_KEYWORD, AspectJTokenTypes.DECLARE_KIND_KEYWORD -> arrayOf(DECLARE_KEYWORD)
            AspectJTokenTypes.PER_CLAUSE_KEYWORD -> arrayOf(PER_CLAUSE_KEYWORD)
            AspectJTokenTypes.MODIFIER_KEYWORD, AspectJTokenTypes.KEYWORD -> arrayOf(MODIFIER_KEYWORD)
            AspectJTokenTypes.ANNOTATION -> arrayOf(ANNOTATION)
            AspectJTokenTypes.STRING -> arrayOf(STRING)
            AspectJTokenTypes.COMMENT -> arrayOf(COMMENT)
            AspectJTokenTypes.NUMBER -> arrayOf(NUMBER)
            AspectJTokenTypes.OPERATOR -> arrayOf(OPERATOR)
            else -> emptyArray()
        }
    }

    companion object {
        private val ASPECT_KEYWORD = TextAttributesKey.createTextAttributesKey("AJ_ASPECT_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        private val POINTCUT_KEYWORD = TextAttributesKey.createTextAttributesKey("AJ_POINTCUT_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        private val ADVICE_KEYWORD = TextAttributesKey.createTextAttributesKey("AJ_ADVICE_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        private val DESIGNATOR_KEYWORD = TextAttributesKey.createTextAttributesKey("AJ_DESIGNATOR_KEYWORD", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
        private val DECLARE_KEYWORD = TextAttributesKey.createTextAttributesKey("AJ_DECLARE_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        private val PER_CLAUSE_KEYWORD = TextAttributesKey.createTextAttributesKey("AJ_PER_CLAUSE_KEYWORD", DefaultLanguageHighlighterColors.METADATA)
        private val MODIFIER_KEYWORD = TextAttributesKey.createTextAttributesKey("AJ_MODIFIER_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        private val ANNOTATION = TextAttributesKey.createTextAttributesKey("AJ_ANNOTATION", DefaultLanguageHighlighterColors.METADATA)
        private val STRING = TextAttributesKey.createTextAttributesKey("AJ_STRING", DefaultLanguageHighlighterColors.STRING)
        private val COMMENT = TextAttributesKey.createTextAttributesKey("AJ_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        private val NUMBER = TextAttributesKey.createTextAttributesKey("AJ_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        private val OPERATOR = TextAttributesKey.createTextAttributesKey("AJ_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    }
}

