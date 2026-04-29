package org.example.aop.aspectj

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import org.example.aop.aspectj.psi.TypeReferenceElement

class AspectJMemberReference(
    element: TypeReferenceElement
) : PsiReferenceBase<TypeReferenceElement>(element, true) {

    override fun resolve(): PsiElement? {
        val text = element.text
        if ('.' !in text) return null

        val className = text.substringBeforeLast('.')
        val memberName = text.substringAfterLast('.')

        val project = element.project
        val scope = GlobalSearchScope.allScope(project)
        val facade = com.intellij.psi.JavaPsiFacade.getInstance(project)

        val psiClass = facade.findClass(className, scope) ?: return null

        // Try to find a method matching the name
        val methods = psiClass.findMethodsByName(memberName, true)
        if (methods.isNotEmpty()) {
            return methods.first().nameIdentifier
        }

        // Try to find a field matching the name
        val field = psiClass.findFieldByName(memberName, true)
        if (field != null) {
            return field.nameIdentifier
        }

        return null
    }

    override fun getVariants(): Array<Any> = emptyArray()
}
