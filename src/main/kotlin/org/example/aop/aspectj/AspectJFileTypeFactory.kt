@file:Suppress("unused", "DEPRECATION")

package org.example.aop.aspectj

import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory

class AspectJFileTypeFactory : FileTypeFactory() {
    override fun createFileTypes(consumer: FileTypeConsumer) {
        consumer.consume(AspectJFileType, "aj")
    }
}


