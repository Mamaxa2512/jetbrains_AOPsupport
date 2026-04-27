package org.example.aop.aspectj

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.MockProblemDescriptor

class AspectJQuickFixTest : BasePlatformTestCase() {

    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    fun testCreatePointcutQuickFix() {
        val code = """
            aspect QFAspect {
                before() : missingPc() { }
            }
        """.trimIndent()

        val psiFile = myFixture.addFileToProject("QuickFix.aj", code)
        myFixture.configureFromExistingVirtualFile(psiFile.virtualFile)

        // The inspection may not run reliably in the lightweight fixture for our custom language.
        // Instead, directly exercise the quick-fix implementation: create a ProblemDescriptor on the file
        // and call applyFix. This verifies the quick-fix behavior (inserting a new pointcut).
        val fix = CreatePointcutQuickFix("missingPc")
        val descriptor = MockProblemDescriptor(
            psiFile,
            "Pointcut 'missingPc' is not defined",
            ProblemHighlightType.GENERIC_ERROR,
            fix
        )

        // Apply the quick fix
        fix.applyFix(project, descriptor)

        // After applying quick-fix, file should contain pointcut declaration
        val updated = myFixture.file.text
        assertTrue("File should contain new pointcut declaration", updated.contains("pointcut missingPc()"))
    }
}







