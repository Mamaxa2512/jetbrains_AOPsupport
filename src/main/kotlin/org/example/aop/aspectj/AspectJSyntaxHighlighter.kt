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
            AspectJTokenTypes.KEYWORD -> arrayOf(KEYWORD)
            AspectJTokenTypes.ANNOTATION -> arrayOf(ANNOTATION)
            AspectJTokenTypes.STRING -> arrayOf(STRING)
            AspectJTokenTypes.COMMENT -> arrayOf(COMMENT)
            AspectJTokenTypes.NUMBER -> arrayOf(NUMBER)
            else -> emptyArray()
        }
    }

    companion object {
        private val KEYWORD = TextAttributesKey.createTextAttributesKey("AJ_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        private val ANNOTATION = TextAttributesKey.createTextAttributesKey("AJ_ANNOTATION", DefaultLanguageHighlighterColors.METADATA)
        private val STRING = TextAttributesKey.createTextAttributesKey("AJ_STRING", DefaultLanguageHighlighterColors.STRING)
        private val COMMENT = TextAttributesKey.createTextAttributesKey("AJ_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        private val NUMBER = TextAttributesKey.createTextAttributesKey("AJ_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    }
}


