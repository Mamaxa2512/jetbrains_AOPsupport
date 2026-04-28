package org.example.aop

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import org.example.aop.aspectj.AspectJFileType

class AopPlugin : ProjectActivity {
    override suspend fun execute(project: Project) {
        val fileTypeManager = FileTypeManager.getInstance()
        val alreadyRegistered = fileTypeManager.getAssociations(AspectJFileType)
            .any { matcher -> matcher.acceptsCharSequence("Sample.aj") }

        if (!alreadyRegistered) {
            ApplicationManager.getApplication().runWriteAction {
                fileTypeManager.associateExtension(AspectJFileType, "aj")
            }
        }
    }
}
