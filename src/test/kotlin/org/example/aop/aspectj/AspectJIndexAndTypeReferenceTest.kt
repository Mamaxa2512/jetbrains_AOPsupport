package org.example.aop.aspectj

import com.intellij.lang.LanguageParserDefinitions
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.example.aop.aspectj.psi.TypeReferenceElement

class AspectJIndexAndTypeReferenceTest : BasePlatformTestCase() {

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

    fun testCollectsIndexKeysFromPsi() {
        val file = myFixture.configureByText(
            "IndexedAspect.aj",
            """
                privileged aspect IndexedAspect pertarget(execution(* com.example.Service.*(..))) {
                    pointcut internal() : execution(* *(..));
                    declare warning : execution(* com.example..*(..)) : "warn";
                    public void com.example.Service.audit() { }
                }
            """.trimIndent()
        )

        assertContainsElements(AspectJIndexSupport.aspectNames(file), "IndexedAspect")
        assertContainsElements(AspectJIndexSupport.pointcutNames(file), "internal")
        assertContainsElements(AspectJIndexSupport.declareKeys(file), "warning")
        assertContainsElements(AspectJIndexSupport.interTypeTargets(file), "com.example.Service")
    }

    fun testDeclareParentsTypeReferenceResolvesToJavaClass() {
        myFixture.addFileToProject(
            "src/com/example/Service.java",
            """
                package com.example;
                public class Service {}
            """.trimIndent()
        )
        myFixture.addFileToProject(
            "src/com/example/Auditable.java",
            """
                package com.example;
                public interface Auditable {}
            """.trimIndent()
        )

        myFixture.configureByText(
            "ParentsAspect.aj",
            """
                aspect ParentsAspect {
                    declare parents : com.example.Service implements com.example.Auditable;
                }
            """.trimIndent()
        )

        val references = PsiTreeUtil.findChildrenOfType(myFixture.file, TypeReferenceElement::class.java)
        val serviceRef = references.firstOrNull { it.text == "com.example.Service" }
        val auditableRef = references.firstOrNull { it.text == "com.example.Auditable" }

        assertNotNull(serviceRef)
        assertNotNull(auditableRef)
        assertEquals("Service", (AspectJTypeReference(serviceRef!!).resolve() as? PsiNamedElement)?.name)
        assertEquals("Auditable", (AspectJTypeReference(auditableRef!!).resolve() as? PsiNamedElement)?.name)
    }

    fun testInterTypeTargetReferenceResolvesToJavaClass() {
        myFixture.addFileToProject(
            "src/com/example/Service.java",
            """
                package com.example;
                public class Service {}
            """.trimIndent()
        )

        myFixture.configureByText(
            "ItdAspect.aj",
            """
                aspect ItdAspect {
                    public void com.example.Service.audit() { }
                    public int com.example.Service.retryCount;
                }
            """.trimIndent()
        )

        val resolvedClasses = PsiTreeUtil.findChildrenOfType(myFixture.file, TypeReferenceElement::class.java)
            .mapNotNull { AspectJTypeReference(it).resolve() }
            .mapNotNull { (it as? PsiNamedElement)?.name }

        assertContainsElements(resolvedClasses, "Service")
    }
}
