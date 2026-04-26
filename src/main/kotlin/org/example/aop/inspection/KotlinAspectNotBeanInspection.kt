package org.example.aop.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.idea.base.codeInsight.ShortenReferencesFacility
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

class KotlinAspectNotBeanInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file as? KtFile ?: return PsiElementVisitor.EMPTY_VISITOR
        return object : KtTreeVisitorVoid() {
            override fun visitClass(klass: KtClass) {
                super.visitClass(klass)
                if (!klass.hasAspectAnnotation()) return
                if (klass.hasSpringBeanAnnotation()) return

                val aspectAnnotation = klass.annotationEntries.firstOrNull {
                    it.isQualifiedOrShort(AopInspectionRules.ASPECT_ANNOTATION)
                } ?: return

                holder.registerProblem(
                    aspectAnnotation,
                    "@Aspect class '${klass.name}' is not a Spring Bean — Spring AOP will not apply it",
                    AddComponentFix
                )
            }
        }
    }

    object AddComponentFix : LocalQuickFix {
        override fun getName(): String = "Add @Component"
        override fun getFamilyName(): String = "AOP fixes"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val annotationEntry = descriptor.psiElement as? org.jetbrains.kotlin.psi.KtAnnotationEntry ?: return
            val klass = annotationEntry.parent?.parent as? KtClass ?: return
            if (klass.hasSpringBeanAnnotation()) return

            val psiFactory = KtPsiFactory(project)
            val componentAnnotation = psiFactory.createAnnotationEntry("@org.springframework.stereotype.Component")
            val inserted = klass.addBefore(componentAnnotation, klass.firstChild)
            klass.addAfter(psiFactory.createNewLine(), inserted)
            ShortenReferencesFacility.getInstance().shorten(inserted as org.jetbrains.kotlin.psi.KtElement)
        }
    }
}

private fun KtClass.hasAspectAnnotation(): Boolean {
    return annotationEntries.any { it.isQualifiedOrShort(AopInspectionRules.ASPECT_ANNOTATION) }
}

private fun KtClass.hasSpringBeanAnnotation(): Boolean {
    return annotationEntries.any { entry ->
        AopInspectionRules.springBeanAnnotations.any { spring ->
            entry.isQualifiedOrShort(spring)
        }
    }
}

private fun org.jetbrains.kotlin.psi.KtAnnotationEntry.isQualifiedOrShort(qualifiedName: String): Boolean {
    val shortName = qualifiedName.substringAfterLast('.')
    val text = typeReference?.text ?: return false
    return text == qualifiedName || text.endsWith(".$shortName") || text == shortName
}
