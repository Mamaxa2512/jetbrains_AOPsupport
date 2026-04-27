@file:Suppress("unused")

package org.example.aop.aspectj

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

object AspectJTokenTypes {
    val FILE = IFileElementType(AspectJLanguage)
    val BAD_CHARACTER = IElementType("AJ_BAD_CHARACTER", AspectJLanguage)
    val IDENTIFIER = IElementType("AJ_IDENTIFIER", AspectJLanguage)
    val KEYWORD = IElementType("AJ_KEYWORD", AspectJLanguage)
    val ANNOTATION = IElementType("AJ_ANNOTATION", AspectJLanguage)
    val STRING = IElementType("AJ_STRING", AspectJLanguage)
    val COMMENT = IElementType("AJ_COMMENT", AspectJLanguage)
    val NUMBER = IElementType("AJ_NUMBER", AspectJLanguage)
    val PUNCTUATION = IElementType("AJ_PUNCTUATION", AspectJLanguage)

    val KEYWORDS = setOf(
        "aspect", "pointcut", "before", "after", "around", "returning", "throwing",
        "execution", "call", "within", "this", "target", "args", "bean", "if",
        "declare", "parents", "warning", "error", "privileged", "staticinitialization",
        "initialization", "preinitialization", "handler", "adviceexecution", "cflow", "cflowbelow"
    )

    val COMMENT_TOKENS: TokenSet = TokenSet.create(COMMENT)
    val STRING_TOKENS: TokenSet = TokenSet.create(STRING)
    val KEYWORD_TOKENS: TokenSet = TokenSet.create(KEYWORD)
    val IDENTIFIER_TOKENS: TokenSet = TokenSet.create(IDENTIFIER, ANNOTATION)
}


