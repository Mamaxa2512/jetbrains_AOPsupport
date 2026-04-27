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
			builder.tokenType == AspectJTokenTypes.KEYWORD && builder.tokenText == "aspect" -> {
				parseAspectDeclaration(builder)
			}
			builder.tokenType == AspectJTokenTypes.KEYWORD && builder.tokenText in setOf("before", "after", "around") -> {
				parseAdviceDeclaration(builder)
			}
			builder.tokenType == AspectJTokenTypes.KEYWORD && builder.tokenText == "pointcut" -> {
				parsePointcutDeclaration(builder)
			}
			builder.tokenType == AspectJTokenTypes.KEYWORD && builder.tokenText == "declare" -> {
				skipDeclaration(builder)
			}
			else -> {
				builder.advanceLexer()
			}
		}
	}

	private fun parseAspectDeclaration(builder: PsiBuilder) {
		val marker = builder.mark()

		// "aspect"
		builder.advanceLexer()

		// aspect name (identifier)
		if (builder.tokenType == AspectJTokenTypes.IDENTIFIER) {
			builder.advanceLexer()
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
				builder.tokenType == AspectJTokenTypes.KEYWORD && builder.tokenText in setOf("before", "after", "around") -> {
					parseAdviceDeclaration(builder)
				}
				builder.tokenType == AspectJTokenTypes.KEYWORD && builder.tokenText == "pointcut" -> {
					parsePointcutDeclaration(builder)
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

		// advice type: before, after, around
		builder.advanceLexer()

		// (...)
		skipParentheses(builder)

		// optional: returning(...) or throwing(...)
		while (builder.tokenType == AspectJTokenTypes.KEYWORD && builder.tokenText in setOf("returning", "throwing")) {
			builder.advanceLexer()
			skipParentheses(builder)
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

		// pointcut keyword
		builder.advanceLexer()

		// pointcut name (identifier)
		if (builder.tokenType == AspectJTokenTypes.IDENTIFIER) {
			builder.advanceLexer()
		}

		// (...)
		skipParentheses(builder)

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
				builder.tokenType == AspectJTokenTypes.KEYWORD &&
					builder.tokenText in setOf("execution", "call", "within", "this", "target", "args", "bean", "if",
										"staticinitialization", "initialization", "preinitialization", "handler",
										"adviceexecution", "cflow", "cflowbelow") -> {
					parseDesignator(builder)
				}
				builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText in setOf("&&", "||") -> {
					val opMarker = builder.mark()
					builder.advanceLexer()
					opMarker.done(AspectJElementTypes.LOGICAL_OPERATOR)
				}
				builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == "!" -> {
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

		// designator type (e.g., "execution")
		builder.advanceLexer()

		// (...)
		skipParentheses(builder)

		marker.done(AspectJElementTypes.DESIGNATOR)
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
		// declare warning/error/parents: ...
		while (!builder.eof()) {
			if ((builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == ";") ||
				(builder.tokenType == AspectJTokenTypes.PUNCTUATION && builder.tokenText == "{")) {
				if (builder.tokenText == ";") {
					builder.advanceLexer()
				} else {
					skipBraces(builder)
				}
				break
			}
			builder.advanceLexer()
		}
	}
}


