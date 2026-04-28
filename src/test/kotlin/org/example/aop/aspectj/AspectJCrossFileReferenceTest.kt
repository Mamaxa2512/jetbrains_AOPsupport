package org.example.aop.aspectj

import com.intellij.lang.LanguageParserDefinitions
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.psi.util.PsiTreeUtil
import org.example.aop.aspectj.psi.AspectJPointcutReference
import org.example.aop.aspectj.psi.DesignatorReference

class AspectJCrossFileReferenceTest : BasePlatformTestCase() {

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

	fun testPointcutReferenceResolvesAcrossFiles() {
		val declaration = myFixture.addFileToProject(
			"lib/Shared.aj",
			"""
				aspect SharedAspect {
					pointcut sharedPointcut() : execution(* shared(..));
				}
			""".trimIndent()
		)

		myFixture.configureByText(
			AspectJFileType,
			"""
				aspect UseAspect {
					before() : sharedPointcut() { }
				}
			""".trimIndent()
		)

		val designator = PsiTreeUtil.findChildrenOfType(myFixture.file, DesignatorReference::class.java)
			.firstOrNull { it.referenceName == "sharedPointcut" }

		assertNotNull("Expected a designator reference at the pointcut usage", designator)
		val reference = AspectJPointcutReference(designator!!, designator.referenceName!!)
		val resolved = reference.resolve()

		assertNotNull("Expected the reference to resolve to the declaration in another file", resolved)
		assertEquals("Shared.aj", resolved!!.containingFile.name)
		assertEquals(declaration.virtualFile, resolved.containingFile.virtualFile)
	}

	fun testCrossFileUsageDoesNotLookUndefined() {
		myFixture.addFileToProject(
			"lib/Shared.aj",
			"""
				aspect SharedAspect {
					pointcut sharedPointcut() : execution(* shared(..));
				}
			""".trimIndent()
		)

		myFixture.configureByText(
			AspectJFileType,
			"""
				aspect UseAspect {
					before() : sharedPointcut() { }
				}
			""".trimIndent()
		)

		myFixture.enableInspections(AspectJInspection())

		val highlightInfos = myFixture.doHighlighting()
		assertTrue(
			"Cross-file pointcut usage should not be flagged as undefined",
			highlightInfos.none { info -> info.description?.contains("not defined", ignoreCase = true) == true }
		)
	}
}
