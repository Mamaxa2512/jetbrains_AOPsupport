@file:Suppress("unused")

package org.example.aop.aspectj

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon
import com.intellij.icons.AllIcons

object AspectJFileType : LanguageFileType(AspectJLanguage) {
    override fun getName(): String = "AspectJ"
    override fun getDescription(): String = "AspectJ aspect file"
    override fun getDefaultExtension(): String = "aj"
    override fun getIcon(): Icon = AllIcons.FileTypes.JavaClass
}


