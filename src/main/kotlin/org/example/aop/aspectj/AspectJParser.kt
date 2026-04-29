package org.example.aop.aspectj

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType
import org.example.aop.aspectj.psi.AspectJElementTypes

class AspectJParser : PsiParser {
	/**
	 * Returns true if the current token is an identifier-like token
	 * (regular identifiers or uppercase class references like Object, String).
	 */
	private fun isIdentifierLike(builder: PsiBuilder): Boolean =
		builder.tokenType == AspectJTokenTypes.IDENTIFIER ||
		builder.tokenType == AspectJTokenTypes.CLASS_REFERENCE
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
			isJavaKeywordWithText(builder, "package") -> {
				skipUntilSemicolon(builder)
			}
			isJavaKeywordWithText(builder, "import") -> {
				skipUntilSemicolon(builder)
			}
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
				// Silently consume tokens that are valid Java but not AspectJ-specific
				// (e.g., comments, annotations at file level, class declarations)
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
		if (isIdentifierLike(builder)) {
			builder.advanceLexer()
		}

		if (builder.tokenType == AspectJTokenTypes.PER_CLAUSE_KEYWORD) {
			parsePerClause(builder)
		}

		// skip tokens until we find { or end
		var braceDepth = 0
		while (!builder.eof()) {
			when {
				isToken(builder, "{") -> {
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
				isToken(builder, "{") -> {
					braceDepth++
					builder.advanceLexer()
				}
				isToken(builder, "}") -> {
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
				looksLikeReturnTypedAdvice(builder) -> {
					parseReturnTypedAdviceDeclaration(builder)
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
		if (isToken(builder, ":")) {
			builder.advanceLexer()
		}

		// pointcut expression
		parsePointcutExpression(builder)

		// skip to { or ;
		while (!builder.eof()) {
			when {
				isToken(builder, "{") -> {
					skipBraces(builder)
					break
				}
				isToken(builder, ";") -> {
					builder.advanceLexer()
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
		if (isIdentifierLike(builder)) {
			builder.advanceLexer()
		}

		parseParameters(builder)

		// : (colon)
		if (isToken(builder, ":")) {
			builder.advanceLexer()
		}

		// pointcut expression
		parsePointcutExpression(builder)

		// ; or end
		if (isToken(builder, ";")) {
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
				isIdentifierLike(builder) -> {
					// pointcut reference
					val refMarker = builder.mark()
					builder.advanceLexer()
					refMarker.done(AspectJElementTypes.DESIGNATOR_REFERENCE)
				}
				isToken(builder, "(") || isToken(builder, ")") ||
				isToken(builder, ";") || isToken(builder, "{") || isToken(builder, ":") -> {
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
			AspectJElementTypes.DECLARE_PRECEDENCE -> parseDeclarePrecedenceStatement(builder)
			else -> skipUntilSemicolon(builder)
		}

		marker.done(statementType)
	}

	private fun parseDeclareMessageStatement(builder: PsiBuilder) {
		if (isToken(builder, ":")) {
			builder.advanceLexer()
		}
		parsePointcutExpression(builder)
		if (isToken(builder, ":")) {
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
		if (isToken(builder, ":")) {
			builder.advanceLexer()
		}
		parseTypeReference(builder)
		if (isToken(builder, ":")) {
			builder.advanceLexer()
		}
		parsePointcutExpression(builder)
		consumeOptionalSemicolon(builder)
	}

	private fun parseDeclareParentsStatement(builder: PsiBuilder) {
		if (isToken(builder, ":")) {
			builder.advanceLexer()
		}
		parseTypeReference(builder, stopKeywords = setOf("extends", "implements"))
		while (!builder.eof()) {
			when {
				isJavaKeywordWithText(builder, "extends") || isJavaKeywordWithText(builder, "implements") -> builder.advanceLexer()
				isToken(builder, ",") -> builder.advanceLexer()
				isToken(builder, ";") -> {
					builder.advanceLexer()
					return
				}
				isIdentifierLike(builder) || builder.tokenType == AspectJTokenTypes.ANNOTATION -> parseTypeReference(builder)
				else -> builder.advanceLexer()
			}
		}
	}

	private fun parseDeclarePrecedenceStatement(builder: PsiBuilder) {
		if (isToken(builder, ":")) {
			builder.advanceLexer()
		}
		while (!builder.eof()) {
			when {
				isToken(builder, ";") -> {
					builder.advanceLexer()
					return
				}
				isToken(builder, ",") -> builder.advanceLexer()
				isIdentifierLike(builder) || builder.tokenType == AspectJTokenTypes.ANNOTATION -> {
					parseTypeReference(builder)
				}
				builder.tokenType == AspectJTokenTypes.OPERATOR && builder.tokenText in setOf("*", "+") -> {
					parseTypeReference(builder)
				}
				else -> builder.advanceLexer()
			}
		}
	}

	private fun parseInterTypeDeclaration(builder: PsiBuilder) {
		val marker = builder.mark()
		parseModifiers(builder)
		while (!builder.eof()) {
			when {
				isIdentifierLike(builder) ||
				builder.tokenType == AspectJTokenTypes.PRIMITIVE_TYPE -> {
					parseTypeReference(builder)
				}
				isToken(builder, "(") -> {
					parseParameters(builder)
				}
				isToken(builder, "{") -> {
					skipBraces(builder)
					break
				}
				isToken(builder, ";") -> {
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
		builder.advanceLexer() // consume per-clause keyword
		if (isToken(builder, "(")) {
			builder.advanceLexer()
			parsePointcutExpression(builder)
			if (isToken(builder, ")")) {
				builder.advanceLexer()
			}
		} else {
			skipParentheses(builder)
		}
		marker.done(AspectJElementTypes.PER_CLAUSE)
	}

	private fun parseTypeReference(builder: PsiBuilder, stopKeywords: Set<String> = emptySet()) {
		val marker = builder.mark()
		var consumed = false
		while (!builder.eof()) {
			when {
				builder.tokenText in stopKeywords && consumed -> break
				isIdentifierLike(builder) ||
					builder.tokenType == AspectJTokenTypes.ANNOTATION ||
					builder.tokenType == AspectJTokenTypes.JAVA_KEYWORD ||
					builder.tokenType == AspectJTokenTypes.PRIMITIVE_TYPE -> {
						builder.advanceLexer()
						consumed = true
					}
				builder.tokenType == AspectJTokenTypes.OPERATOR && builder.tokenText in setOf("*", "+") -> {
					builder.advanceLexer()
					consumed = true
				}
				isToken(builder, ".") || isToken(builder, "[") || isToken(builder, "]") -> {
					builder.advanceLexer()
					consumed = true
				}
				builder.tokenType == AspectJTokenTypes.OPERATOR && builder.tokenText == "<" -> {
					parseTypeArguments(builder)
					consumed = true
				}
				else -> break
			}
		}
		marker.done(AspectJElementTypes.TYPE_REFERENCE)
	}

	private fun parseTypeArguments(builder: PsiBuilder) {
		val marker = builder.mark()
		builder.advanceLexer() // consume <
		while (!builder.eof()) {
			when {
				builder.tokenType == AspectJTokenTypes.OPERATOR && builder.tokenText == ">" -> {
					builder.advanceLexer()
					break
				}
				isToken(builder, ",") -> {
					builder.advanceLexer()
				}
				else -> {
					val argMarker = builder.mark()
					val before = builder.currentOffset
					parseTypeReference(builder)
					if (builder.currentOffset == before) {
						// Prevent infinite loop if parseTypeReference doesn't consume anything
						builder.advanceLexer()
					}
					argMarker.done(AspectJElementTypes.TYPE_ARGUMENT)
				}
			}
		}
		marker.done(AspectJElementTypes.TYPE_PARAMETER)
	}

	private fun parseParameters(builder: PsiBuilder) {
		if (!isToken(builder, "(")) {
			return
		}
		val marker = builder.mark()
		skipParentheses(builder)
		marker.done(AspectJElementTypes.PARAMETERS)
	}

	private fun skipParentheses(builder: PsiBuilder) {
		if (isToken(builder, "(")) {
			var depth = 0
			while (!builder.eof()) {
				when {
					isToken(builder, "(") -> {
						depth++
						builder.advanceLexer()
					}
					isToken(builder, ")") -> {
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
		if (isToken(builder, "{")) {
			var depth = 0
			while (!builder.eof()) {
				when {
					isToken(builder, "{") -> {
						depth++
						builder.advanceLexer()
					}
					isToken(builder, "}") -> {
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
			if (isToken(builder, ";")) {
				builder.advanceLexer()
				return
			}
			builder.advanceLexer()
		}
	}

	private fun consumeOptionalSemicolon(builder: PsiBuilder) {
		if (isToken(builder, ";")) {
			builder.advanceLexer()
		}
	}

	/**
	 * Token-text matching helper that works regardless of which specific punctuation
	 * token type the lexer emits (PUNCTUATION, SEMICOLON, LBRACE, LPAREN, DOT, etc.).
	 */
	private fun isToken(builder: PsiBuilder, text: String): Boolean {
		return builder.tokenText == text
	}

	private fun isJavaKeywordWithText(builder: PsiBuilder, text: String): Boolean {
		return builder.tokenType == AspectJTokenTypes.JAVA_KEYWORD && builder.tokenText == text
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
		// ITDs look like: [modifier] <type> <QualifiedName>.<member> ...
		// They must contain a dot (e.g., com.example.Service.method)
		// If there's no dot, it's likely a regular Java statement or advice with return type
		if (!isIdentifierLike(builder) &&
			builder.tokenType != AspectJTokenTypes.MODIFIER_KEYWORD &&
			builder.tokenType != AspectJTokenTypes.PRIMITIVE_TYPE) {
			return false
		}
		val marker = builder.mark()
		// Skip modifiers
		while (builder.tokenType == AspectJTokenTypes.MODIFIER_KEYWORD) {
			builder.advanceLexer()
		}
		// Skip return type / type tokens
		var foundDot = false
		var depth = 0
		while (!builder.eof()) {
			when {
				isToken(builder, ".") -> {
					foundDot = true
					builder.advanceLexer()
				}
				isIdentifierLike(builder) ||
				builder.tokenType == AspectJTokenTypes.PRIMITIVE_TYPE -> {
					builder.advanceLexer()
				}
				isToken(builder, "(") -> break
				isToken(builder, ";") -> break
				isToken(builder, "{") -> break
				isToken(builder, "}") -> break
				builder.tokenType == AspectJTokenTypes.OPERATOR -> break
				else -> break
			}
		}
		marker.rollbackTo()
		return foundDot
	}

	/**
	 * Detects advice with a return type prefix, like `Object around(): pointcut() { }`
	 */
	private fun looksLikeReturnTypedAdvice(builder: PsiBuilder): Boolean {
		if (!isIdentifierLike(builder) &&
			builder.tokenType != AspectJTokenTypes.PRIMITIVE_TYPE) {
			return false
		}
		val marker = builder.mark()
		builder.advanceLexer() // skip return type
		val isAdvice = isAdviceKeyword(builder)
		marker.rollbackTo()
		return isAdvice
	}

	/**
	 * Parse advice that has a return type prefix: `Object around(): pointcutName() { ... }`
	 */
	private fun parseReturnTypedAdviceDeclaration(builder: PsiBuilder) {
		val marker = builder.mark()
		// Skip the return type
		builder.advanceLexer()
		// Now parse as regular advice
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
		if (isToken(builder, ":")) {
			builder.advanceLexer()
		}

		// pointcut expression
		parsePointcutExpression(builder)

		// skip to { or ;
		while (!builder.eof()) {
			when {
				isToken(builder, "{") -> {
					skipBraces(builder)
					break
				}
				isToken(builder, ";") -> {
					builder.advanceLexer()
					break
				}
				else -> builder.advanceLexer()
			}
		}

		marker.done(AspectJElementTypes.ADVICE_DECLARATION)
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
