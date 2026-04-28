package org.example.aop.aspectj

import com.intellij.lang.LanguageParserDefinitions
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.util.Disposer
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.example.aop.aspectj.psi.AspectDeclaration
import org.example.aop.aspectj.psi.DeclareErrorDeclaration
import org.example.aop.aspectj.psi.DeclareParentsDeclaration
import org.example.aop.aspectj.psi.DeclarePrecedenceDeclaration
import org.example.aop.aspectj.psi.DeclareSoftDeclaration
import org.example.aop.aspectj.psi.DeclareWarningDeclaration
import org.example.aop.aspectj.psi.InterTypeDeclaration
import org.example.aop.aspectj.psi.PerClause
import org.example.aop.aspectj.psi.PointcutDeclaration

class AspectJParserGrammarTest : BasePlatformTestCase() {

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

    fun testParsesPrivilegedAspectWithPerClause() {
        myFixture.configureByText(
            "PrivilegedAspect.aj",
            """
                privileged aspect SecurityAspect perthis(execution(* com.example..*(..))) {
                    before() : execution(* *(..)) { }
                }
            """.trimIndent()
        )

        val aspect = PsiTreeUtil.findChildOfType(myFixture.file, AspectDeclaration::class.java)
        val perClause = PsiTreeUtil.findChildOfType(myFixture.file, PerClause::class.java)

        assertNotNull(aspect)
        assertNotNull(perClause)
        assertEquals("perthis", perClause?.getClauseKind())
    }

    fun testParsesDeclareStatements() {
        myFixture.configureByText(
            "DeclareAspect.aj",
            """
                aspect DeclareAspect {
                    declare warning : execution(* com.example..*(..)) : "warn";
                    declare error : call(* com.example.Service.*(..)) : "err";
                    declare soft : java.lang.Exception : execution(* *(..));
                    declare parents : com.example.Service implements java.io.Serializable, java.lang.Cloneable;
                    declare precedence : com.example..*, *;
                }
            """.trimIndent()
        )

        assertEquals(1, PsiTreeUtil.findChildrenOfType(myFixture.file, DeclareWarningDeclaration::class.java).size)
        assertEquals(1, PsiTreeUtil.findChildrenOfType(myFixture.file, DeclareErrorDeclaration::class.java).size)
        assertEquals(1, PsiTreeUtil.findChildrenOfType(myFixture.file, DeclareSoftDeclaration::class.java).size)
        assertEquals(1, PsiTreeUtil.findChildrenOfType(myFixture.file, DeclareParentsDeclaration::class.java).size)
        assertEquals(1, PsiTreeUtil.findChildrenOfType(myFixture.file, DeclarePrecedenceDeclaration::class.java).size)
    }

    fun testParsesInterTypeDeclarationsAndModifiedPointcuts() {
        myFixture.configureByText(
            "InterTypeAspect.aj",
            """
                aspect InterTypeAspect {
                    private pointcut internal() : execution(* *(..));
                    public int com.example.Service.retryCount;
                    public void com.example.Service.audit() { }
                }
            """.trimIndent()
        )

        assertEquals(1, PsiTreeUtil.findChildrenOfType(myFixture.file, PointcutDeclaration::class.java).size)
        assertEquals(2, PsiTreeUtil.findChildrenOfType(myFixture.file, InterTypeDeclaration::class.java).size)
    }
}
