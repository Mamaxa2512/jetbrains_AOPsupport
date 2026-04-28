package org.example.aop.aspectj

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import org.example.aop.aspectj.psi.DeclareErrorDeclaration
import org.example.aop.aspectj.psi.DeclareParentsDeclaration
import org.example.aop.aspectj.psi.DeclareStatement
import org.example.aop.aspectj.psi.DeclareWarningDeclaration
import org.example.aop.aspectj.psi.DesignatorReference
import org.example.aop.aspectj.psi.InterTypeDeclaration
import org.example.aop.aspectj.psi.PointcutDeclaration
import org.example.aop.aspectj.psi.TypeReferenceElement

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
                    is DeclareParentsDeclaration -> checkDeclareParents(element, holder)
                    is DeclareWarningDeclaration -> checkDeclareMessage(element, holder)
                    is DeclareErrorDeclaration -> checkDeclareMessage(element, holder)
                    is InterTypeDeclaration -> checkInterTypeDeclaration(element, holder)
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
                ProblemHighlightType.GENERIC_ERROR,
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
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
            )
        }
    }

    private fun checkDeclareParents(declaration: DeclareParentsDeclaration, holder: ProblemsHolder) {
        val typeReferences = declaration.getTypeReferences()
        if (typeReferences.size < 2) {
            holder.registerProblem(
                declaration,
                "declare parents must specify a target type and at least one introduced parent type",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
            )
            return
        }

        typeReferences.forEach { typeReference ->
            checkResolvedType(typeReference, holder, "Type '${typeReference.text}' cannot be resolved")
        }
    }

    private fun checkDeclareMessage(declaration: DeclareStatement, holder: ProblemsHolder) {
        val message = declaration.getMessage()
        if (message == null) {
            holder.registerProblem(
                declaration,
                "${AspectJLineMarkerContext.declareTooltip(declaration)} must define a message string",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
            )
            return
        }

        val unquoted = message.text.trim().removePrefix("\"").removeSuffix("\"").trim()
        if (unquoted.isBlank()) {
            holder.registerProblem(
                message,
                "${AspectJLineMarkerContext.declareTooltip(declaration)} message cannot be empty",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
            )
        }
    }

    private fun checkInterTypeDeclaration(declaration: InterTypeDeclaration, holder: ProblemsHolder) {
        declaration.getTargetTypeReferences()
            .filter { reference -> AspectJIndexSupport.normalizeQualifiedTypeName(reference.text) != null }
            .forEach { reference ->
                checkResolvedType(reference, holder, "Inter-type target type '${reference.text}' cannot be resolved")
            }
    }

    private fun checkResolvedType(reference: TypeReferenceElement, holder: ProblemsHolder, message: String) {
        if (AspectJReferenceSupport.resolveTypeReference(reference) == null) {
            holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
        }
    }
}


