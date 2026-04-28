package org.example.aop.aspectj

import org.example.aop.aspectj.psi.AdviceDeclaration
import org.example.aop.aspectj.psi.AspectDeclaration
import org.example.aop.aspectj.psi.DeclareErrorDeclaration
import org.example.aop.aspectj.psi.DeclareParentsDeclaration
import org.example.aop.aspectj.psi.DeclarePrecedenceDeclaration
import org.example.aop.aspectj.psi.DeclareSoftDeclaration
import org.example.aop.aspectj.psi.DeclareStatement
import org.example.aop.aspectj.psi.DeclareWarningDeclaration
import org.example.aop.aspectj.psi.InterTypeDeclaration
import org.example.aop.aspectj.psi.PerClause
import org.example.aop.aspectj.psi.PointcutDeclaration

internal object AspectJLineMarkerContext {
    fun aspectTooltip(aspect: AspectDeclaration): String {
        val adviceCount = aspect.getAdviceDeclarations().size
        val pointcutCount = aspect.getPointcutDeclarations().size
        val declareCount = aspect.getDeclareStatements().size
        val itdCount = aspect.getInterTypeDeclarations().size
        return "AspectJ aspect - $adviceCount advice, $pointcutCount pointcut, $declareCount declare, $itdCount ITD"
    }

    fun pointcutTooltip(pointcut: PointcutDeclaration): String {
        val name = pointcut.getPointcutName() ?: "<anonymous>"
        val modifier = pointcut.getModifier()?.let { "$it " } ?: ""
        return "${modifier}pointcut $name()"
    }

    fun adviceTooltip(advice: AdviceDeclaration, aspect: AspectDeclaration?): String {
        val adviceType = advice.getAdviceType() ?: "advice"
        val aspectName = aspect?.getAspectName() ?: "<anonymous>"
        return "$adviceType advice in aspect $aspectName"
    }

    fun declareTooltip(statement: DeclareStatement): String = when (statement) {
        is DeclareParentsDeclaration -> "declare parents"
        is DeclareWarningDeclaration -> "declare warning"
        is DeclareErrorDeclaration -> "declare error"
        is DeclareSoftDeclaration -> "declare soft"
        is DeclarePrecedenceDeclaration -> "declare precedence"
        else -> "declare statement"
    }

    fun interTypeTooltip(declaration: InterTypeDeclaration): String {
        val targets = declaration.getTargetTypeReferences().map { it.text }.distinct()
        val renderedTargets = if (targets.isEmpty()) "<unknown>" else targets.joinToString(", ")
        return "inter-type declaration for $renderedTargets"
    }

    fun perClauseTooltip(perClause: PerClause): String {
        val kind = perClause.getClauseKind() ?: "per-clause"
        return "$kind aspect instantiation model"
    }
}
