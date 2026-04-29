@file:Suppress("unused")

package org.example.aop.aspectj

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

internal class AspectJNamedPointcutReference(
    element: PsiElement
) : PsiReferenceBase<PsiElement>(element, true) {

    override fun resolve(): PsiElement? {
        val file = element.containingFile
        val pointcutNameText = element.text

        if ('.' in pointcutNameText) {
            val aspectName = pointcutNameText.substringBeforeLast('.')
            val shortName = pointcutNameText.substringAfterLast('.')
            
            // Resolve the aspect
            val aspectDecl = AspectJReferenceSupport.findAspectDeclarationInProject(file.project, aspectName)
            if (aspectDecl != null) {
                val pointcut = AspectJReferenceSupport.findPointcutDeclaration(aspectDecl.containingFile, shortName)
                return pointcut?.nameIdentifier
            }
            
            // Try Java class
            val javaClass = com.intellij.psi.JavaPsiFacade.getInstance(file.project)
                .findClass(aspectName, com.intellij.psi.search.GlobalSearchScope.allScope(file.project))
                ?: com.intellij.psi.search.PsiShortNamesCache.getInstance(file.project).getClassesByName(aspectName.substringAfterLast('.'), com.intellij.psi.search.GlobalSearchScope.allScope(file.project)).firstOrNull()
            
            if (javaClass != null) {
                return javaClass.methods.firstOrNull { it.name == shortName && it.hasAnnotation("org.aspectj.lang.annotation.Pointcut") }?.nameIdentifier ?: javaClass.methods.firstOrNull { it.name == shortName }?.nameIdentifier
            }
            return null
        }

        val psiDecl = AspectJReferenceSupport.findPointcutDeclaration(file, pointcutNameText)
        if (psiDecl != null) {
            return psiDecl.nameIdentifier
        }

        val projectDecl = AspectJReferenceSupport.findPointcutDeclarationInProject(file.project, pointcutNameText)
        if (projectDecl != null) return projectDecl.nameIdentifier

        // Search unqualified Java/Kotlin @Pointcut methods
        val javaMethods = com.intellij.psi.search.PsiShortNamesCache.getInstance(file.project)
            .getMethodsByName(pointcutNameText, com.intellij.psi.search.GlobalSearchScope.allScope(file.project))
        val javaPointcut = javaMethods.firstOrNull { it.hasAnnotation("org.aspectj.lang.annotation.Pointcut") }
        if (javaPointcut != null) {
            return javaPointcut.nameIdentifier
        }

        // Fallback to regex-based approach for compatibility
        val declarationOffset = AspectJReferenceSupport.findPointcutDeclarationOffset(file.text, pointcutNameText) ?: return null
        return file.findElementAt(declarationOffset)
    }

    override fun getVariants(): Array<Any> = emptyArray()
}


