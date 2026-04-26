package org.example.aop.ai.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PriorityAction
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
import org.example.aop.inspection.AopInspectionRules

/**
 * Intention для пояснення pointcut виразу за допомогою AI
 */
class ExplainPointcutIntention : IntentionAction, PriorityAction {

    override fun getText(): String = "Explain this pointcut with AI"

    override fun getFamilyName(): String = "AOP AI"

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        val element = file.findElementAt(editor.caretModel.offset) ?: return false
        val literal = PsiTreeUtil.getParentOfType(element, PsiLiteralExpression::class.java)
            ?: return false

        // Перевіряємо, чи це pointcut вираз
        val value = literal.value as? String ?: return false
        return value.isNotBlank() && isLikelyPointcutExpression(value)
    }

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val element = file.findElementAt(editor.caretModel.offset) ?: return
        val literal = PsiTreeUtil.getParentOfType(element, PsiLiteralExpression::class.java)
            ?: return
        val pointcutExpression = literal.value as? String ?: return

        // Пояснюємо pointcut в background task
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            "Explaining Pointcut with AI...",
            true
        ) {
            private var explanation: String? = null

            override fun run(indicator: com.intellij.openapi.progress.ProgressIndicator) {
                indicator.text = "Asking AI to explain pointcut..."
                indicator.isIndeterminate = true

                val aiService = AopAiService.getInstance(project)
                explanation = runBlocking {
                    aiService.explainPointcut(pointcutExpression)
                }
            }

            override fun onSuccess() {
                val text = explanation ?: return

                // Показуємо пояснення в діалозі
                Messages.showMessageDialog(
                    project,
                    text,
                    "Pointcut Explanation (AI)",
                    Messages.getInformationIcon()
                )
            }

            override fun onThrowable(error: Throwable) {
                Messages.showErrorDialog(
                    project,
                    "Failed to explain pointcut: ${error.message}",
                    "AI Error"
                )
            }
        })
    }

    override fun startInWriteAction(): Boolean = false

    override fun getPriority(): PriorityAction.Priority = PriorityAction.Priority.NORMAL

    private fun isLikelyPointcutExpression(value: String): Boolean {
        // Перевіряємо, чи містить вираз pointcut designators
        return AopInspectionRules.supportedPointcutDesignators.any { 
            value.contains("$it(")
        }
    }
}
