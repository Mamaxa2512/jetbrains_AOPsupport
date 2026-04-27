package org.example.aop.aspectj

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiDocumentManager
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.usageView.UsageInfo
import org.example.aop.aspectj.psi.PointcutDeclaration
import com.intellij.openapi.command.WriteCommandAction

/**
 * Handles Rename refactoring (Shift+F6) for AspectJ pointcuts using text replacement.
 *
 * Renames:
 * - Pointcut declaration name
 * - All designator references using this pointcut
 */
class AspectJSimpleRenameProcessor : RenamePsiElementProcessor() {

    override fun canProcessElement(element: PsiElement): Boolean {
        return element is PointcutDeclaration
    }

    override fun renameElement(
        element: PsiElement,
        newName: String,
        usages: Array<UsageInfo>,
        listener: com.intellij.refactoring.listeners.RefactoringElementListener?
    ) {
        val pointcut = element as? PointcutDeclaration ?: return
        pointcut.setName(newName)

        val psiDocumentManager = PsiDocumentManager.getInstance(pointcut.project)
        WriteCommandAction.runWriteCommandAction(pointcut.project) {
            usages.asSequence()
                .mapNotNull { it.element }
                .filter { it != pointcut }
                .forEach { usageElement ->
                    val document = psiDocumentManager.getDocument(usageElement.containingFile) ?: return@forEach
                    document.replaceString(
                        usageElement.textRange.startOffset,
                        usageElement.textRange.endOffset,
                        newName
                    )
                    psiDocumentManager.commitDocument(document)
                }
        }

        listener?.elementRenamed(element)
    }
}


