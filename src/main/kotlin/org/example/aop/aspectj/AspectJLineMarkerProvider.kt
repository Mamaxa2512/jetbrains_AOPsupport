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
        // Line markers must be registered on LEAF elements only.
        // We detect the parent composite type via the leaf's parent chain.
        if (element.firstChild != null) return // skip composite nodes

        val parent = element.parent ?: return
        when (parent) {
            is AspectDeclaration -> {
                if (element == findFirstLeaf(parent)) markAspect(parent, element, result)
            }
            is PointcutDeclaration -> {
                if (element == findFirstLeaf(parent)) markPointcut(parent, element, result)
            }
            is AdviceDeclaration -> {
                if (element == findFirstLeaf(parent)) markAdvice(parent, element, result)
            }
            is DeclareStatement -> {
                if (element == findFirstLeaf(parent)) markDeclare(parent, element, result)
            }
            is InterTypeDeclaration -> {
                if (element == findFirstLeaf(parent)) markInterType(parent, element, result)
            }
            is PerClause -> {
                if (element == findFirstLeaf(parent)) markPerClause(parent, element, result)
            }
        }
    }

    private fun findFirstLeaf(element: PsiElement): PsiElement? {
        var current: PsiElement? = element
        while (current != null && current.firstChild != null) {
            current = current.firstChild
        }
        return current
    }

    private fun markAspect(
        aspect: AspectDeclaration,
        anchor: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
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
        anchor: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
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
        anchor: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
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
        anchor: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val targets = listOfNotNull(PsiTreeUtil.getParentOfType(statement, AspectDeclaration::class.java))
            .ifEmpty { listOf(statement) }
        result.add(
            NavigationGutterIconBuilder.create(AllIcons.Gutter.ImplementedMethod)
                .setTargets(targets)
                .setTooltipText(AspectJLineMarkerContext.declareTooltip(statement))
                .createLineMarkerInfo(anchor)
        )
    }

    private fun markInterType(
        declaration: InterTypeDeclaration,
        anchor: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
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
        anchor: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val aspect = PsiTreeUtil.getParentOfType(perClause, AspectDeclaration::class.java) ?: return
        result.add(
            NavigationGutterIconBuilder.create(AllIcons.Gutter.ImplementingMethod)
                .setTargets(listOf(aspect))
                .setTooltipText(AspectJLineMarkerContext.perClauseTooltip(perClause))
                .createLineMarkerInfo(anchor)
        )
    }
}

