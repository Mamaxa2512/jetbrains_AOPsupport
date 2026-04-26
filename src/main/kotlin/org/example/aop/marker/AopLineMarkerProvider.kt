package org.example.aop.marker

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import org.example.aop.inspection.AopInspectionRules

class AopLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        when (element) {
            is PsiClass -> markAspectClass(element, result)
            is PsiMethod -> markAdviceMethod(element, result)
        }
    }

    private fun markAspectClass(
        psiClass: PsiClass,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        if (!psiClass.hasAnnotation(AopInspectionRules.ASPECT_ANNOTATION)) return
        val nameIdentifier = psiClass.nameIdentifier ?: return

        val adviceMethods = psiClass.methods
            .filter { method ->
                val methodAnnotations = method.modifierList.annotations
                    .mapNotNull { it.qualifiedName }
                    .toSet()
                AopLineMarkerContext.hasAdviceAnnotation(methodAnnotations)
            }
            .sortedBy { it.name }
        if (adviceMethods.isEmpty()) return

        result.add(
            NavigationGutterIconBuilder.create(AllIcons.Gutter.ImplementingMethod)
                .setTargets(adviceMethods)
                .setTooltipText(AopLineMarkerContext.classTooltip(adviceMethods.size))
                .createLineMarkerInfo(nameIdentifier)
        )
    }

    private fun markAdviceMethod(
        method: PsiMethod,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val methodAnnotations = method.modifierList.annotations
            .mapNotNull { it.qualifiedName }
            .toSet()
        val matchedAnnotation = AopLineMarkerContext
            .adviceAnnotationsInPreferredOrder(methodAnnotations)
            .firstOrNull() ?: return
        val containingClass = method.containingClass ?: return
        if (!containingClass.hasAnnotation(AopInspectionRules.ASPECT_ANNOTATION)) return
        val nameIdentifier = method.nameIdentifier ?: return

        result.add(
            NavigationGutterIconBuilder.create(AllIcons.Gutter.OverridingMethod)
                .setTargets(listOf(containingClass))
                .setTooltipText(AopLineMarkerContext.adviceMethodTooltip(matchedAnnotation, containingClass.name))
                .createLineMarkerInfo(nameIdentifier)
        )
    }
}
