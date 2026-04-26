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
        val error = AopInspectionRules.validatePointcutExpression(expression)
        if (error != null) {
            holder.registerProblem(element, error)
        }
    }
}
