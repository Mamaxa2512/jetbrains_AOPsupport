package org.example.aop.aspectj

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiParser
import com.intellij.lang.ParserDefinition
import com.intellij.lexer.Lexer
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.example.aop.aspectj.psi.*

class AspectJParserDefinition : ParserDefinition {
    override fun createLexer(project: com.intellij.openapi.project.Project?): Lexer = AspectJLexer()

    override fun createParser(project: com.intellij.openapi.project.Project?): PsiParser = AspectJParser()

    override fun getFileNodeType(): com.intellij.psi.tree.IFileElementType = AspectJTokenTypes.FILE
    override fun getCommentTokens(): TokenSet = AspectJTokenTypes.COMMENT_TOKENS
    override fun getStringLiteralElements(): TokenSet = AspectJTokenTypes.STRING_TOKENS

    override fun createElement(node: ASTNode): PsiElement {
        return when (node.elementType) {
            AspectJElementTypes.ASPECT_DECLARATION -> AspectDeclaration(node)
            AspectJElementTypes.PER_CLAUSE -> PerClause(node)
            AspectJElementTypes.DECLARE_STATEMENT -> DeclareStatement(node)
            AspectJElementTypes.DECLARE_PARENTS -> DeclareParentsDeclaration(node)
            AspectJElementTypes.DECLARE_WARNING -> DeclareWarningDeclaration(node)
            AspectJElementTypes.DECLARE_ERROR -> DeclareErrorDeclaration(node)
            AspectJElementTypes.DECLARE_SOFT -> DeclareSoftDeclaration(node)
            AspectJElementTypes.DECLARE_PRECEDENCE -> DeclarePrecedenceDeclaration(node)
            AspectJElementTypes.INTER_TYPE_DECLARATION -> InterTypeDeclaration(node)
            AspectJElementTypes.TYPE_REFERENCE -> TypeReferenceElement(node)
            AspectJElementTypes.DECLARE_MESSAGE -> DeclareMessage(node)
            AspectJElementTypes.ADVICE_DECLARATION -> AdviceDeclaration(node)
            AspectJElementTypes.POINTCUT_DECLARATION -> PointcutDeclaration(node)
            AspectJElementTypes.POINTCUT_EXPRESSION -> PointcutExpression(node)
            AspectJElementTypes.DESIGNATOR -> Designator(node)
            AspectJElementTypes.DESIGNATOR_REFERENCE -> DesignatorReference(node)
            else -> com.intellij.extapi.psi.ASTWrapperPsiElement(node)
        }
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile = AspectJPsiFile(viewProvider)
    override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): ParserDefinition.SpaceRequirements = ParserDefinition.SpaceRequirements.MAY
}

