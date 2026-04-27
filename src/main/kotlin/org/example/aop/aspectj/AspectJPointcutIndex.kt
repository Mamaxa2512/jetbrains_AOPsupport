package org.example.aop.aspectj

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.io.EnumeratorStringDescriptor

/**
 * Indexes named AspectJ pointcuts so references can resolve across files.
 */
class AspectJPointcutIndex : FileBasedIndexExtension<String, Void>() {

	companion object {
		val NAME: ID<String, Void> = ID.create("org.example.aop.aspectj.pointcut.index")
		private val POINTCUT_REGEX = Regex("""(?m)^\s*pointcut\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(""")
	}

	override fun getName(): ID<String, Void> = NAME

	override fun getVersion(): Int = 1

	override fun getIndexer(): DataIndexer<String, Void, FileContent> = DataIndexer { inputData ->
		POINTCUT_REGEX.findAll(inputData.contentAsText).associate { match ->
			match.groupValues[1] to null
		}
	}

	override fun getKeyDescriptor() = EnumeratorStringDescriptor.INSTANCE

	override fun getValueExternalizer() = com.intellij.util.io.VoidDataExternalizer.INSTANCE

	override fun getInputFilter() = com.intellij.util.indexing.FileBasedIndex.InputFilter { file ->
		file.extension == "aj"
	}

	override fun dependsOnFileContent(): Boolean = true

	override fun getCacheSize() = 1024
}





