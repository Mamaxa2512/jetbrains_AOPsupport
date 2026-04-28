package org.example.aop.aspectj

import com.intellij.util.indexing.ID

/**
 * Indexes named AspectJ pointcuts so references can resolve across files.
 */
class AspectJPointcutIndex : AbstractAspectJIndex() {

	companion object {
		val NAME: ID<String, Void> = ID.create("org.example.aop.aspectj.pointcut.index")
	}

	override fun getName(): ID<String, Void> = NAME

	override fun getVersion(): Int = 1

	override fun getIndexer() = psiIndexer(AspectJIndexSupport::pointcutNames)
}
