package org.example.aop.ai.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import kotlinx.coroutines.runBlocking
import org.example.aop.ai.AopAiService
import org.example.aop.ai.AopAiSnippetSupport

/**
 * Action for fixing a selected pointcut/advice snippet with AI.
 *
 * Works in both Java/Kotlin string literals and native AspectJ `.aj` files when
 * the user selects a problematic snippet.
 */
class FixPointcutAction : AnAction() {

    init {
        templatePresentation.text = "Fix with AI"
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return

        val snippet = AopAiSnippetSupport.resolveSnippet(editor, psiFile) ?: run {
            Messages.showInfoMessage(
                project,
                "Select a pointcut or advice snippet first.",
                "Fix with AI"
            )
            return
        }

        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            "Fixing snippet with AI...",
            true
        ) {
            private var fixedExpression: String? = null

            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Asking AI to fix snippet..."
                indicator.isIndeterminate = true

                val aiService = AopAiService.getInstance(project)
                fixedExpression = runBlocking {
                    aiService.fixPointcut(snippet.text, "Selected AspectJ/AOP snippet")
                }
            }

            override fun onSuccess() {
                val fixed = fixedExpression ?: return
                val replacement = if (snippet.shouldWrapInQuotes) "\"$fixed\"" else fixed

                val message = buildString {
                    appendLine("AI suggests fixing the selected snippet:")
                    appendLine()
                    appendLine("Current:")
                    appendLine(snippet.text)
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

                if (result != Messages.YES) return

                WriteCommandAction.runWriteCommandAction(project) {
                    when {
                        snippet.literal != null -> {
                            val elementFactory = com.intellij.psi.JavaPsiFacade.getElementFactory(project)
                            val newLiteral = elementFactory.createExpressionFromText(
                                replacement,
                                snippet.literal.context
                            )
                            snippet.literal.replace(newLiteral)
                        }
                        snippet.selectionStart != null && snippet.selectionEnd != null -> {
                            editor.document.replaceString(
                                snippet.selectionStart,
                                snippet.selectionEnd,
                                replacement
                            )
                        }
                    }
                }
            }

            override fun onThrowable(error: Throwable) {
                Messages.showErrorDialog(
                    project,
                    "Failed to fix snippet: ${error.message}",
                    "AI Error"
                )
            }
        })
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        val file = e.getData(CommonDataKeys.PSI_FILE)
        e.presentation.isEnabledAndVisible = project != null && editor != null && file != null
    }
}


