@file:Suppress("unused")

package org.example.aop.aspectj

internal object AspectJReferenceSupport {

    private val pointcutDeclarationRegex = Regex("""(?m)^\s*pointcut\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(""")
    private val importRegex = Regex("""(?m)^\s*import\s+([A-Za-z_][A-Za-z0-9_.]*)\s*;\s*$""")

    fun isAnnotationReference(text: String, fileText: String, startOffset: Int): Boolean {
        return text.startsWith('@') && text.length > 1 && previousNonWhitespaceChar(fileText, startOffset) == '('
    }

    fun isNamedPointcutReference(text: String, fileText: String, startOffset: Int): Boolean {
        if (text.isBlank()) return false
        val previous = previousNonWhitespaceChar(fileText, startOffset)
        return previous == ':' && fileText.indexOf(text, startOffset) == startOffset
    }

    fun findPointcutDeclarationOffset(fileText: String, pointcutName: String): Int? {
        return pointcutDeclarationRegex.findAll(fileText).firstOrNull { match ->
            match.groupValues[1] == pointcutName
        }?.groups?.get(1)?.range?.first
    }

    fun findImportedQualifiedName(fileText: String, shortName: String): String? {
        return importRegex.findAll(fileText)
            .map { it.groupValues[1] }
            .firstOrNull { it.substringAfterLast('.') == shortName }
    }

    private fun previousNonWhitespaceChar(text: String, offset: Int): Char? {
        var i = offset - 1
        while (i >= 0 && text[i].isWhitespace()) i--
        return if (i >= 0) text[i] else null
    }
}




