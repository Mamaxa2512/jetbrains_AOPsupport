package org.example.aop

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class AopPlugin : ProjectActivity {
    override suspend fun execute(project: Project) {
        // AOP Support plugin initialized — future phases register services here
    }
}
