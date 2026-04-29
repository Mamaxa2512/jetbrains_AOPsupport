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
import org.example.aop.aspectj.psi.DeclarePrecedenceDeclaration
import org.example.aop.aspectj.psi.DeclareStatement
import org.example.aop.aspectj.psi.DeclareWarningDeclaration
import org.example.aop.aspectj.psi.DesignatorReference
import org.example.aop.aspectj.psi.InterTypeDeclaration
import org.example.aop.aspectj.psi.PointcutDeclaration
import org.example.aop.aspectj.psi.TypeReferenceElement
import org.example.aop.aspectj.psi.AdviceDeclaration

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
                    is DeclarePrecedenceDeclaration -> checkDeclarePrecedence(element, holder)
                    is DeclareWarningDeclaration -> checkDeclareMessage(element, holder)
                    is DeclareErrorDeclaration -> checkDeclareMessage(element, holder)
                    is InterTypeDeclaration -> checkInterTypeDeclaration(element, holder)
                    is AdviceDeclaration -> checkAdvice(element, holder)
                    is org.example.aop.aspectj.psi.Designator -> checkDesignator(element, holder)
                }
                super.visitElement(element)
            }
        }
    }

    private fun checkAdvice(advice: AdviceDeclaration, holder: ProblemsHolder) {
        val type = advice.getAdviceType() ?: return
        val text = advice.text
        if (type == "after returning" && !text.contains("returning(")) {
            holder.registerProblem(
                advice.firstChild ?: advice,
                "'after returning' advice should specify a returning parameter clause",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                AddReturningClauseQuickFix("returning", "result")
            )
        }
        if (type == "after throwing" && !text.contains("throwing(")) {
            holder.registerProblem(
                advice.firstChild ?: advice,
                "'after throwing' advice should specify a throwing parameter clause",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                AddReturningClauseQuickFix("throwing", "ex")
            )
        }
    }

    private fun checkDesignator(designator: org.example.aop.aspectj.psi.Designator, holder: ProblemsHolder) {
        val type = designator.getDesignatorType() ?: return
        if (type.startsWith("@")) {
            val content = designator.getDesignatorContent() ?: return
            val cleanContent = content.trim().removeSurrounding("(").removeSurrounding(")").trim()
            if (cleanContent.isNotBlank() && !cleanContent.contains("*") && !cleanContent.contains("..")) {
                // check if annotation resolves
                val javaClass = com.intellij.psi.JavaPsiFacade.getInstance(designator.project)
                    .findClass(cleanContent, GlobalSearchScope.allScope(designator.project))
                
                if (javaClass == null) {
                    holder.registerProblem(
                        designator,
                        "Annotation type '$cleanContent' cannot be resolved",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                    )
                }
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

        if (pointcut.getModifier() == null) {
            holder.registerProblem(
                pointcut.firstChild ?: pointcut,
                "Pointcut declaration is missing a visibility modifier",
                ProblemHighlightType.INFORMATION,
                AddModifierQuickFix("public"),
                AddModifierQuickFix("private")
            )
        }

        // Check for usages via DesignatorReference within the same file
        val file = pointcut.containingFile
        val designatorUsages = PsiTreeUtil.findChildrenOfType(file, DesignatorReference::class.java)
            .any { it.referenceName == name }

        if (designatorUsages) return // pointcut IS used in advice

        // Check for usages via standard reference search (cross-file)
        val references = ReferencesSearch.search(pointcut, GlobalSearchScope.projectScope(pointcut.project)).count()

        if (references == 0) {
            // Also check cross-file DesignatorReference usages
            val crossFileUsed = AspectJReferenceSupport.findPointcutDeclarationInProject(pointcut.project, name) != null

            if (!crossFileUsed) {
                holder.registerProblem(
                    pointcut.nameIdentifier ?: pointcut,
                    "Pointcut '$name' is never used",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                )
            }
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

    private fun checkDeclarePrecedence(declaration: DeclarePrecedenceDeclaration, holder: ProblemsHolder) {
        val typeReferences = declaration.getTypeReferences()
        if (typeReferences.size < 2) {
            holder.registerProblem(
                declaration,
                "declare precedence must define at least two type patterns",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
            )
            return
        }

        val seen = mutableSetOf<String>()
        typeReferences.forEach { reference ->
            val normalized = reference.text.trim()
            if (!seen.add(normalized)) {
                holder.registerProblem(
                    reference,
                    "Duplicate precedence entry '$normalized'",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                )
            }
        }
    }

    private fun checkInterTypeDeclaration(declaration: InterTypeDeclaration, holder: ProblemsHolder) {
        val targetReferences = declaration.getTargetTypeReferences()
        if (targetReferences.isEmpty()) {
            holder.registerProblem(
                declaration,
                "Inter-type declaration must specify a target type",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
            )
            return
        }

        if (declaration.getDeclaredMemberName().isNullOrBlank()) {
            holder.registerProblem(
                declaration,
                "Inter-type declaration must declare a target member name",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
            )
        }

        val memberName = declaration.getDeclaredMemberName()
        if (memberName != null && !memberName.matches(Regex("[A-Za-z_$][A-Za-z0-9_$]*"))) {
            holder.registerProblem(
                declaration,
                "Inter-type declaration has an invalid member name '$memberName'",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
            )
        }

        if (declaration.isMethodLike() && declaration.getReturnTypeReference() == null) {
            holder.registerProblem(
                declaration,
                "Inter-type method declaration must declare a return type",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
            )
        }

        targetReferences
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
