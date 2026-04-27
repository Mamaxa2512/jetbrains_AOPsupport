package org.example.aop.ai.intentions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiFile
import kotlinx.coroutines.runBlocking
import org.example.aop.ai.AopAiService
import org.example.aop.ai.AopAiSnippetSupport

/**
 * Action для пояснення pointcut або AspectJ snippets за допомогою AI
 */
class ExplainPointcutIntention : AnAction() {

    init {
        templatePresentation.text = "Explain this pointcut with AI"
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val snippet = AopAiSnippetSupport.resolveSnippet(editor, file) ?: return
        val pointcutExpression = snippet.text

        // Пояснюємо pointcut в background task
        com.intellij.openapi.progress.ProgressManager.getInstance().run(object : Task.Backgroundable(
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

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val file = e.getData(CommonDataKeys.PSI_FILE)
        val available = editor != null && file != null && AopAiSnippetSupport.resolveSnippet(editor, file)?.let {
            AopAiSnippetSupport.looksLikeAopSnippet(it.text)
        } == true
        e.presentation.isEnabledAndVisible = available
    }
}
