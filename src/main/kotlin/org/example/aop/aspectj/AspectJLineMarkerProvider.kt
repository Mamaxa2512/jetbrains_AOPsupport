package org.example.aop.aspectj

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.example.aop.aspectj.psi.AdviceDeclaration
import org.example.aop.aspectj.psi.AspectDeclaration
import org.example.aop.aspectj.psi.DeclareStatement
import org.example.aop.aspectj.psi.InterTypeDeclaration
import org.example.aop.aspectj.psi.PerClause
import org.example.aop.aspectj.psi.PointcutDeclaration

class AspectJLineMarkerProvider : RelatedItemLineMarkerProvider() {
    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        when (element) {
            is AspectDeclaration -> markAspect(element, result)
            is PointcutDeclaration -> markPointcut(element, result)
            is AdviceDeclaration -> markAdvice(element, result)
            is DeclareStatement -> markDeclare(element, result)
            is InterTypeDeclaration -> markInterType(element, result)
            is PerClause -> markPerClause(element, result)
        }
    }

    private fun markAspect(
        aspect: AspectDeclaration,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val anchor = aspect.nameIdentifierLike() ?: return
        val targets = aspect.getAdviceDeclarations() + aspect.getPointcutDeclarations() +
            aspect.getDeclareStatements() + aspect.getInterTypeDeclarations()
        if (targets.isEmpty()) return
        result.add(
            NavigationGutterIconBuilder.create(AllIcons.Gutter.ImplementingMethod)
                .setTargets(targets)
                .setTooltipText(AspectJLineMarkerContext.aspectTooltip(aspect))
                .createLineMarkerInfo(anchor)
        )
    }

    private fun markPointcut(
        pointcut: PointcutDeclaration,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val anchor = pointcut.nameIdentifier ?: return
        val target = PsiTreeUtil.getParentOfType(pointcut, AspectDeclaration::class.java) ?: pointcut
        result.add(
            NavigationGutterIconBuilder.create(AllIcons.Gutter.ImplementedMethod)
                .setTargets(listOf(target))
                .setTooltipText(AspectJLineMarkerContext.pointcutTooltip(pointcut))
                .createLineMarkerInfo(anchor)
        )
    }

    private fun markAdvice(
        advice: AdviceDeclaration,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val anchor = advice.firstChild ?: return
        val aspect = PsiTreeUtil.getParentOfType(advice, AspectDeclaration::class.java)
        val targets = listOfNotNull(aspect).ifEmpty { listOf(advice) }
        result.add(
            NavigationGutterIconBuilder.create(AllIcons.Gutter.OverridingMethod)
                .setTargets(targets)
                .setTooltipText(AspectJLineMarkerContext.adviceTooltip(advice, aspect))
                .createLineMarkerInfo(anchor)
        )
    }

    private fun markDeclare(
        statement: DeclareStatement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val anchor = statement.firstChild ?: return
        val targets = statement.children.filter { it !== anchor }.toList().ifEmpty {
            listOfNotNull(PsiTreeUtil.getParentOfType(statement, AspectDeclaration::class.java))
        }
        result.add(
            NavigationGutterIconBuilder.create(AllIcons.Gutter.ImplementedMethod)
                .setTargets(targets)
                .setTooltipText(AspectJLineMarkerContext.declareTooltip(statement))
                .createLineMarkerInfo(anchor)
        )
    }

    private fun markInterType(
        declaration: InterTypeDeclaration,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val anchor = declaration.firstChild ?: return
        val targets = declaration.getTargetTypeReferences()
            .mapNotNull { AspectJReferenceSupport.resolveTypeReference(it) }
            .ifEmpty { listOfNotNull(PsiTreeUtil.getParentOfType(declaration, AspectDeclaration::class.java)) }
        result.add(
            NavigationGutterIconBuilder.create(AllIcons.Gutter.ImplementingMethod)
                .setTargets(targets)
                .setTooltipText(AspectJLineMarkerContext.interTypeTooltip(declaration))
                .createLineMarkerInfo(anchor)
        )
    }

    private fun markPerClause(
        perClause: PerClause,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val anchor = perClause.firstChild ?: return
        val aspect = PsiTreeUtil.getParentOfType(perClause, AspectDeclaration::class.java) ?: return
        result.add(
            NavigationGutterIconBuilder.create(AllIcons.Gutter.ImplementingMethod)
                .setTargets(listOf(aspect))
                .setTooltipText(AspectJLineMarkerContext.perClauseTooltip(perClause))
                .createLineMarkerInfo(anchor)
        )
    }

    private fun AspectDeclaration.nameIdentifierLike(): PsiElement? {
        var child = firstChild
        while (child != null) {
            if (child.node?.elementType == AspectJTokenTypes.IDENTIFIER) return child
            child = child.nextSibling
        }
        return firstChild
    }
}
