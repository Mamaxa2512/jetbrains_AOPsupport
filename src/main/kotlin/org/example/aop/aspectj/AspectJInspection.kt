package org.example.aop.aspectj

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.search.searches.ReferencesSearch
import org.example.aop.aspectj.psi.DesignatorReference
import org.example.aop.aspectj.psi.PointcutDeclaration

/**
 * Inspection for AspectJ code to detect issues:
 * - Undefined pointcut references
 * - Unused pointcut declarations
 */
class AspectJInspection : LocalInspectionTool() {

    override fun getDisplayName() = "AspectJ code analysis"
    override fun getShortName() = "AspectJInspection"
    override fun getGroupDisplayName() = "AspectJ"

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                when (element) {
                    is DesignatorReference -> checkReference(element, holder)
                    is PointcutDeclaration -> checkPointcutUsage(element, holder)
                }
                super.visitElement(element)
            }
        }
    }

    private fun checkReference(reference: DesignatorReference, holder: ProblemsHolder) {
        val refName = reference.referenceName ?: return
        val file = reference.containingFile

        val exists = AspectJReferenceSupport.findPointcutDeclaration(file, refName) != null ||
            AspectJReferenceSupport.findPointcutDeclarationInProject(file.project, refName) != null ||
            PsiTreeUtil.findChildrenOfType(file, PointcutDeclaration::class.java).any { it.getPointcutName() == refName }

        if (!exists) {
            holder.registerProblem(
                reference,
                "Pointcut '$refName' is not defined",
                com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR,
                CreatePointcutQuickFix(refName)
            )
        }
    }

    private fun checkPointcutUsage(pointcut: PointcutDeclaration, holder: ProblemsHolder) {
        val name = pointcut.getPointcutName() ?: return

        val references = ReferencesSearch.search(pointcut, GlobalSearchScope.projectScope(pointcut.project)).count()

        // Warn if unused
        if (references == 0) {
            holder.registerProblem(
                pointcut.nameIdentifier ?: pointcut,
                "Pointcut '$name' is never used",
                com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING
            )
        }
    }
}



