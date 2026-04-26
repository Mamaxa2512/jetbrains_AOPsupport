package org.example.aop.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.example.aop.inspection.AopInspectionRules

class AopCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(PsiJavaToken::class.java)
                .withParent(PsiLiteralExpression::class.java),
            AopPointcutCompletionProvider()
        )
    }
}

private class AopPointcutCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        val annotation = PsiTreeUtil.getParentOfType(position, PsiAnnotation::class.java) ?: return
        if (!AopCompletionContext.isSupportedAopAdviceAnnotation(annotation.qualifiedName)) return

        val nameValuePair = PsiTreeUtil.getParentOfType(position, PsiNameValuePair::class.java)
        if (!AopCompletionContext.isSupportedPointcutAttribute(nameValuePair?.name)) return

        // Extract the typed prefix from inside the string literal
        val prefix = AopCompletionContext.extractPrefix(position.text)

        val prefixedResult = result.withPrefixMatcher(prefix)

        AopInspectionRules.pointcutCompletionItems.forEach { (designator, description) ->
            prefixedResult.addElement(
                LookupElementBuilder.create(designator)
                    .withPresentableText("$designator(...)")
                    .withTailText("  $description", true)
                    .withTypeText("pointcut designator")
                    .bold()
                    .withInsertHandler { ctx, _ ->
                        ctx.document.insertString(ctx.tailOffset, "()")
                        ctx.editor.caretModel.moveToOffset(ctx.tailOffset - 1)
                    }
            )
        }
    }
}

internal object AopCompletionContext {
    private val supportedPointcutAttributeNames = setOf(
        PsiAnnotation.DEFAULT_REFERENCED_METHOD_NAME,
        "value",
        "pointcut"
    )

    fun isSupportedAopAdviceAnnotation(qualifiedName: String?): Boolean {
        return qualifiedName in AopInspectionRules.adviceAndPointcutAnnotations
    }

    fun isSupportedPointcutAttribute(attributeName: String?): Boolean {
        return attributeName == null || attributeName in supportedPointcutAttributeNames
    }

    fun extractPrefix(rawTokenText: String): String {
        val quoteIdx = rawTokenText.indexOf('"')
        val dummyIdx = rawTokenText.indexOf(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED)
        return when {
            quoteIdx >= 0 && dummyIdx > quoteIdx -> rawTokenText.substring(quoteIdx + 1, dummyIdx)
            dummyIdx >= 0 -> rawTokenText.substring(0, dummyIdx)
            else -> ""
        }
    }
}

