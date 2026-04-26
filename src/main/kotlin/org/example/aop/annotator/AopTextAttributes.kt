package org.example.aop.annotator

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey

object AopTextAttributes {
    val AOP_ANNOTATION: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
        "AOP_ANNOTATION",
        DefaultLanguageHighlighterColors.METADATA
    )
}
