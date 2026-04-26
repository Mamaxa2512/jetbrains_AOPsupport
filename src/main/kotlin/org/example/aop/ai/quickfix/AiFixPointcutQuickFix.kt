package org.example.aop.ai.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiLiteralExpression
import kotlinx.coroutines.runBlocking
import org.example.aop.ai.AopAiService

/**
 * Quick Fix який використовує AI для виправлення помилок в pointcut виразах
 */
class AiFixPointcutQuickFix(
    private val errorMessage: String
) : LocalQuickFix {

    override fun getFamilyName(): String = "Fix with AI"

    override fun getName(): String = "Fix this pointcut with AI"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement as? PsiLiteralExpression ?: return
        val invalidExpression = element.value as? String ?: return

        // Виправляємо pointcut в background task
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            "Fixing Pointcut with AI...",
            true
        ) {
            private var fixedExpression: String? = null

            override fun run(indicator: com.intellij.openapi.progress.ProgressIndicator) {
                indicator.text = "Asking AI to fix pointcut..."
                indicator.isIndeterminate = true

                val aiService = AopAiService.getInstance(project)
                fixedExpression = runBlocking {
                    aiService.fixPointcut(invalidExpression, errorMessage)
                }
            }

            override fun onSuccess() {
                val fixed = fixedExpression ?: return

                // Показуємо діалог з пропозицією виправлення
                val message = buildString {
                    appendLine("AI suggests fixing this pointcut:")
                    appendLine()
                    appendLine("Current (invalid):")
                    appendLine(invalidExpression)
                    appendLine()
                    appendLine("Error:")
                    appendLine(errorMessage)
                    appendLine()
                    appendLine("Suggested fix:")
                    appendLine(fixed)
                    appendLine()
                    appendLine("Apply this fix?")
                }

                val result = Messages.showYesNoDialog(
                    project,
                    message,
                    "AI Fix Suggestion",
                    "Apply",
                    "Cancel",
                    Messages.getQuestionIcon()
                )

                if (result == Messages.YES) {
                    // Застосовуємо виправлення
                    com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(project) {
                        val newText = "\"$fixed\""
                        val elementFactory = JavaPsiFacade.getElementFactory(project)
                        val newLiteral = elementFactory.createExpressionFromText(
                            newText,
                            element.context
                        )
                        element.replace(newLiteral)
                    }
                }
            }

            override fun onThrowable(error: Throwable) {
                Messages.showErrorDialog(
                    project,
                    "Failed to fix pointcut: ${error.message}",
                    "AI Error"
                )
            }
        })
    }

    override fun startInWriteAction(): Boolean = false
}
