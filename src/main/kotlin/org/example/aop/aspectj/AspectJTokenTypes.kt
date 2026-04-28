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
    val ASPECT_KEYWORD = IElementType("AJ_ASPECT_KEYWORD", AspectJLanguage)
    val POINTCUT_KEYWORD = IElementType("AJ_POINTCUT_KEYWORD", AspectJLanguage)
    val ADVICE_KEYWORD = IElementType("AJ_ADVICE_KEYWORD", AspectJLanguage)
    val DESIGNATOR_KEYWORD = IElementType("AJ_DESIGNATOR_KEYWORD", AspectJLanguage)
    val DECLARE_KEYWORD = IElementType("AJ_DECLARE_KEYWORD", AspectJLanguage)
    val DECLARE_KIND_KEYWORD = IElementType("AJ_DECLARE_KIND_KEYWORD", AspectJLanguage)
    val PER_CLAUSE_KEYWORD = IElementType("AJ_PER_CLAUSE_KEYWORD", AspectJLanguage)
    val MODIFIER_KEYWORD = IElementType("AJ_MODIFIER_KEYWORD", AspectJLanguage)
    val ANNOTATION = IElementType("AJ_ANNOTATION", AspectJLanguage)
    val STRING = IElementType("AJ_STRING", AspectJLanguage)
    val COMMENT = IElementType("AJ_COMMENT", AspectJLanguage)
    val NUMBER = IElementType("AJ_NUMBER", AspectJLanguage)
    val OPERATOR = IElementType("AJ_OPERATOR", AspectJLanguage)
    val PUNCTUATION = IElementType("AJ_PUNCTUATION", AspectJLanguage)

    val ASPECT_KEYWORDS = setOf("aspect")
    val POINTCUT_KEYWORDS = setOf("pointcut")
    val ADVICE_KEYWORDS = setOf("before", "after", "around", "returning", "throwing")
    val DESIGNATOR_KEYWORDS = setOf(
        "execution", "call", "within", "this", "target", "args", "bean", "if",
        "staticinitialization", "initialization", "preinitialization", "handler",
        "adviceexecution", "cflow", "cflowbelow"
    )
    val DECLARE_KEYWORDS = setOf("declare")
    val DECLARE_KIND_KEYWORDS = setOf("parents", "warning", "error", "soft", "precedence")
    val PER_CLAUSE_KEYWORDS = setOf("perthis", "pertarget", "percflow", "percflowbelow", "pertypewithin")
    val MODIFIER_KEYWORDS = setOf("public", "protected", "private", "abstract", "final", "static", "privileged")
    val KEYWORDS = ASPECT_KEYWORDS + POINTCUT_KEYWORDS + ADVICE_KEYWORDS + DESIGNATOR_KEYWORDS +
        DECLARE_KEYWORDS + DECLARE_KIND_KEYWORDS + PER_CLAUSE_KEYWORDS + MODIFIER_KEYWORDS

    val COMMENT_TOKENS: TokenSet = TokenSet.create(COMMENT)
    val STRING_TOKENS: TokenSet = TokenSet.create(STRING)
    val KEYWORD_TOKENS: TokenSet = TokenSet.create(
        KEYWORD,
        ASPECT_KEYWORD,
        POINTCUT_KEYWORD,
        ADVICE_KEYWORD,
        DESIGNATOR_KEYWORD,
        DECLARE_KEYWORD,
        DECLARE_KIND_KEYWORD,
        PER_CLAUSE_KEYWORD,
        MODIFIER_KEYWORD
    )
    val IDENTIFIER_TOKENS: TokenSet = TokenSet.create(IDENTIFIER, ANNOTATION)
}

