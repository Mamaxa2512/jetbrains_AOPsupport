package org.example.aop.aspectj

import com.intellij.find.findUsages.FindUsagesHandler
import com.intellij.find.findUsages.FindUsagesHandlerFactory
import com.intellij.find.findUsages.FindUsagesOptions
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.usageView.UsageInfo
import com.intellij.util.Processor
import org.example.aop.aspectj.psi.PointcutDeclaration
import org.example.aop.aspectj.psi.DesignatorReference

/**
 * Handles "Find Usages" (Ctrl+Alt+F7) for pointcut declarations.
 *
 * Shows all places where a pointcut is referenced:
 * - In other pointcut expressions
 * - In advice declarations
 * - In designator references
 */
class AspectJFindUsagesHandler(pointcutDecl: PointcutDeclaration) : FindUsagesHandler(pointcutDecl) {

    private val pointcutName: String? = pointcutDecl.getPointcutName()

    override fun getPrimaryElements(): Array<PsiElement> {
        val element = psiElement as? PointcutDeclaration
        return if (element != null && element.nameIdentifier != null) {
            arrayOf(element.nameIdentifier!!)
        } else {
            arrayOf(psiElement)
        }
    }

    override fun getSecondaryElements(): Array<PsiElement> {
        // Secondary elements could include advice that uses this pointcut
        return emptyArray()
    }
}

/**
 * Factory for creating Find Usages handlers for AspectJ elements
 */
class AspectJFindUsagesHandlerFactory : FindUsagesHandlerFactory() {

    override fun canFindUsages(element: PsiElement): Boolean {
        return element is PointcutDeclaration
    }

    override fun createFindUsagesHandler(element: PsiElement, showDialogs: Boolean): FindUsagesHandler? {
        return if (element is PointcutDeclaration) {
            AspectJFindUsagesHandler(element)
        } else {
            null
        }
    }
}


