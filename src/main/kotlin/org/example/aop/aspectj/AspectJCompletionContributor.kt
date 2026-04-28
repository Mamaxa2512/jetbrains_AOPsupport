package org.example.aop.aspectj

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.example.aop.inspection.AopInspectionRules
import org.example.aop.aspectj.psi.AspectDeclaration
import org.example.aop.aspectj.psi.PointcutDeclaration

class AspectJCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(AspectJLanguage),
            AspectJPointcutCompletionProvider()
        )
    }
}

private class AspectJPointcutCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        val fileText = position.containingFile.text
        val offset = parameters.offset
        val inPointcutContext = AspectJTextSupport.isPointcutContext(fileText, offset)
        val inDeclareContext = AspectJTextSupport.isDeclareContext(fileText, offset)
        val inAspectHeaderContext = AspectJTextSupport.isAspectHeaderContext(fileText, offset)

        if (!inPointcutContext && !inDeclareContext && !inAspectHeaderContext) return

        val prefix = AspectJTextSupport.extractCompletionPrefix(fileText, offset)
        val prefixed = result.withPrefixMatcher(prefix)

        addStructuralKeywordCompletions(inDeclareContext, inAspectHeaderContext, prefixed)

        // Add built-in pointcut designators
        if (inPointcutContext) {
            AopInspectionRules.pointcutCompletionItems.forEach { (designator, description) ->
                val element = LookupElementBuilder.create(designator)
                    .withPresentableText("$designator(...)")
                    .withTailText("  $description", true)
                    .withTypeText("AspectJ pointcut")
                    .withInsertHandler { ctx, _ ->
                        ctx.document.insertString(ctx.tailOffset, "()")
                        ctx.editor.caretModel.moveToOffset(ctx.tailOffset - 1)
                    }
                prefixed.addElement(PrioritizedLookupElement.withPriority(element, 200.0))
            }

            // Collect declared pointcuts from PSI tree
            val declaredPointcuts = collectDeclaredPointcuts(position)
            declaredPointcuts.forEach { (pointcutName, _) ->
                val element = LookupElementBuilder.create(pointcutName)
                    .withPresentableText("$pointcutName()")
                    .withTailText("  declared pointcut", true)
                    .withTypeText("AspectJ")
                    .withInsertHandler { ctx, _ ->
                        ctx.document.insertString(ctx.tailOffset, "()")
                        ctx.editor.caretModel.moveToOffset(ctx.tailOffset - 1)
                    }
                prefixed.addElement(PrioritizedLookupElement.withPriority(element, 180.0))
            }

            // Also keep regex-based fallback for compatibility
            AspectJTextSupport.collectDeclaredPointcutNames(fileText).forEach { pointcutName ->
                val element = LookupElementBuilder.create(pointcutName)
                    .withPresentableText("$pointcutName()")
                    .withTailText("  declared pointcut (regex fallback)", true)
                    .withTypeText("AspectJ")
                    .withInsertHandler { ctx, _ ->
                        ctx.document.insertString(ctx.tailOffset, "()")
                        ctx.editor.caretModel.moveToOffset(ctx.tailOffset - 1)
                    }
                prefixed.addElement(PrioritizedLookupElement.withPriority(element, 170.0))
            }

            // Add logical operators
            listOf("&&", "||", "!").forEach { operator ->
                val element = LookupElementBuilder.create(operator)
                    .withTailText("  logical operator", true)
                    .withTypeText("AspectJ")
                prefixed.addElement(PrioritizedLookupElement.withPriority(element, 120.0))
            }
        }
    }

    private fun addStructuralKeywordCompletions(
        inDeclareContext: Boolean,
        inAspectHeaderContext: Boolean,
        result: CompletionResultSet
    ) {
        if (inDeclareContext) {
            AspectJTextSupport.declareKeywordCompletions().forEach { keyword ->
                result.addElement(
                    PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(keyword)
                            .withTypeText("AspectJ declare")
                            .withTailText("  declare $keyword", true),
                        220.0
                    )
                )
            }
        }

        if (inAspectHeaderContext) {
            AspectJTextSupport.perClauseCompletions().forEach { keyword ->
                result.addElement(
                    PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(keyword)
                            .withPresentableText("$keyword(...)")
                            .withTypeText("AspectJ per-clause")
                            .withInsertHandler { ctx, _ ->
                                ctx.document.insertString(ctx.tailOffset, "()")
                                ctx.editor.caretModel.moveToOffset(ctx.tailOffset - 1)
                            },
                        210.0
                    )
                )
            }
        }

        listOf("aspect", "privileged", "declare", "pointcut", "before", "after", "around").forEach { keyword ->
            result.addElement(
                PrioritizedLookupElement.withPriority(
                    LookupElementBuilder.create(keyword)
                        .withTypeText("AspectJ keyword"),
                    80.0
                )
            )
        }
    }
    
    private fun collectDeclaredPointcuts(position: PsiElement): List<Pair<String, PointcutDeclaration>> {
        val result = mutableListOf<Pair<String, PointcutDeclaration>>()
        val file = position.containingFile
        
        // Find all pointcut declarations in the file
        var element: PsiElement? = file.firstChild
        while (element != null) {
            collectPointcutsFromElement(element, result)
            element = element.nextSibling
        }
        
        return result.distinctBy { it.first }
    }
    
    private fun collectPointcutsFromElement(
        element: PsiElement,
        result: MutableList<Pair<String, PointcutDeclaration>>
    ) {
        when (element) {
            is AspectDeclaration -> {
                element.getPointcutDeclarations().forEach { pointcut ->
                    pointcut.getPointcutName()?.let { name ->
                        result.add(name to pointcut)
                    }
                }
            }
        }
        
        // Recursively search children
        var child = element.firstChild
        while (child != null) {
            collectPointcutsFromElement(child, result)
            child = child.nextSibling
        }
    }
}

