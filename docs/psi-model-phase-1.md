# PSI Model Migration Progress

## Phase 1: ✅ Complete - Basic PSI Structure

### What was implemented:

#### 1. PSI Element Types (`AspectJElementTypes.kt`)
- `ASPECT_DECLARATION` – aspect { ... } blocks
- `ADVICE_DECLARATION` – before/after/around advice
- `POINTCUT_DECLARATION` – pointcut declarations
- `POINTCUT_EXPRESSION` – pointcut expression nodes
- `DESIGNATOR` – individual pointcut designators (execution, call, etc.)
- `LOGICAL_OPERATOR` – &&, ||, ! operators
- Supporting types for parameters, modifiers, references

#### 2. PSI Element Classes (`AspectJPsiElements.kt`)
- `AspectJPsiElement` – base class
- `AspectDeclaration` – represents `aspect { ... }` with methods:
  - `getAspectName(): String?`
  - `getAdviceDeclarations(): List<AdviceDeclaration>`
  - `getPointcutDeclarations(): List<PointcutDeclaration>`

- `AdviceDeclaration` – represents before/after/around with methods:
  - `getAdviceType(): String?`
  - `getPointcutExpression(): PointcutExpression?`
  - `getParameters(): PsiElement?`
  - `getReturningType(): String?`
  - `getThrowingType(): String?`

- `PointcutDeclaration` implements `PsiNameIdentifierOwner`:
  - `getPointcutName(): String?`
  - `getNameIdentifier(): PsiElement?` (required for refactoring support)
  - `getPointcutExpression(): PointcutExpression?`

- `PointcutExpression` – container for designators:
  - `getDesignators(): List<Designator>`
  - `getLogicalOperators(): List<PsiElement>`

- `Designator` – represents execution(...), call(...), etc.
- `DesignatorReference` – pointcut name references in expressions

#### 3. Enhanced Parser (`AspectJParser.kt`)
Builds proper AST instead of just advancing through tokens:

```
File (IFileElementType)
└─ AspectDeclaration
   ├─ IDENTIFIER (aspect name)
   ├─ AdviceDeclaration
   │  ├─ ADVICE_TYPE (before/after/around)
   │  ├─ PARAMETERS (...)
   │  ├─ POINTCUT_EXPRESSION
   │  │  ├─ Designator (execution)
   │  │  ├─ LOGICAL_OPERATOR (&&)
   │  │  └─ Designator (call)
   │  └─ METHOD_BODY
   └─ PointcutDeclaration
      ├─ IDENTIFIER (pointcut name)
      ├─ PARAMETERS (...)
      └─ POINTCUT_EXPRESSION
         └─ Designator
```

Key parsing logic:
- Recognizes `aspect` keyword and braces
- Parses advice declarations with returning/throwing modifiers
- Parses pointcut declarations
- Builds `PointcutExpression` with chain of designators and operators
- Handles nested parentheses in designator content

#### 4. Updated ParserDefinition (`AspectJParserDefinition.kt`)
Maps AST nodes to concrete PSI classes:
```kotlin
override fun createElement(node: ASTNode): PsiElement {
    return when (node.elementType) {
        AspectJElementTypes.ASPECT_DECLARATION -> AspectDeclaration(node)
        AspectJElementTypes.ADVICE_DECLARATION -> AdviceDeclaration(node)
        AspectJElementTypes.POINTCUT_DECLARATION -> PointcutDeclaration(node)
        AspectJElementTypes.POINTCUT_EXPRESSION -> PointcutExpression(node)
        // ... more mappings
    }
}
```

#### 5. Updated Completion Contributor (`AspectJCompletionContributor.kt`)
Now uses PSI tree via `collectDeclaredPointcuts()`:
```kotlin
private fun collectDeclaredPointcuts(position: PsiElement): List<Pair<String, PointcutDeclaration>> {
    // Traverses PSI tree to find PointcutDeclaration nodes
    // Returns list of (name, declaration) pairs
}
```

**Hybrid approach:**
- Primary: PSI tree-based discovery
- Fallback: regex-based (for compatibility)
- Future: Deprecate regex fallback once PSI is stable

### Build Status
✅ **Compilation successful**
✅ **All tests pass**
✅ **No breaking changes to existing code**

## Phase 2: Recommended Next Steps

### 2.1 Resolve Support (Priority: High)
Target: Enable "Go to Definition" on pointcut references

Files to create:
- `AspectJReference.kt` – reference implementation
- Update `AspectJReferenceContributor.kt` to use PSI

Usage:
```kotlin
// @pointcut("myPointcut()") // Ctrl+Click should jump to declaration
```

### 2.2 Rename Refactoring (Priority: High)
Target: Safely rename pointcut declarations across file

Files to modify:
- `PointcutDeclaration.setName()` – currently throws UnsupportedOperationException

Steps:
1. Implement textual rename in AST
2. Register `RenameHandler` in plugin.xml
3. Use PsiNameIdentifierOwner.setName()

### 2.3 Find Usages (Priority: Medium)
Target: Find all pointcut references

Implementation:
- Extend reference classes from Phase 2.1
- Register in plugin.xml: `<lang.findUsagesProvider language="AspectJ" ... />`

### 2.4 Hover Documentation (Priority: Medium)
Target: Show designator descriptions on hover

Files:
- Create `AspectJDocumentationProvider.kt`
- Register in plugin.xml

### 2.5 Improved Syntax Highlighting (Priority: Low)
Target: Color different PSI elements differently

Current: Basic token-level highlighting
Next: Element-level coloring (designators, pointcut names, etc.)

## Architecture Notes

### PSI Tree Benefits
1. **Structural Understanding** – Parser builds semantic tree, not just tokens
2. **Refactoring Ready** – PsiNameIdentifierOwner enables rename/find usages
3. **Semantic Analysis** – Can validate pointcut expressions
4. **IDE Integration** – Leverage IntelliJ's PSI-aware features

### Design Decisions

**Why keep regex fallback?**
- Regex is still used for pointcutContext detection (fast)
- Parser handles main AST parsing
- Gradual migration reduces risk

**Element Types vs TokenTypes?**
- `AspectJElementTypes` – node/element types (structural)
- `AspectJTokenTypes` – token types (lexical)
- Both needed for full PSI support

**Generic vs Specific?**
- Specific classes (PointcutDeclaration extends PsiNameIdentifierOwner)
- Allows strong typing and IDE features
- More verbose but type-safe

## Testing Coverage

Current test suite passes:
- `AopAiSnippetSupportTest`
- `AopAnnotationHighlightingContextTest`
- `AspectJReferenceSupportTest`
- `AspectJTextSupportTest`
- `AopCompletionContextTest`
- `AopInspectionFixtureTest`
- `AopInspectionRulesTest`
- `PointcutParserTest`
- `AopLineMarkerContextTest`

**Next:** Create `AspectJParserTest` to test PSI building directly

## Files Changed Summary

```
✅ NEW:
  src/main/kotlin/org/example/aop/aspectj/psi/AspectJElementTypes.kt
  src/main/kotlin/org/example/aop/aspectj/psi/AspectJPsiElements.kt
  src/main/kotlin/org/example/aop/aspectj/AspectJParser.kt

✅ MODIFIED:
  src/main/kotlin/org/example/aop/aspectj/AspectJParserDefinition.kt
  src/main/kotlin/org/example/aop/aspectj/AspectJCompletionContributor.kt

⚠️ UNCHANGED (for compatibility):
  src/main/kotlin/org/example/aop/aspectj/AspectJTextSupport.kt
  src/main/kotlin/org/example/aop/aspectj/AspectJLexer.kt
```

## Git Usage

First commit message:
```
feat(psi): introduce proper PSI model for AspectJ

- Add AspectJElementTypes for declarations, advice, pointcuts
- Implement PSI element classes (AspectDeclaration, AdviceDeclaration, etc.)
- Create AspectJParser that builds semantic AST
- Update ParserDefinition to map ASTNode to PSI elements
- Migrate AspectJCompletionContributor to use PSI tree
- Keep regex helpers as fallback for compatibility

This is first step toward full IDE support. Next phases:
- Reference resolution (Go to Definition)
- Rename refactoring
- Find usages
- Semantic validation
```

