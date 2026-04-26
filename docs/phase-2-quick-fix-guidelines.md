# Phase 2 Quick-Fix Guidelines

## Scope
This document defines safety rules for inspection quick-fixes in the AOP plugin.
Current focus: `AspectNotBeanInspection.AddComponentFix`.

## Safety Contract

### 1. Preconditions
- Apply only when the problem element resolves to an `@Aspect` class.
- Do not modify PSI when the target class cannot be resolved.
- Do not modify PSI when the class modifier list is unavailable.

### 2. Idempotency
- Re-applying the same quick-fix must produce the same final code state.
- The fix must not add duplicate bean stereotype annotations.

### 3. Non-Destructive Behavior
- The fix may only add missing annotation(s) required to satisfy the inspection.
- The fix must not remove existing annotations or alter method/class bodies.
- The fix should keep imports clean via IntelliJ shortening utilities.

### 4. Already-Valid Case
- If the class is already recognized as a Spring bean (including via meta-annotation), the fix must do nothing.

### 5. Error Tolerance
- On unresolved PSI or unexpected node shapes, fail safe (no-op) instead of partial edits.

## Acceptance Criteria
- Applying `Add @Component` twice keeps exactly one effective bean stereotype addition by this fix.
- Existing bean stereotypes (`@Component`, `@Service`, etc.) are not duplicated.
- Meta-annotated bean stereotypes are respected as already valid.

