package org.example.aop.aspectj

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.psi.util.PsiTreeUtil
import org.example.aop.aspectj.psi.AspectJPointcutReference
import org.example.aop.aspectj.psi.DesignatorReference

class AspectJCrossFileReferenceTest : BasePlatformTestCase() {

	fun testPointcutReferenceResolvesAcrossFiles() {
		val declaration = myFixture.addFileToProject(
			"lib/Shared.aj",
			"""
				aspect SharedAspect {
					pointcut sharedPointcut() : execution(* shared(..));
				}
			""".trimIndent()
		)

		val usage = myFixture.addFileToProject(
			"Use.aj",
			"""
				aspect UseAspect {
					before() : sharedPointcut() { }
				}
			""".trimIndent()
		)

		myFixture.configureFromExistingVirtualFile(usage.virtualFile)
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

		val usage = myFixture.addFileToProject(
			"Use.aj",
			"""
				aspect UseAspect {
					before() : sharedPointcut() { }
				}
			""".trimIndent()
		)

		myFixture.configureFromExistingVirtualFile(usage.virtualFile)
		myFixture.enableInspections(AspectJInspection())

		val highlightInfos = myFixture.doHighlighting()
		assertTrue(
			"Cross-file pointcut usage should not be flagged as undefined",
			highlightInfos.none { info -> info.description?.contains("not defined", ignoreCase = true) == true }
		)
	}
}











