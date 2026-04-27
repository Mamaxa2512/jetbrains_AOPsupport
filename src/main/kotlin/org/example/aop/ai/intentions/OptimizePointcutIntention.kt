package org.example.aop.ai.intentions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiFile
import kotlinx.coroutines.runBlocking
import org.example.aop.ai.AopAiService
import org.example.aop.ai.AopAiSnippetSupport
import org.example.aop.ai.PerformanceAnalysis
import org.example.aop.ai.OptimizationResult

/**
 * Action для оптимізації pointcut або AspectJ snippets за допомогою AI
 */
class OptimizePointcutIntention : AnAction() {

    init {
        templatePresentation.text = "Optimize this pointcut with AI"
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val snippet = AopAiSnippetSupport.resolveSnippet(editor, file) ?: return
        val pointcutExpression = snippet.text

        // Оптимізуємо pointcut в background task
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            "Optimizing Pointcut with AI...",
            true
        ) {
            private var result: OptimizationResult? = null
            private var performance: PerformanceAnalysis? = null

            override fun run(indicator: com.intellij.openapi.progress.ProgressIndicator) {
                indicator.text = "Asking AI to optimize pointcut..."
                indicator.isIndeterminate = true

                val aiService = AopAiService.getInstance(project)
                performance = runBlocking {
                    aiService.analyzePerformance(pointcutExpression)
                }
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
                    performance?.let {
                        appendLine("Performance score: ${it.score}/10")
                        appendLine("Estimated matches: ${it.estimatedMatches}")
                        if (it.issues.isNotEmpty()) {
                            appendLine("Issues: ${it.issues.joinToString()}")
                        }
                        appendLine()
                    }
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
                        if (snippet.literal != null) {
                            val newText = "\"${optimization.optimizedExpression}\""
                            val elementFactory = com.intellij.psi.JavaPsiFacade
                                .getElementFactory(project)
                            val newLiteral = elementFactory.createExpressionFromText(
                                newText,
                                snippet.literal.context
                            )
                            snippet.literal.replace(newLiteral)
                        } else if (snippet.selectionStart != null && snippet.selectionEnd != null) {
                            editor.document.replaceString(
                                snippet.selectionStart,
                                snippet.selectionEnd,
                                optimization.optimizedExpression
                            )
                        }
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

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val file = e.getData(CommonDataKeys.PSI_FILE)
        val available = editor != null && file != null && AopAiSnippetSupport.resolveSnippet(editor, file)?.let {
            AopAiSnippetSupport.looksLikeAopSnippet(it.text)
        } == true
        e.presentation.isEnabledAndVisible = available
    }
}
