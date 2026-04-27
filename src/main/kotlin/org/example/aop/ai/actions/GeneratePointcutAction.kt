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
 * Action для генерації pointcut виразу з текстового опису за допомогою AI
 */
class GeneratePointcutAction : AnAction() {

    init {
        templatePresentation.text = "Generate Pointcut with AI"
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val fileName = e.getData(CommonDataKeys.PSI_FILE)?.name

        // Показуємо діалог для введення опису
        val description = Messages.showInputDialog(
            project,
            "Describe what methods you want to intercept:\n\n" +
                    "Examples:\n" +
                    "- All public methods in service package\n" +
                    "- Methods annotated with @Transactional\n" +
                    "- All methods in UserService class",
            "Generate Pointcut with AI",
            Messages.getQuestionIcon()
        ) ?: return

        if (description.isBlank()) {
            Messages.showWarningDialog(
                project,
                "Please provide a description",
                "Empty Description"
            )
            return
        }

        // Генеруємо pointcut в background task
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            "Generating Pointcut with AI...",
            true
        ) {
            private var generatedPointcut: String? = null

            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Asking AI to generate pointcut..."
                indicator.isIndeterminate = true

                val aiService = AopAiService.getInstance(project)
                generatedPointcut = runBlocking {
                    aiService.generatePointcut(description)
                }
            }

            override fun onSuccess() {
                val pointcut = generatedPointcut ?: return

                // Вставляємо згенерований pointcut в редактор
                WriteCommandAction.runWriteCommandAction(project) {
                    val document = editor.document
                    val insertion = AopAiSnippetSupport.formatGeneratedSnippet(pointcut, fileName)
                    if (editor.selectionModel.hasSelection()) {
                        document.replaceString(
                            editor.selectionModel.selectionStart,
                            editor.selectionModel.selectionEnd,
                            insertion
                        )
                        editor.selectionModel.removeSelection()
                    } else {
                        val offset = editor.caretModel.offset
                        document.insertString(offset, insertion)
                        editor.caretModel.moveToOffset(offset + insertion.length)
                    }
                }

                // Показуємо notification
                Messages.showInfoMessage(
                    project,
                    "Generated pointcut:\n$pointcut",
                    "AI Generated Pointcut"
                )
            }

            override fun onThrowable(error: Throwable) {
                Messages.showErrorDialog(
                    project,
                    "Failed to generate pointcut: ${error.message}",
                    "AI Error"
                )
            }
        })
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = project != null && editor != null
    }
}
