@file:Suppress("unused")

package org.example.aop.aspectj

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.search.GlobalSearchScope
internal class AspectJAnnotationReference(
    element: PsiElement
) : PsiReferenceBase<PsiElement>(element, true) {

    override fun resolve(): PsiElement? {
        val name = element.text.trim().trimStart('@')
        if (name.isBlank()) return null

        val project = element.project
        val scope = GlobalSearchScope.allScope(project)
        val facade = JavaPsiFacade.getInstance(project)

        if ('.' in name) {
            return facade.findClasses(name, scope).firstOrNull()
        }

        // Try imported qualified names first: `import com.foo.MyAnno;` + `@MyAnno`
        val importedQualifiedName = AspectJReferenceSupport.findImportedQualifiedName(element.containingFile.text, name)
        if (importedQualifiedName != null) {
            val importedClass = facade.findClasses(importedQualifiedName, scope).firstOrNull()
            if (importedClass != null) return importedClass
        }

        return PsiShortNamesCache.getInstance(project).getClassesByName(name, scope).firstOrNull()
    }

    override fun getVariants(): Array<Any> = emptyArray()
}




