package org.example.aop.inspection

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.JavaCodeStyleManager

class AspectNotBeanInspection : AbstractBaseJavaLocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : JavaElementVisitor() {
            override fun visitClass(aClass: PsiClass) {
                if (!aClass.hasAnnotation(AopInspectionRules.ASPECT_ANNOTATION)) return
                if (AopInspectionRules.isSpringBeanClass(aClass)) return

                val aspectAnnotation = aClass.getAnnotation(AopInspectionRules.ASPECT_ANNOTATION) ?: return

                holder.registerProblem(
                    aspectAnnotation,
                    "@Aspect class '${aClass.name}' is not a Spring Bean — Spring AOP will not apply it",
                    AddComponentFix
                )
            }
        }
    }

    object AddComponentFix : LocalQuickFix {
        override fun getName() = "Add @Component"
        override fun getFamilyName() = "AOP fixes"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val annotation = descriptor.psiElement as? PsiAnnotation ?: return
            val psiClass = annotation.parent?.parent as? PsiClass ?: return
            val modifierList = psiClass.modifierList ?: return

            if (AopInspectionRules.isSpringBeanClass(psiClass)) return

            val added = modifierList.addAnnotation("org.springframework.stereotype.Component")
            JavaCodeStyleManager.getInstance(project).shortenClassReferences(added)
        }
    }
}
