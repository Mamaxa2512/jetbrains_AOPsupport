package org.example.aop.aspectj

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import org.example.aop.aspectj.psi.PointcutDeclaration
import org.example.aop.aspectj.psi.DesignatorReference

/**
 * Provides hover documentation (Ctrl+Q or mouse hover) for AspectJ elements.
 *
 * Shows:
 * - Pointcut name
 * - Full pointcut expression
 * - Usage count
 * - Declaration location
 */
class AspectJDocumentationProvider : DocumentationProvider {

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element == null) return null

        return when {
            element is PointcutDeclaration -> generatePointcutDoc(element)
            element.parent is DesignatorReference -> generateReferenceDoc(element)
            originalElement is PointcutDeclaration -> generatePointcutDoc(originalElement)
            originalElement?.parent is DesignatorReference -> generateReferenceDoc(originalElement!!)
            else -> null
        }
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element == null) return null

        return when {
            element is PointcutDeclaration -> generateFullPointcutDoc(element)
            element.parent is DesignatorReference -> generateFullReferenceDoc(element)
            originalElement is PointcutDeclaration -> generateFullPointcutDoc(originalElement)
            originalElement?.parent is DesignatorReference -> generateFullReferenceDoc(originalElement!!)
            else -> null
        }
    }

    private fun generatePointcutDoc(pointcut: PointcutDeclaration): String {
        val name = pointcut.getPointcutName() ?: return ""
        val expression = pointcut.getPointcutExpression()?.text ?: "(no expression)"
        val usageCount = countPointcutUsages(pointcut)

        return buildString {
            append("<b>pointcut</b> <code>$name()</code><br/>")
            append("<pre>$expression</pre><br/>")
            append("Usage: <b>$usageCount</b> place${if (usageCount != 1) "s" else ""}")
        }
    }

    private fun generateFullPointcutDoc(pointcut: PointcutDeclaration): String {
        val name = pointcut.getPointcutName() ?: return ""
        val modifier = pointcut.getModifier() ?: "package"
        val expression = pointcut.getPointcutExpression()?.text ?: "(no expression)"
        val usageCount = countPointcutUsages(pointcut)
        val file = pointcut.containingFile.name

        return buildString {
            append("<html>")
            append("<body>")
            append("<h3>Pointcut: <code>$name</code></h3>")
            append("<p><b>Modifier:</b> <code>$modifier</code></p>")
            append("<p><b>Expression:</b></p>")
            append("<pre style='background-color: #f0f0f0; padding: 8px;'>$expression</pre>")
            append("<p><b>Usage:</b> $usageCount reference${if (usageCount != 1) "s" else ""}</p>")
            append("<p><b>File:</b> <code>$file</code></p>")
            append("</body>")
            append("</html>")
        }
    }

    private fun generateReferenceDoc(element: PsiElement): String {
        val refName = element.text
        val file = element.containingFile

        val pointcutDecl = PsiTreeUtil.findChildrenOfType(file, PointcutDeclaration::class.java)
            .firstOrNull { it.getPointcutName() == refName }

        return if (pointcutDecl != null) {
            generatePointcutDoc(pointcutDecl)
        } else {
            "<b>$refName</b> - pointcut reference"
        }
    }

    private fun generateFullReferenceDoc(element: PsiElement): String {
        val refName = element.text
        val file = element.containingFile

        val pointcutDecl = PsiTreeUtil.findChildrenOfType(file, PointcutDeclaration::class.java)
            .firstOrNull { it.getPointcutName() == refName }

        return if (pointcutDecl != null) {
            generateFullPointcutDoc(pointcutDecl)
        } else {
            "<html><body>" +
            "<h3>Pointcut Reference: <code>$refName</code></h3>" +
            "<p style='color: red;'><b>Warning:</b> Pointcut not found in this file</p>" +
            "</body></html>"
        }
    }

    private fun countPointcutUsages(pointcut: PointcutDeclaration): Int {
        return ReferencesSearch.search(pointcut, GlobalSearchScope.projectScope(pointcut.project)).count()
    }

    override fun getDocumentationElementForLookupItem(psiManager: PsiManager?, obj: Any?, element: PsiElement?): PsiElement? {
        return element
    }

    override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String?, context: PsiElement?): PsiElement? {
        return null
    }
}


