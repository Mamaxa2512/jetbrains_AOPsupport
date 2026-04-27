package org.example.aop.aspectj

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider

class AspectJPsiFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, AspectJLanguage) {
    override fun getFileType() = AspectJFileType
    override fun toString(): String = "AspectJ File"
}


