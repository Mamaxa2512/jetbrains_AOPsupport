package org.example.aop.ai.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.util.PsiTreeUtil
import kotlinx.coroutines.runBlocking
import org.example.aop.ai.AopAiService
import org.example.aop.ai.OptimizationResult
import org.example.aop.inspection.AopInspectionRules

/**
 * Intention для оптимізації pointcut виразу за допомогою AI
 */
class OptimizePointcutIntention : IntentionAction, PriorityAction {

    override fun getText(): String = "Optimize this pointcut with AI"

    override fun getFamilyName(): String = "AOP AI"

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        val element = file.findElementAt(editor.caretModel.offset) ?: return false
        val literal = PsiTreeUtil.getParentOfType(element, PsiLiteralExpression::class.java)
            ?: return false

        val value = literal.value as? String ?: return false
        return value.isNotBlank() && isLikelyPointcutExpression(value)
    }

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val element = file.findElementAt(editor.caretModel.offset) ?: return
        val literal = PsiTreeUtil.getParentOfType(element, PsiLiteralExpression::class.java)
            ?: return
        val pointcutExpression = literal.value as? String ?: return

        // Оптимізуємо pointcut в background task
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            "Optimizing Pointcut with AI...",
            true
        ) {
            private var result: OptimizationResult? = null

            override fun run(indicator: com.intellij.openapi.progress.ProgressIndicator) {
                indicator.text = "Asking AI to optimize pointcut..."
                indicator.isIndeterminate = true

                val aiService = AopAiService.getInstance(project)
                result = runBlocking {
                    aiService.optimizePointcut(pointcutExpression)
                }
            }

            override fun onSuccess() {
                val optimization = result ?: return

                // Якщо вираз вже оптимальний
                if (optimization.optimizedExpression == pointcutExpression) {
                    Messages.showInfoMessage(
                        project,
                        "This pointcut expression is already optimal.\n\n${optimization.reason}",
                        "Already Optimal"
                    )
                    return
                }

                // Показуємо діалог з пропозицією оптимізації
                val message = buildString {
                    appendLine("AI suggests optimizing this pointcut:")
                    appendLine()
                    appendLine("Current:")
                    appendLine(pointcutExpression)
                    appendLine()
                    appendLine("Optimized:")
                    appendLine(optimization.optimizedExpression)
                    appendLine()
                    appendLine("Reason:")
                    appendLine(optimization.reason)
                    appendLine()
                    appendLine("Performance Impact: ${optimization.impact}")
                    appendLine()
                    appendLine("Apply this optimization?")
                }

                val result = Messages.showYesNoDialog(
                    project,
                    message,
                    "Optimize Pointcut",
                    "Apply",
                    "Cancel",
                    Messages.getQuestionIcon()
                )

                if (result == Messages.YES) {
                    // Застосовуємо оптимізацію
                    WriteCommandAction.runWriteCommandAction(project) {
                        val newText = "\"${optimization.optimizedExpression}\""
                        val elementFactory = com.intellij.psi.JavaPsiFacade
                            .getElementFactory(project)
                        val newLiteral = elementFactory.createExpressionFromText(
                            newText,
                            literal.context
                        )
                        literal.replace(newLiteral)
                    }
                }
            }

            override fun onThrowable(error: Throwable) {
                Messages.showErrorDialog(
                    project,
                    "Failed to optimize pointcut: ${error.message}",
                    "AI Error"
                )
            }
        })
    }

    override fun startInWriteAction(): Boolean = false

    override fun getPriority(): PriorityAction.Priority = PriorityAction.Priority.NORMAL

    private fun isLikelyPointcutExpression(value: String): Boolean {
        return AopInspectionRules.supportedPointcutDesignators.any { 
            value.contains("$it(")
        }
    }
}
