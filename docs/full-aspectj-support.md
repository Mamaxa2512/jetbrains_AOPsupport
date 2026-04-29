# Full AspectJ Support in IntelliJ IDEA AOP Plugin

Starting from version `1.2.0-beta.1`, the AOP Support Plugin provides robust support for native AspectJ `.aj` files. This expands our initial Java-based `@Aspect` support to encompass the specialized syntax and constructs of the AspectJ programming language.

## Supported AspectJ Constructs

The plugin now parses, indexes, and provides tooling support for the following AspectJ language features:

### 1. Aspects and Privileged Aspects
Aspect declarations are the fundamental building block of AspectJ. We fully support both standard and `privileged` aspects.

```aspectj
public aspect LoggingAspect {
    // ...
}

privileged aspect SecurityAspect {
    // Has access to private members of other classes
}
```

### 2. Per-Clauses (Aspect Instantiation Models)
Per-clauses determine how aspect instances are created. We support syntax highlighting, parsing, and hover documentation for all per-clauses.

```aspectj
aspect UserSessionAspect perthis(execution(* com.example.service.UserService.*(..))) {
    // A new instance of this aspect is created for every unique 'this' object
}
```
**Supported per-clauses:**
*   `issingleton()` (default)
*   `perthis(Pointcut)`
*   `pertarget(Pointcut)`
*   `percflow(Pointcut)`
*   `percflowbelow(Pointcut)`
*   `pertypewithin(TypePattern)`

### 3. Pointcuts and Advice
Standard pointcuts and advice are fully supported, including cross-file resolution and gutter icon navigation.

```aspectj
aspect TransactionAspect {
    pointcut serviceMethods() : execution(public * com.example.service.*.*(..));
    
    Object around() : serviceMethods() {
        // advice body
    }
}
```

### 4. Declare Statements
AspectJ's `declare` statements allow aspects to statically modify the type hierarchy or raise compilation messages. We provide full parsing, syntax highlighting, and hover documentation for:

*   **declare parents**: Introduces new parent classes or interfaces to existing types.
    ```aspectj
    declare parents: com.example.domain.* implements Auditable;
    declare parents: com.example.SpecialService extends BaseService;
    ```
*   **declare warning** & **declare error**: Raises custom compilation messages based on pointcut matches.
    ```aspectj
    declare warning : call(* java.sql.Connection.commit()) 
                    : "Direct transaction management is prohibited. Use @Transactional.";
    ```
*   **declare soft**: Softens checked exceptions thrown by matched join points.
    ```aspectj
    declare soft : java.io.IOException : execution(* com.example.io.*.*(..));
    ```
*   **declare precedence**: Controls the order of aspect application.
    ```aspectj
    declare precedence : SecurityAspect, TransactionAspect, *;
    ```

### 5. Inter-Type Declarations (ITD)
Inter-type declarations allow aspects to inject fields or methods into other classes.

```aspectj
aspect AuditableAspect {
    // Introduce a new field into the 'User' class
    private String User.auditLog;
    
    // Introduce a new method into the 'User' class
    public void User.printAuditLog() {
        System.out.println(this.auditLog);
    }
}
```

## Editor Features for AspectJ

With full AspectJ support, you now have access to the following IDE features when working with `.aj` files:

*   **Gutter Icons & Navigation:** Navigate between aspects, pointcuts, advice, inter-type declarations, and their target types.
*   **Semantic Inspections:** Real-time warnings for unresolved `declare parents` target types, missing inter-type targets, and empty `declare warning` messages.
*   **Smart Code Completion:** Completion for pointcut designators, `declare` statements, per-clauses, and logical operators.
*   **Hover Documentation (Ctrl+Q):** View details about a pointcut, its expression, and usage count, or see details about a `declare` statement or `per-clause`.
*   **Project-Wide Indexing:** Quick resolution of cross-file references, making `Find Usages` and `Rename` refactoring robust and accurate for AspectJ constructs.

## Known Limitations in Beta

*   Full AspectJ compile-time weaving features and configurations are out of scope. The plugin provides source-level assistance, but relies on your build tool (Maven/Gradle) for the actual weaving process.
*   Advanced semantic validation for conflicting `declare precedence` orders is still expanding.
