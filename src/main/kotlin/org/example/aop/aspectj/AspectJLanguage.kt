package org.example.aop.aspectj

import com.intellij.lang.Language

@Suppress("unused")
object AspectJLanguage : Language("AspectJ") {
	@Suppress("unused")
	private fun readResolve(): Any = AspectJLanguage
}


