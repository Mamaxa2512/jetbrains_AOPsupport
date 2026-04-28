package org.example.aop.aspectj

import com.intellij.lang.LanguageParserDefinitions
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class AspectJInspectionFixtureTest : BasePlatformTestCase() {

    private var parserDefinition: AspectJParserDefinition? = null

    override fun setUp() {
        super.setUp()
        parserDefinition = AspectJParserDefinition().also { definition ->
            LanguageParserDefinitions.INSTANCE.addExplicitExtension(AspectJLanguage, definition)
            Disposer.register(testRootDisposable) {
                LanguageParserDefinitions.INSTANCE.removeExplicitExtension(AspectJLanguage, definition)
            }
        }
        val fileTypeManager = FileTypeManager.getInstance()
        val alreadyRegistered = fileTypeManager.getAssociations(AspectJFileType)
            .any { matcher -> matcher.acceptsCharSequence("Sample.aj") }
        if (!alreadyRegistered) {
            ApplicationManager.getApplication().runWriteAction {
                fileTypeManager.associateExtension(AspectJFileType, "aj")
            }
        }
    }

    fun testDeclareParentsRequiresResolvableTypes() {
        myFixture.addFileToProject(
            "src/com/example/Service.java",
            "package com.example; public class Service {}"
        )
        myFixture.enableInspections(AspectJInspection())
        myFixture.configureByText(
            AspectJFileType,
            """
                aspect ParentsAspect {
                    declare parents : com.example.Service implements <warning descr="Type 'com.example.MissingContract' cannot be resolved">com.example.MissingContract</warning>;
                }
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, false)
    }

    fun testDeclareWarningRequiresNonEmptyMessage() {
        myFixture.enableInspections(AspectJInspection())
        myFixture.configureByText(
            AspectJFileType,
            """
                aspect WarningAspect {
                    declare warning : execution(* *(..)) : <warning descr="declare warning message cannot be empty">""</warning>;
                }
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, false)
    }

    fun testInterTypeDeclarationRequiresResolvableTargetType() {
        myFixture.enableInspections(AspectJInspection())
        myFixture.configureByText(
            AspectJFileType,
            """
                aspect ItdAspect {
                    public void <warning descr="Inter-type target type 'com.example.MissingService' cannot be resolved">com.example.MissingService</warning>.audit() { }
                }
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, false, false)
    }
}
