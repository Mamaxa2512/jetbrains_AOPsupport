@file:Suppress("unused")

package org.example.aop.aspectj.psi

import com.intellij.psi.tree.IElementType
import org.example.aop.aspectj.AspectJLanguage

object AspectJElementTypes {
	// File-level elements
	val ASPECT_DECLARATION = IElementType("AJ_ASPECT_DECLARATION", AspectJLanguage)

	// Advice and Pointcuts
	val ADVICE_DECLARATION = IElementType("AJ_ADVICE_DECLARATION", AspectJLanguage)
	val POINTCUT_DECLARATION = IElementType("AJ_POINTCUT_DECLARATION", AspectJLanguage)

	// Pointcut expression parts
	val POINTCUT_EXPRESSION = IElementType("AJ_POINTCUT_EXPRESSION", AspectJLanguage)
	val DESIGNATOR = IElementType("AJ_DESIGNATOR", AspectJLanguage)
	val DESIGNATOR_TYPE = IElementType("AJ_DESIGNATOR_TYPE", AspectJLanguage)
	val DESIGNATOR_CONTENT = IElementType("AJ_DESIGNATOR_CONTENT", AspectJLanguage)
	val DESIGNATOR_REFERENCE = IElementType("AJ_DESIGNATOR_REFERENCE", AspectJLanguage)

	// Operators
	val LOGICAL_OPERATOR = IElementType("AJ_LOGICAL_OPERATOR", AspectJLanguage)

	// Advice metadata
	val ADVICE_TYPE = IElementType("AJ_ADVICE_TYPE", AspectJLanguage)
	val RETURNING = IElementType("AJ_RETURNING", AspectJLanguage)
	val THROWING = IElementType("AJ_THROWING", AspectJLanguage)
	val PARAMETERS = IElementType("AJ_PARAMETERS", AspectJLanguage)

	// Modifiers and identifiers
	val MODIFIER = IElementType("AJ_MODIFIER", AspectJLanguage)

	// Forward compatibility
	val IDENTIFIER = IElementType("AJ_IDENTIFIER", AspectJLanguage)
}

