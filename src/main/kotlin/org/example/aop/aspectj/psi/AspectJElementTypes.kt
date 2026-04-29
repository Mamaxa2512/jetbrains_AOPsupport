@file:Suppress("unused")

package org.example.aop.aspectj.psi

import com.intellij.psi.tree.IElementType
import org.example.aop.aspectj.AspectJLanguage

object AspectJElementTypes {
	// File-level elements
	val ASPECT_DECLARATION = IElementType("AJ_ASPECT_DECLARATION", AspectJLanguage)
	val PER_CLAUSE = IElementType("AJ_PER_CLAUSE", AspectJLanguage)
	val DECLARE_STATEMENT = IElementType("AJ_DECLARE_STATEMENT", AspectJLanguage)
	val DECLARE_PARENTS = IElementType("AJ_DECLARE_PARENTS", AspectJLanguage)
	val DECLARE_WARNING = IElementType("AJ_DECLARE_WARNING", AspectJLanguage)
	val DECLARE_ERROR = IElementType("AJ_DECLARE_ERROR", AspectJLanguage)
	val DECLARE_SOFT = IElementType("AJ_DECLARE_SOFT", AspectJLanguage)
	val DECLARE_PRECEDENCE = IElementType("AJ_DECLARE_PRECEDENCE", AspectJLanguage)
	val INTER_TYPE_DECLARATION = IElementType("AJ_INTER_TYPE_DECLARATION", AspectJLanguage)
	val TYPE_REFERENCE = IElementType("AJ_TYPE_REFERENCE", AspectJLanguage)
	val DECLARE_MESSAGE = IElementType("AJ_DECLARE_MESSAGE", AspectJLanguage)

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

	val IDENTIFIER = IElementType("AJ_IDENTIFIER", AspectJLanguage)

	// Generics and patterns
	val TYPE_PARAMETER = IElementType("AJ_TYPE_PARAMETER", AspectJLanguage)
	val TYPE_ARGUMENT = IElementType("AJ_TYPE_ARGUMENT", AspectJLanguage)
	val ANNOTATION_PATTERN = IElementType("AJ_ANNOTATION_PATTERN", AspectJLanguage)
}
