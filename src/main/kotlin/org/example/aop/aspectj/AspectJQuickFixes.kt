package org.example.aop.aspectj

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.openapi.command.WriteCommandAction
import org.example.aop.aspectj.psi.PointcutDeclaration

class CreatePointcutQuickFix(private val name: String) : LocalQuickFix {
    override fun getName(): String = "Create pointcut '$name'"
    override fun getFamilyName(): String = "AspectJ quick fixes"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement ?: return
        val file = element.containingFile ?: return

        // Create a new pointcut declaration stub and append it to the file using the document API.
        val text = "\npointcut $name() : ;\n"
        WriteCommandAction.runWriteCommandAction(project) {
            val document = com.intellij.psi.PsiDocumentManager.getInstance(project).getDocument(file)
            if (document != null) {
                document.insertString(document.textLength, text)
                com.intellij.psi.PsiDocumentManager.getInstance(project).commitDocument(document)
            } else {
                // Fallback to PSI-based insertion if document is not available
                val factory = PsiFileFactory.getInstance(project)
                val dummy = factory.createFileFromText("__tmp.aj", AspectJFileType, text)
                val decl = PsiTreeUtil.findChildOfType(dummy, PointcutDeclaration::class.java)
                if (decl != null) {
                    file.addAfter(decl, file.lastChild)
                }
            }
        }
    }
}
class AddModifierQuickFix(private val modifier: String) : LocalQuickFix {
    override fun getName(): String = "Add '$modifier' modifier"
    override fun getFamilyName(): String = "AspectJ quick fixes"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement ?: return
        WriteCommandAction.runWriteCommandAction(project) {
            val document = com.intellij.psi.PsiDocumentManager.getInstance(project).getDocument(element.containingFile)
            if (document != null) {
                document.insertString(element.textRange.startOffset, "$modifier ")
                com.intellij.psi.PsiDocumentManager.getInstance(project).commitDocument(document)
            }
        }
    }
}

class AddReturningClauseQuickFix(private val clauseName: String, private val defaultParam: String) : LocalQuickFix {
    override fun getName(): String = "Add '$clauseName($defaultParam)' clause"
    override fun getFamilyName(): String = "AspectJ quick fixes"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement ?: return
        WriteCommandAction.runWriteCommandAction(project) {
            val document = com.intellij.psi.PsiDocumentManager.getInstance(project).getDocument(element.containingFile)
            if (document != null) {
                // insert it after the advice parameters or pointcut expression. 
                // A simple approach is to append to the end of the element before the block '{' or ';'
                // Since this might be tricky, we'll append it just before the `{` or `;` if we can find it.
                val text = element.text
                val blockIndex = text.indexOf('{')
                val semicolonIndex = text.indexOf(';')
                val insertIndex = if (blockIndex != -1) blockIndex else if (semicolonIndex != -1) semicolonIndex else text.length
                
                val insertOffset = element.textRange.startOffset + insertIndex
                document.insertString(insertOffset, " $clauseName($defaultParam) ")
                com.intellij.psi.PsiDocumentManager.getInstance(project).commitDocument(document)
            }
        }
    }
}

