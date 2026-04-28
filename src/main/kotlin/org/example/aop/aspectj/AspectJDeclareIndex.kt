package org.example.aop.aspectj

import com.intellij.util.indexing.ID

class AspectJDeclareIndex : AbstractAspectJIndex() {
    companion object {
        val NAME: ID<String, Void> = ID.create("org.example.aop.aspectj.declare.index")
    }

    override fun getName(): ID<String, Void> = NAME

    override fun getVersion(): Int = 1

    override fun getIndexer() = psiIndexer(AspectJIndexSupport::declareKeys)
}
