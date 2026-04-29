package org.example.aop.aspectj

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import javax.swing.Icon

class CreateAspectJFileAction : CreateFileFromTemplateAction(
    "AspectJ File",
    "Create a new AspectJ file",
    AspectJFileType.icon
) {
    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder.setTitle("New AspectJ File")
            .addKind("Aspect", AspectJFileType.icon, "AspectJ Aspect")
            .addKind("Empty file", AspectJFileType.icon, "AspectJ Empty")
    }

    override fun createFile(name: String, templateName: String, dir: PsiDirectory): PsiFile? {
        val fileName = if (name.endsWith(".aj")) name else "$name.aj"
        val cleanName = name.removeSuffix(".aj")

        val content = when (templateName) {
            "AspectJ Aspect" -> buildAspectTemplate(cleanName, dir)
            else -> ""
        }

        val file = PsiFileFactory.getInstance(dir.project)
            .createFileFromText(fileName, AspectJFileType, content)
        return dir.add(file) as? PsiFile
    }

    private fun buildAspectTemplate(name: String, dir: PsiDirectory): String {
        val packageName = guessPackageName(dir)
        val sb = StringBuilder()
        if (packageName.isNotEmpty()) {
            sb.appendLine("package $packageName;")
            sb.appendLine()
        }
        sb.appendLine("public aspect $name {")
        sb.appendLine()
        sb.appendLine("}")
        return sb.toString()
    }

    private fun guessPackageName(dir: PsiDirectory): String {
        // Try to infer package name from directory structure
        val parts = mutableListOf<String>()
        var current: PsiDirectory? = dir
        while (current != null) {
            val name = current.name
            if (name == "java" || name == "kotlin" || name == "src" ||
                name == "main" || name == "test" || name == "resources") {
                break
            }
            parts.add(0, name)
            current = current.parentDirectory
        }
        return parts.joinToString(".")
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String {
        return "Create AspectJ File $newName"
    }
}
