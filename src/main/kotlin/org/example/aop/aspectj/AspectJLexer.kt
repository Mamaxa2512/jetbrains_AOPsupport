package org.example.aop.aspectj

import com.intellij.lexer.LexerBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

class AspectJLexer : LexerBase() {

    private var buffer: CharSequence = ""
    private var startOffset: Int = 0
    private var endOffset: Int = 0
    private var tokenStart: Int = 0
    private var tokenEnd: Int = 0
    private var tokenType: IElementType? = null
    private var state: Int = 0

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.startOffset = startOffset
        this.endOffset = endOffset
        this.tokenStart = startOffset
        this.tokenEnd = startOffset
        this.state = initialState
        locateToken(startOffset)
    }

    override fun getState(): Int = state
    override fun getTokenType(): IElementType? = tokenType
    override fun getTokenStart(): Int = tokenStart
    override fun getTokenEnd(): Int = tokenEnd
    override fun getBufferSequence(): CharSequence = buffer
    override fun getBufferEnd(): Int = endOffset

    override fun advance() {
        if (tokenType == null) return
        locateToken(tokenEnd)
    }

    private fun locateToken(position: Int) {
        var i = position
        tokenStart = i

        if (i >= endOffset) {
            tokenType = null
            tokenEnd = endOffset
            return
        }

        if (buffer[i].isWhitespace()) {
            i++
            while (i < endOffset && buffer[i].isWhitespace()) i++
            setToken(TokenType.WHITE_SPACE, i)
            return
        }

        val ch = buffer[i]
        when {
            ch == '/' && i + 1 < endOffset && buffer[i + 1] == '/' -> {
                i += 2
                while (i < endOffset && buffer[i] != '\n') i++
                setToken(AspectJTokenTypes.COMMENT, i)
            }
            ch == '/' && i + 1 < endOffset && buffer[i + 1] == '*' -> {
                i += 2
                while (i + 1 < endOffset && !(buffer[i] == '*' && buffer[i + 1] == '/')) i++
                if (i + 1 < endOffset) i += 2
                setToken(AspectJTokenTypes.COMMENT, i)
            }
            ch == '"' || ch == '\'' -> {
                val quote = ch
                i++
                while (i < endOffset) {
                    if (buffer[i] == '\\' && i + 1 < endOffset) {
                        i += 2
                        continue
                    }
                    if (buffer[i] == quote) {
                        i++
                        break
                    }
                    i++
                }
                setToken(AspectJTokenTypes.STRING, i)
            }
            ch == '@' -> {
                i++
                while (i < endOffset && isIdentifierPart(buffer[i])) i++
                while (i < endOffset && buffer[i] == '.') {
                    i++
                    while (i < endOffset && isIdentifierPart(buffer[i])) i++
                }
                setToken(AspectJTokenTypes.ANNOTATION, i)
            }
            ch.isDigit() -> {
                i++
                while (i < endOffset && buffer[i].isDigit()) i++
                setToken(AspectJTokenTypes.NUMBER, i)
            }
            isIdentifierStart(ch) -> {
                i++
                while (i < endOffset && isIdentifierPart(buffer[i])) i++
                val word = buffer.subSequence(tokenStart, i).toString()
                val lower = word.lowercase()
                setToken(if (lower in AspectJTokenTypes.KEYWORDS) AspectJTokenTypes.KEYWORD else AspectJTokenTypes.IDENTIFIER, i)
            }
            else -> {
                i++
                setToken(AspectJTokenTypes.PUNCTUATION, i)
            }
        }
    }

    private fun setToken(type: IElementType, end: Int) {
        tokenType = type
        tokenEnd = end
    }

    private fun isIdentifierStart(ch: Char): Boolean = ch == '_' || ch == '$' || ch.isLetter()
    private fun isIdentifierPart(ch: Char): Boolean = isIdentifierStart(ch) || ch.isDigit()
}
