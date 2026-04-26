package org.example.aop.inspection

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.*

class PointcutSyntaxInspection : AbstractBaseJavaLocalInspectionTool() {

    private val supportedAttributeNames = listOf(
        PsiAnnotation.DEFAULT_REFERENCED_METHOD_NAME,
        "pointcut"
    )

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : JavaElementVisitor() {
            override fun visitAnnotation(annotation: PsiAnnotation) {
                if (annotation.qualifiedName !in AopInspectionRules.adviceAndPointcutAnnotations) return

                val seenLiterals = mutableSetOf<PsiLiteralExpression>()
                for (attributeName in supportedAttributeNames) {
                    val valueLiteral = annotation.findAttributeValue(attributeName) as? PsiLiteralExpression ?: continue
                    if (!seenLiterals.add(valueLiteral)) continue

                    val expression = valueLiteral.value as? String ?: continue
                    validate(expression, valueLiteral, holder)
                }
            }
        }
    }

    private fun validate(expression: String, element: PsiElement, holder: ProblemsHolder) {
        // Спочатку базова валідація
        val basicError = AopInspectionRules.validatePointcutExpression(expression)
        if (basicError != null) {
            // Реєструємо проблему з AI Quick Fix
            holder.registerProblem(
                element,
                basicError,
                org.example.aop.ai.quickfix.AiFixPointcutQuickFix(basicError)
            )
            return
        }

        // Потім детальний синтаксичний аналіз
        val parseResult = PointcutParser.parse(expression)
        
        // Реєструємо помилки з AI Quick Fix
        for (error in parseResult.errors) {
            holder.registerProblem(
                element,
                error.message,
                org.example.aop.ai.quickfix.AiFixPointcutQuickFix(error.message)
            )
        }
        
        // Реєструємо попередження (як weak warnings)
        for (warning in parseResult.warnings) {
            holder.registerProblem(
                element,
                warning.message,
                com.intellij.codeInspection.ProblemHighlightType.WEAK_WARNING
            )
        }
    }
}
