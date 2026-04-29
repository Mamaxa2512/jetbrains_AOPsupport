AspectJ Full Support - Next Steps

1) Language and PSI
- Expand grammar to cover full AspectJ syntax (declare parents/warning/error, inter-type declarations, perthis/pertarget, declare soft, etc.).
- Track tokens for annotations, modifiers, generics, and type patterns in pointcut expressions.
- Add PSI elements for all missing constructs and ensure navigation targets are consistent.

2) Parser and Lexer
- Finalize lexer for operators, wildcards, and qualified identifiers.
- Harden parser error recovery and produce error elements for invalid pointcut expressions.

3) References and Resolution
- Resolve pointcut references across files and modules (already working) and add qualified name handling.
- Add type and member resolution for pointcut signatures (method/field patterns, constructors).
- Implement reference support for annotation-based pointcuts and @Pointcut declarations in Java/Kotlin files.

4) Indexing and Performance
- Extend indexes for aspect declarations, advice declarations, and inter-type declarations.
- Add incremental reindexing tests for file changes and rename refactoring.

5) Inspections and Quick Fixes
- Add inspections for unused pointcuts, unreachable advice, and invalid type patterns.
- Provide quick fixes for common syntax errors and missing imports.

6) Editor Features
- Add completion for designators, types, and annotation names inside pointcut expressions.
- Add documentation provider details for designators and pointcut parameters.

7) Testing
- Add parser tests for all AspectJ constructs and error recovery cases.
- Add cross-language tests for Java/Kotlin annotations and interop.

8) Release
- Update compatibility matrix and user docs.
- Add migration notes and verify plugin packaging for the target IDE versions.

