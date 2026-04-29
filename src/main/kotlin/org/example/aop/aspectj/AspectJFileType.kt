@file:Suppress("unused")

package org.example.aop.aspectj

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object AspectJFileType : LanguageFileType(AspectJLanguage) {
    override fun getName(): String = "AspectJ"
    override fun getDescription(): String = "AspectJ aspect file"
    override fun getDefaultExtension(): String = "aj"
    override fun getIcon(): Icon = IconLoader.getIcon("/icons/aspectj.svg", AspectJFileType::class.java)
}
