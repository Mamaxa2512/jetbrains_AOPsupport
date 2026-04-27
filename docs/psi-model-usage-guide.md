# PSI Model Usage Guide

## Quick Start

### Example: Accessing Pointcut Declarations

```kotlin
// Get file
val file = element.containingFile as? AspectJPsiFile

// Find all aspects
val aspects = PsiTreeUtil.findChildrenOfType(file, AspectDeclaration::class.java)

// Get pointcuts from aspect
aspects.forEach { aspect ->
    val pointcuts = aspect.getPointcutDeclarations()
    pointcuts.forEach { pointcut ->
        println("Pointcut: ${pointcut.getPointcutName()}")
        pointcut.getPointcutExpression()?.let { expr ->
            println("  Expression: ${expr.text}")
        }
    }
}
```

### Example: Finding All Advice

```kotlin
val file = element.containingFile
val allAdvice = PsiTreeUtil.findChildrenOfType(file, AdviceDeclaration::class.java)

allAdvice.forEach { advice ->
    println("${advice.getAdviceType()} ${advice.text}")
    advice.getPointcutExpression()?.getDesignators()?.forEach { designator ->
        println("  -> ${designator.text}")
    }
}
```

## Common Patterns

### 1. Traversing the Tree

```kotlin
// Direct child access
val children: List<AdviceDeclaration> = 
    PsiTreeUtil.getChildrenOfTypeAsList(aspect, AdviceDeclaration::class.java)

// Recursive search
val allPointcuts: Collection<PointcutDeclaration> = 
    PsiTreeUtil.findChildrenOfType(file, PointcutDeclaration::class.java)

// First of type
val firstAdvice: AdviceDeclaration? = 
    PsiTreeUtil.findChildOfType(file, AdviceDeclaration::class.java)
```

### 2. Working with Pointcut Expressions

```kotlin
val pointcutDecl: PointcutDeclaration = ...
val expr: PointcutExpression? = pointcutDecl.getPointcutExpression()

if (expr != null) {
    // Get all designators (execution, call, within, etc.)
    expr.getDesignators().forEach { designator ->
        // Each designator is like: execution(...) or call(...)
    }
    
    // Get logical operators
    expr.getLogicalOperators().forEach { op ->
        // op.text will be "&&", "||", or "!"
    }
}
```

### 3. Finding Pointcut References

```kotlin
// Find a pointcut by name
fun findPointcutDeclaration(name: String, file: PsiFile): PointcutDeclaration? {
    return PsiTreeUtil.findChildrenOfType(file, PointcutDeclaration::class.java)
        .find { it.getPointcutName() == name }
}

// Find where it's used
fun findPointcutUsages(pointcut: PointcutDeclaration, file: PsiFile): List<DesignatorReference> {
    return PsiTreeUtil.findChildrenOfType(file, DesignatorReference::class.java)
        .filter { it.text == pointcut.getPointcutName() }
}
```

### 4. Inspections/Quick Fixes Usage

```kotlin
class MyPointcutInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) =
        object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                when (element) {
                    is PointcutDeclaration -> checkPointcut(element, holder)
                    is AdviceDeclaration -> checkAdvice(element, holder)
                    else -> super.visitElement(element)
                }
            }
        }
    
    private fun checkPointcut(pointcut: PointcutDeclaration, holder: ProblemsHolder) {
        val name = pointcut.getPointcutName() ?: return
        if (name.startsWith("_")) {
            holder.registerProblem(
                pointcut.nameIdentifier ?: pointcut,
                "PointCut names shouldn't start with underscore",
                RenameQuickFix(pointcut)
            )
        }
    }
}
```

## Element Types Cheat Sheet

### Understanding Element Types

```kotlin
// Token types (lexical)
AspectJTokenTypes.KEYWORD        // "aspect", "pointcut", "before"
AspectJTokenTypes.IDENTIFIER     // variable/method names
AspectJTokenTypes.PUNCTUATION    // {, }, (, ), etc.

// Element types (structural) - defined in AspectJElementTypes
AspectJElementTypes.ASPECT_DECLARATION       // <aspect_declaration>
AspectJElementTypes.POINTCUT_DECLARATION     // <pointcut_decl>
AspectJElementTypes.POINTCUT_EXPRESSION      // <pointcut_expr>
AspectJElementTypes.DESIGNATOR               // <execution(...)>
```

### AST Node Hierarchy

```
File
├─ AspectDeclaration
│  ├─ Keyword: "aspect"
│  ├─ Identifier: aspect name
│  ├─ Punctuation: {
│  ├─ AdviceDeclaration
│  │  ├─ Keyword: "before"
│  │  ├─ Parameters: (...)
│  │  ├─ Keyword: ":"
│  │  ├─ PointcutExpression
│  │  │  ├─ Designator: execution(...)
│  │  │  ├─ LogicalOp: "&&"
│  │  │  └─ Designator: call(...)
│  │  └─ Body: {...}
│  ├─ PointcutDeclaration
│  │  ├─ Keyword: "pointcut"
│  │  ├─ Identifier: pointcut name
│  │  ├─ Parameters: ()
│  │  ├─ Keyword: ":"
│  │  └─ PointcutExpression
│  │     └─ DesignatorReference: name
│  └─ Punctuation: }
```

## Migration Path

If you're working with code that used `AspectJTextSupport`:

### Before (Regex-based)
```kotlin
val occurrences = AspectJTextSupport.collectPointcutOccurrences(fileText)
occurrences.forEach { occurrence ->
    println("Found: ${occurrence.expression} at ${occurrence.range}")
}
```

### After (PSI-based)
```kotlin
val expressions = PsiTreeUtil.findChildrenOfType(file, PointcutExpression::class.java)
expressions.forEach { expr ->
    println("Found: ${expr.text}")
}
```

## Performance Tips

1. **Use specific searches when possible:**
   ```kotlin
   // ❌ Slower: searches entire tree
   PsiTreeUtil.findChildrenOfType(file, PsiElement::class.java)
   
   // ✅ Faster: only finds specific types
   PsiTreeUtil.findChildrenOfType(file, PointcutDeclaration::class.java)
   ```

2. **Cache results in inspection listeners:**
   ```kotlin
   // ❌ Recalculates on every call
   fun getPointcuts() = PsiTreeUtil.findChildrenOfType(...)
   
   // ✅ Calculate once
   private val pointcutsCache = PsiTreeUtil.findChildrenOfType(...)
   ```

3. **Use context when available:**
   ```kotlin
   // ❌ Search entire file
   PsiTreeUtil.findChildrenOfType(file, PointcutDeclaration::class.java)
   
   // ✅ Search only in aspect
   PsiTreeUtil.getChildrenOfTypeAsList(aspect, PointcutDeclaration::class.java)
   ```

## Debugging

Enable debug logging for parser:

```kotlin
// In test or debug configuration
System.setProperty("idea.log.debug.categories", "aspectj.parser")
```

Print AST tree:

```kotlin
fun printAst(element: PsiElement, indent: String = "") {
    println("$indent${element::class.simpleName} [${element.node?.elementType}] = '${element.text.take(50)}'")
    element.children.forEach { child ->
        printAst(child, "$indent  ")
    }
}

printAst(file)
```

## Troubleshooting

**Q: Parser not creating expected nodes?**
A: Check `AspectJParser.parseTopLevel()` logic. Add debug output:
```kotlin
// In parser
println("Token: ${builder.tokenText}, Type: ${builder.tokenType}")
```

**Q: PSI element is null when expected?**
A: Parser isn't building that node. Check:
1. Is the keyword recognized?
2. Is it reaching the right `parseXxx()` function?
3. Is the `marker.done(ElementType)` being called?

**Q: Element types not showing up in debugger?**
A: Clear IDE cache: `File > Invalidate Caches > Invalidate and Restart`

## Links

- Main implementation: `src/main/kotlin/org/example/aop/aspectj/psi/`
- Test file: (to be created in Phase 2) `src/test/kotlin/org/example/aop/aspectj/AspectJParserTest.kt`
- IntelliJ PSI docs: https://plugins.jetbrains.com/docs/intellij/implementing-psi.html

