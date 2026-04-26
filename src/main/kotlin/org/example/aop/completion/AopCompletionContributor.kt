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
        val designatorPriority = AopCompletionContext.designatorPriority(prefix)
        val operatorPriority = AopCompletionContext.operatorPriority(prefix)
        val templatePriority = AopCompletionContext.templatePriority(prefix)

        AopCompletionContext.logicalOperatorCompletionItems(prefix).forEach { operator ->
            val element = LookupElementBuilder.create(operator)
                .withPresentableText(operator)
                .withTailText("  logical operator", true)
                .withTypeText("pointcut")
                .withInsertHandler { ctx, _ ->
                    ctx.document.insertString(ctx.tailOffset, " ")
                }
            prefixedResult.addElement(PrioritizedLookupElement.withPriority(element, operatorPriority))
        }

        AopInspectionRules.pointcutCompletionItems.forEach { (designator, description) ->
            val element = LookupElementBuilder.create(designator)
                .withPresentableText("$designator(...)")
                .withTailText("  $description", true)
                .withTypeText("pointcut designator")
                .bold()
                .withInsertHandler { ctx, _ ->
                    ctx.document.insertString(ctx.tailOffset, "()")
                    ctx.editor.caretModel.moveToOffset(ctx.tailOffset - 1)
                }
            prefixedResult.addElement(PrioritizedLookupElement.withPriority(element, designatorPriority))
        }

        AopCompletionContext.expressionTemplateCompletionItems(prefix).forEach { template ->
            val element = LookupElementBuilder.create(template.expression)
                .withPresentableText(template.label)
                .withTailText("  ${template.expression}", true)
                .withTypeText("pointcut template")
            prefixedResult.addElement(PrioritizedLookupElement.withPriority(element, templatePriority))
        }
    }
}

internal object AopCompletionContext {
    data class ExpressionTemplate(
        val label: String,
        val expression: String
    )

    private val supportedPointcutAttributeNames = setOf(
        PsiAnnotation.DEFAULT_REFERENCED_METHOD_NAME,
        "value",
        "pointcut"
    )

    private val logicalOperators = listOf("&&", "||", "!")

    private val expressionTemplates = listOf(
        ExpressionTemplate(
            label = "Execution in package",
            expression = "execution(* com.example..*(..))"
        ),
        ExpressionTemplate(
            label = "Execution and package scope",
            expression = "execution(* com.example..*(..)) && within(com.example..*)"
        ),
        ExpressionTemplate(
            label = "Execution excluding internal package",
            expression = "execution(* com.example..*(..)) && !within(com.example.internal..*)"
        )
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

    fun logicalOperatorCompletionItems(prefix: String): List<String> {
        val trimmedPrefix = prefix.trim()
        if (trimmedPrefix.isEmpty()) return listOf("!")
        return logicalOperators
    }

    fun expressionTemplateCompletionItems(prefix: String): List<ExpressionTemplate> {
        if (prefix.isNotBlank()) return emptyList()
        return expressionTemplates
    }

    fun designatorPriority(prefix: String): Double {
        val trimmed = prefix.trim()
        return when {
            trimmed.isEmpty() -> 120.0
            isLikelyDesignatorPrefix(trimmed) -> 220.0
            else -> 110.0
        }
    }

    fun operatorPriority(prefix: String): Double {
        val trimmed = prefix.trimEnd()
        return when {
            trimmed.isBlank() -> 130.0
            isOperatorContinuationContext(trimmed) -> 200.0
            else -> 90.0
        }
    }

    fun templatePriority(prefix: String): Double {
        return if (prefix.isBlank()) 100.0 else 0.0
    }

    private fun isLikelyDesignatorPrefix(trimmedPrefix: String): Boolean {
        if (trimmedPrefix.contains("&&") || trimmedPrefix.contains("||")) return false
        if (trimmedPrefix.contains(' ')) return false
        return trimmedPrefix.none { it == ')' || it == '(' }
    }

    private fun isOperatorContinuationContext(trimmedPrefix: String): Boolean {
        return trimmedPrefix.endsWith(')') || trimmedPrefix.endsWith('*')
    }
}

