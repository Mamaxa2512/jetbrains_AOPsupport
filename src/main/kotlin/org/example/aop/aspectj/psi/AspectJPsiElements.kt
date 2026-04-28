@file:Suppress("unused")

package org.example.aop.aspectj.psi

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil

// ============================================================================
// Base PSI Elements
// ============================================================================

abstract class AspectJPsiElement(node: ASTNode) : ASTWrapperPsiElement(node)

// ============================================================================
// File-level Elements
// ============================================================================

class AspectDeclaration(node: ASTNode) : AspectJPsiElement(node) {
	fun getAspectName(): String? {
		var child = firstChild
		while (child != null) {
			if (child.node?.elementType?.toString()?.contains("IDENTIFIER") == true) {
				return child.text
			}
			child = child.nextSibling
		}
		return null
	}

	fun getAdviceDeclarations(): List<AdviceDeclaration> =
		PsiTreeUtil.getChildrenOfTypeAsList(this, AdviceDeclaration::class.java)

	fun getPointcutDeclarations(): List<PointcutDeclaration> =
		PsiTreeUtil.getChildrenOfTypeAsList(this, PointcutDeclaration::class.java)

	fun getDeclareStatements(): List<DeclareStatement> =
		PsiTreeUtil.getChildrenOfTypeAsList(this, DeclareStatement::class.java)

	fun getInterTypeDeclarations(): List<InterTypeDeclaration> =
		PsiTreeUtil.getChildrenOfTypeAsList(this, InterTypeDeclaration::class.java)

	fun getPerClause(): PerClause? =
		PsiTreeUtil.findChildOfType(this, PerClause::class.java)
}

// ============================================================================
// Advice Elements
// ============================================================================

class AdviceDeclaration(node: ASTNode) : AspectJPsiElement(node) {
	fun getAdviceType(): String? {
		var child = firstChild
		while (child != null) {
			if (child.node?.elementType == AspectJElementTypes.ADVICE_TYPE) {
				return child.text
			}
			child = child.nextSibling
		}
		return null
	}

	fun getPointcutExpression(): PointcutExpression? =
		PsiTreeUtil.findChildOfType(this, PointcutExpression::class.java)

	fun getParameters(): PsiElement? {
		var child = firstChild
		while (child != null) {
			if (child.node?.elementType == AspectJElementTypes.PARAMETERS) {
				return child
			}
			child = child.nextSibling
		}
		return null
	}

	fun getReturningType(): String? {
		var child = firstChild
		while (child != null) {
			if (child.node?.elementType == AspectJElementTypes.RETURNING) {
				return child.text
			}
			child = child.nextSibling
		}
		return null
	}

	fun getThrowingType(): String? {
		var child = firstChild
		while (child != null) {
			if (child.node?.elementType == AspectJElementTypes.THROWING) {
				return child.text
			}
			child = child.nextSibling
		}
		return null
	}
}

// ============================================================================
// Pointcut Elements
// ============================================================================

class PointcutDeclaration(node: ASTNode) : AspectJPsiElement(node), PsiNameIdentifierOwner {
	fun getPointcutName(): String? = nameIdentifier?.text

	override fun getNameIdentifier(): PsiElement? {
		var child = firstChild
		while (child != null) {
			if (child.node?.elementType?.toString()?.contains("IDENTIFIER") == true) {
				return child
			}
			child = child.nextSibling
		}
		return null
	}

	override fun getName(): String? = nameIdentifier?.text

	override fun setName(name: String): PsiElement {
		val identifier = nameIdentifier ?: return this
		val file = containingFile ?: return this
		val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return this

		WriteCommandAction.runWriteCommandAction(project) {
			document.replaceString(identifier.textRange.startOffset, identifier.textRange.endOffset, name)
			PsiDocumentManager.getInstance(project).commitDocument(document)
		}

		return this
	}

	fun getModifier(): String? {
		var child = firstChild
		while (child != null) {
			if (child.node?.elementType == AspectJElementTypes.MODIFIER) {
				return child.text
			}
			child = child.nextSibling
		}
		return null
	}

	fun getParameters(): PsiElement? {
		var child = firstChild
		while (child != null) {
			if (child.node?.elementType == AspectJElementTypes.PARAMETERS) {
				return child
			}
			child = child.nextSibling
		}
		return null
	}

	fun getPointcutExpression(): PointcutExpression? =
		PsiTreeUtil.findChildOfType(this, PointcutExpression::class.java)
}

class PointcutExpression(node: ASTNode) : AspectJPsiElement(node) {
	fun getDesignators(): List<Designator> =
		PsiTreeUtil.getChildrenOfTypeAsList(this, Designator::class.java)

	fun getLogicalOperators(): List<PsiElement> =
		PsiTreeUtil.getChildrenOfTypeAsList(this, PsiElement::class.java)
			.filter { it.node?.elementType == AspectJElementTypes.LOGICAL_OPERATOR }
}

class Designator(node: ASTNode) : AspectJPsiElement(node) {
	fun getDesignatorType(): String? {
		var child = firstChild
		while (child != null) {
			if (child.node?.elementType == AspectJElementTypes.DESIGNATOR_TYPE) {
				return child.text
			}
			child = child.nextSibling
		}
		return null
	}

	fun getDesignatorContent(): String? {
		var child = firstChild
		while (child != null) {
			if (child.node?.elementType == AspectJElementTypes.DESIGNATOR_CONTENT) {
				return child.text
			}
			child = child.nextSibling
		}
		return null
	}
}

// ============================================================================
// Full AspectJ Constructs
// ============================================================================

open class DeclareStatement(node: ASTNode) : AspectJPsiElement(node) {
	fun getTypeReferences(): List<TypeReferenceElement> =
		PsiTreeUtil.getChildrenOfTypeAsList(this, TypeReferenceElement::class.java)

	fun getMessage(): DeclareMessage? =
		PsiTreeUtil.findChildOfType(this, DeclareMessage::class.java)
}

class DeclareParentsDeclaration(node: ASTNode) : DeclareStatement(node)

class DeclareWarningDeclaration(node: ASTNode) : DeclareStatement(node)

class DeclareErrorDeclaration(node: ASTNode) : DeclareStatement(node)

class DeclareSoftDeclaration(node: ASTNode) : DeclareStatement(node)

class DeclarePrecedenceDeclaration(node: ASTNode) : DeclareStatement(node)

class InterTypeDeclaration(node: ASTNode) : AspectJPsiElement(node) {
	fun getTargetTypeReferences(): List<TypeReferenceElement> =
		PsiTreeUtil.getChildrenOfTypeAsList(this, TypeReferenceElement::class.java)
}

class PerClause(node: ASTNode) : AspectJPsiElement(node) {
	fun getClauseKind(): String? = firstChild?.text
}

class TypeReferenceElement(node: ASTNode) : AspectJPsiElement(node)

class DeclareMessage(node: ASTNode) : AspectJPsiElement(node)

// ============================================================================
// Supporting Elements
// ============================================================================

class DesignatorReference(node: ASTNode) : AspectJPsiElement(node) {
	val referenceName: String? get() = text
}

