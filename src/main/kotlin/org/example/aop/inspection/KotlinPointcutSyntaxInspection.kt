package org.example.aop.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

class KotlinPointcutSyntaxInspection : LocalInspectionTool() {

    private val supportedAttributeNames = setOf(null, "value", "pointcut")

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file as? KtFile ?: return PsiElementVisitor.EMPTY_VISITOR
        return object : KtTreeVisitorVoid() {
            override fun visitAnnotationEntry(annotationEntry: KtAnnotationEntry) {
                super.visitAnnotationEntry(annotationEntry)
                if (!annotationEntry.isSupportedAopAnnotation()) return

                annotationEntry.valueArguments
                    .filter { arg -> arg.getArgumentName()?.asName?.identifier in supportedAttributeNames }
                    .mapNotNull { it.getArgumentExpression() as? KtStringTemplateExpression }
                    .forEach { stringExpression ->
                        validatePointcut(stringExpression, holder)
                    }
            }
        }
    }

    private fun validatePointcut(expressionElement: KtStringTemplateExpression, holder: ProblemsHolder) {
        val expression = expressionElement.entries.joinToString(separator = "") { it.text }
        val basicError = AopInspectionRules.validatePointcutExpression(expression)
        if (basicError != null) {
            holder.registerProblem(expressionElement, basicError)
            return
        }

        val parseResult = PointcutParser.parse(expression)
        parseResult.errors.forEach { holder.registerProblem(expressionElement, it.message) }
        parseResult.warnings.forEach {
            holder.registerProblem(
                expressionElement,
                it.message,
                com.intellij.codeInspection.ProblemHighlightType.WEAK_WARNING
            )
        }
    }
}

private fun KtAnnotationEntry.isSupportedAopAnnotation(): Boolean {
    val text = typeReference?.text ?: return false
    return AopInspectionRules.adviceAndPointcutAnnotations.any { qName ->
        val short = qName.substringAfterLast('.')
        text == qName || text.endsWith(".$short") || text == short
    }
}
