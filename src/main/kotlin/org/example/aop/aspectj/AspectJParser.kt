package org.example.aop.aspectj

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType
import org.example.aop.aspectj.psi.AspectJElementTypes

class AspectJParser : PsiParser {
	override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
		val rootMarker = builder.mark()

		while (!builder.eof()) {
			parseTopLevel(builder)
		}

		rootMarker.done(root)
		return builder.treeBuilt
	}

	private fun parseTopLevel(builder: PsiBuilder) {
		when {
			isModifierKeyword(builder, "privileged") || isAspectKeyword(builder) -> {
				parseAspectDeclaration(builder)
			}
			isModifiedPointcutDeclaration(builder) -> {
				parsePointcutDeclaration(builder)
			}
			isAdviceKeyword(builder) -> {
				parseAdviceDeclaration(builder)
			}
			isPointcutKeyword(builder) -> {
				parsePointcutDeclaration(builder)
			}
			isDeclareKeyword(builder) -> {
				parseDeclareStatement(builder)
			}
			else -> {
				builder.advanceLexer()
			}
		}
	}

	private fun parseAspectDeclaration(builder: PsiBuilder) {
		val marker = builder.mark()

		parseModifiers(builder)

		if (isAspectKeyword(builder)) {
			builder.advanceLexer()
		}

		// aspect name (identifier)
		if (builder.tokenType == AspectJTokenTypes.IDENTIFIER) {
			builder.advanceLexer()
		}

		if (builder.tokenType == AspectJTokenTypes.PER_CLAUSE_KEYWORD) {
			parsePerClause(builder)
		}

		// skip tokens until we find { or end
		var braceDepth = 0
		while (!builder.eof()) {
			when {
				builder.tokenType?.let { it.toString() == "AJ_PUNCTUATION" } == true && builder.tokenText == "{" -> {
					braceDepth++
					builder.advanceLexer()
					break
				}
				builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == "{" -> {
					braceDepth++
					builder.advanceLexer()
					break
				}
				else -> {
					builder.advanceLexer()
				}
			}
		}

		// parse body
		while (!builder.eof() && braceDepth > 0) {
			when {
				builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == "{" -> {
					braceDepth++
					builder.advanceLexer()
				}
				builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == "}" -> {
					braceDepth--
					if (braceDepth == 0) {
						builder.advanceLexer()
						break
					}
					builder.advanceLexer()
				}
				isAdviceKeyword(builder) -> {
					parseAdviceDeclaration(builder)
				}
				isModifiedPointcutDeclaration(builder) -> {
					parsePointcutDeclaration(builder)
				}
				isPointcutKeyword(builder) -> {
					parsePointcutDeclaration(builder)
				}
				isDeclareKeyword(builder) -> {
					parseDeclareStatement(builder)
				}
				looksLikeInterTypeDeclaration(builder) -> {
					parseInterTypeDeclaration(builder)
				}
				else -> {
					builder.advanceLexer()
				}
			}
		}

		marker.done(AspectJElementTypes.ASPECT_DECLARATION)
	}

	private fun parseAdviceDeclaration(builder: PsiBuilder) {
		val marker = builder.mark()

		markCurrentToken(builder, AspectJElementTypes.ADVICE_TYPE)

		parseParameters(builder)

		// optional: returning(...) or throwing(...)
		while (builder.tokenType == AspectJTokenTypes.ADVICE_KEYWORD && builder.tokenText in setOf("returning", "throwing")) {
			val elementType = if (builder.tokenText == "returning") AspectJElementTypes.RETURNING else AspectJElementTypes.THROWING
			val returningMarker = builder.mark()
			builder.advanceLexer()
			skipParentheses(builder)
			returningMarker.done(elementType)
		}

		// : (colon)
		if (builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == ":") {
			builder.advanceLexer()
		}

		// pointcut expression
		parsePointcutExpression(builder)

		// skip to { or ;
		while (!builder.eof()) {
			when {
				builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText in setOf("{", ";") -> {
					if (builder.tokenText == "{") {
						skipBraces(builder)
					} else {
						builder.advanceLexer()
					}
					break
				}
				else -> builder.advanceLexer()
			}
		}

		marker.done(AspectJElementTypes.ADVICE_DECLARATION)
	}

	private fun parsePointcutDeclaration(builder: PsiBuilder) {
		val marker = builder.mark()

		parseModifiers(builder)

		if (isPointcutKeyword(builder)) {
			builder.advanceLexer()
		}

		// pointcut name (identifier)
		if (builder.tokenType == AspectJTokenTypes.IDENTIFIER) {
			builder.advanceLexer()
		}

		parseParameters(builder)

		// : (colon)
		if (builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == ":") {
			builder.advanceLexer()
		}

		// pointcut expression
		parsePointcutExpression(builder)

		// ; or end
		if (builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == ";") {
			builder.advanceLexer()
		}

		marker.done(AspectJElementTypes.POINTCUT_DECLARATION)
	}

	private fun parsePointcutExpression(builder: PsiBuilder) {
		val marker = builder.mark()

		parseDesignatorChain(builder)

		marker.done(AspectJElementTypes.POINTCUT_EXPRESSION)
	}

	private fun parseDesignatorChain(builder: PsiBuilder) {
		while (!builder.eof()) {
			when {
				builder.tokenType == AspectJTokenTypes.DESIGNATOR_KEYWORD -> {
					parseDesignator(builder)
				}
				builder.tokenType == AspectJTokenTypes.OPERATOR && builder.tokenText in setOf("&&", "||") -> {
					val opMarker = builder.mark()
					builder.advanceLexer()
					opMarker.done(AspectJElementTypes.LOGICAL_OPERATOR)
				}
				builder.tokenType == AspectJTokenTypes.OPERATOR && builder.tokenText == "!" -> {
					val opMarker = builder.mark()
					builder.advanceLexer()
					opMarker.done(AspectJElementTypes.LOGICAL_OPERATOR)
				}
				builder.tokenType == AspectJTokenTypes.IDENTIFIER -> {
					// pointcut reference
					val refMarker = builder.mark()
					builder.advanceLexer()
					refMarker.done(AspectJElementTypes.DESIGNATOR_REFERENCE)
				}
				builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText in setOf("(", ")", ";", "{", ":") -> {
					break
				}
				else -> {
					if (builder.tokenType == null) break
					builder.advanceLexer()
				}
			}
		}
	}

	private fun parseDesignator(builder: PsiBuilder) {
		val marker = builder.mark()

		markCurrentToken(builder, AspectJElementTypes.DESIGNATOR_TYPE)

		val contentMarker = builder.mark()
		skipParentheses(builder)
		contentMarker.done(AspectJElementTypes.DESIGNATOR_CONTENT)

		marker.done(AspectJElementTypes.DESIGNATOR)
	}

	private fun parseDeclareStatement(builder: PsiBuilder) {
		val marker = builder.mark()
		builder.advanceLexer()
		val statementType = declareStatementType(builder.tokenText)

		if (builder.tokenType == AspectJTokenTypes.DECLARE_KIND_KEYWORD) {
			builder.advanceLexer()
		}

		when (statementType) {
			AspectJElementTypes.DECLARE_WARNING,
			AspectJElementTypes.DECLARE_ERROR -> parseDeclareMessageStatement(builder)
			AspectJElementTypes.DECLARE_SOFT -> parseDeclareSoftStatement(builder)
			AspectJElementTypes.DECLARE_PARENTS -> parseDeclareParentsStatement(builder)
			AspectJElementTypes.DECLARE_PRECEDENCE -> skipUntilSemicolon(builder)
			else -> skipUntilSemicolon(builder)
		}

		marker.done(statementType)
	}

	private fun parseDeclareMessageStatement(builder: PsiBuilder) {
		if (builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == ":") {
			builder.advanceLexer()
		}
		parsePointcutExpression(builder)
		if (builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == ":") {
			builder.advanceLexer()
			if (builder.tokenType == AspectJTokenTypes.STRING) {
				val messageMarker = builder.mark()
				builder.advanceLexer()
				messageMarker.done(AspectJElementTypes.DECLARE_MESSAGE)
			}
		}
		consumeOptionalSemicolon(builder)
	}

	private fun parseDeclareSoftStatement(builder: PsiBuilder) {
		if (builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == ":") {
			builder.advanceLexer()
		}
		parseTypeReference(builder)
		if (builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == ":") {
			builder.advanceLexer()
		}
		parsePointcutExpression(builder)
		consumeOptionalSemicolon(builder)
	}

	private fun parseDeclareParentsStatement(builder: PsiBuilder) {
		if (builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == ":") {
			builder.advanceLexer()
		}
		parseTypeReference(builder, stopKeywords = setOf("extends", "implements"))
		while (!builder.eof()) {
			when {
				builder.tokenType == AspectJTokenTypes.IDENTIFIER && builder.tokenText in setOf("extends", "implements") -> builder.advanceLexer()
				builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == "," -> builder.advanceLexer()
				builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == ";" -> {
					builder.advanceLexer()
					return
				}
				builder.tokenType == AspectJTokenTypes.IDENTIFIER || builder.tokenType == AspectJTokenTypes.ANNOTATION -> parseTypeReference(builder)
				else -> builder.advanceLexer()
			}
		}
	}

	private fun parseInterTypeDeclaration(builder: PsiBuilder) {
		val marker = builder.mark()
		parseModifiers(builder)
		while (!builder.eof()) {
			when {
				builder.tokenType == AspectJTokenTypes.IDENTIFIER -> {
					parseTypeReference(builder)
				}
				builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == "(" -> {
					parseParameters(builder)
				}
				builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == "{" -> {
					skipBraces(builder)
					break
				}
				builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == ";" -> {
					builder.advanceLexer()
					break
				}
				else -> builder.advanceLexer()
			}
		}
		marker.done(AspectJElementTypes.INTER_TYPE_DECLARATION)
	}

	private fun parsePerClause(builder: PsiBuilder) {
		val marker = builder.mark()
		builder.advanceLexer()
		skipParentheses(builder)
		marker.done(AspectJElementTypes.PER_CLAUSE)
	}

	private fun parseTypeReference(builder: PsiBuilder, stopKeywords: Set<String> = emptySet()) {
		val marker = builder.mark()
		var consumed = false
		while (!builder.eof()) {
			when {
				builder.tokenType == AspectJTokenTypes.IDENTIFIER && builder.tokenText in stopKeywords && consumed -> break
				builder.tokenType == AspectJTokenTypes.IDENTIFIER ||
					builder.tokenType == AspectJTokenTypes.ANNOTATION -> {
						builder.advanceLexer()
						consumed = true
					}
				builder.tokenType == AspectJTokenTypes.OPERATOR && builder.tokenText in setOf("*", "+") -> {
					builder.advanceLexer()
					consumed = true
				}
				builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText in setOf(".", "[", "]") -> {
					builder.advanceLexer()
					consumed = true
				}
				else -> break
			}
		}
		marker.done(AspectJElementTypes.TYPE_REFERENCE)
	}

	private fun parseParameters(builder: PsiBuilder) {
		if (builder.tokenType != AspectJTokenTypes.PUNCTUATION || builder.tokenText != "(") {
			return
		}
		val marker = builder.mark()
		skipParentheses(builder)
		marker.done(AspectJElementTypes.PARAMETERS)
	}

	private fun skipParentheses(builder: PsiBuilder) {
		if (builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == "(") {
			var depth = 0
			while (!builder.eof()) {
				when {
					builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == "(" -> {
						depth++
						builder.advanceLexer()
					}
					builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == ")" -> {
						depth--
						builder.advanceLexer()
						if (depth == 0) break
					}
					else -> builder.advanceLexer()
				}
			}
		}
	}

	private fun skipBraces(builder: PsiBuilder) {
		if (builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == "{") {
			var depth = 0
			while (!builder.eof()) {
				when {
					builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == "{" -> {
						depth++
						builder.advanceLexer()
					}
					builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == "}" -> {
						depth--
						builder.advanceLexer()
						if (depth == 0) break
					}
					else -> builder.advanceLexer()
				}
			}
		}
	}

	private fun skipDeclaration(builder: PsiBuilder) {
		skipUntilSemicolon(builder)
	}

	private fun parseModifiers(builder: PsiBuilder) {
		while (builder.tokenType == AspectJTokenTypes.MODIFIER_KEYWORD) {
			markCurrentToken(builder, AspectJElementTypes.MODIFIER)
		}
	}

	private fun markCurrentToken(builder: PsiBuilder, elementType: IElementType) {
		val marker = builder.mark()
		builder.advanceLexer()
		marker.done(elementType)
	}

	private fun skipUntilSemicolon(builder: PsiBuilder) {
		while (!builder.eof()) {
			if (builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == ";") {
				builder.advanceLexer()
				return
			}
			builder.advanceLexer()
		}
	}

	private fun consumeOptionalSemicolon(builder: PsiBuilder) {
		if (builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == ";") {
			builder.advanceLexer()
		}
	}

	private fun isAspectKeyword(builder: PsiBuilder): Boolean =
		builder.tokenType == AspectJTokenTypes.ASPECT_KEYWORD

	private fun isAdviceKeyword(builder: PsiBuilder): Boolean =
		builder.tokenType == AspectJTokenTypes.ADVICE_KEYWORD && builder.tokenText in setOf("before", "after", "around")

	private fun isPointcutKeyword(builder: PsiBuilder): Boolean =
		builder.tokenType == AspectJTokenTypes.POINTCUT_KEYWORD

	private fun isDeclareKeyword(builder: PsiBuilder): Boolean =
		builder.tokenType == AspectJTokenTypes.DECLARE_KEYWORD

	private fun isModifierKeyword(builder: PsiBuilder, text: String): Boolean =
		builder.tokenType == AspectJTokenTypes.MODIFIER_KEYWORD && builder.tokenText == text

	private fun isModifiedPointcutDeclaration(builder: PsiBuilder): Boolean {
		if (builder.tokenType != AspectJTokenTypes.MODIFIER_KEYWORD) {
			return false
		}
		val marker = builder.mark()
		parseModifiers(builder)
		val isPointcut = isPointcutKeyword(builder)
		marker.rollbackTo()
		return isPointcut
	}

	private fun looksLikeInterTypeDeclaration(builder: PsiBuilder): Boolean {
		return builder.tokenType == AspectJTokenTypes.IDENTIFIER || builder.tokenType == AspectJTokenTypes.MODIFIER_KEYWORD
	}

	private fun declareStatementType(tokenText: String?): IElementType {
		return when (tokenText) {
			"parents" -> AspectJElementTypes.DECLARE_PARENTS
			"warning" -> AspectJElementTypes.DECLARE_WARNING
			"error" -> AspectJElementTypes.DECLARE_ERROR
			"soft" -> AspectJElementTypes.DECLARE_SOFT
			"precedence" -> AspectJElementTypes.DECLARE_PRECEDENCE
			else -> AspectJElementTypes.DECLARE_STATEMENT
		}
	}
}
