# Smoke Test Guide

## Purpose
Quick validation that core plugin features work before releasing a beta build.

## Prerequisites
- Plugin built: `./gradlew buildPlugin`
- Or running in sandbox: `./gradlew runIde`

## Test Scenarios

### 1. Plugin Loads Successfully

**Steps:**
1. Start IDE with plugin installed
2. Check `Settings` → `Plugins` → `Installed`
3. Verify "AOP Support" is listed and enabled

**Expected:** Plugin appears in the list, no error notifications

---

### 2. AOP Annotation Highlighting

**Steps:**
1. Create a new Java file with:
   ```java
   import org.aspectj.lang.annotation.Aspect;
   import org.aspectj.lang.annotation.Before;
   import org.springframework.stereotype.Component;
   
   @Aspect
   @Component
   public class TestAspect {
       @Before("execution(* com.example..*(..))")
       public void beforeAdvice() {}
   }
   ```
2. Observe annotation highlighting

**Expected:** `@Aspect`, `@Before`, `@Component` are visually highlighted

---

### 3. Gutter Icons and Navigation

**Steps:**
1. Using the code from scenario 2
2. Look for gutter icon next to `public class TestAspect`
3. Click the icon

**Expected:** 
- Gutter icon appears on the class name
- Clicking navigates to the `beforeAdvice` method

**Steps (continued):**
4. Look for gutter icon next to `public void beforeAdvice()`
5. Click the icon

**Expected:**
- Gutter icon appears on the method name
- Clicking navigates back to `TestAspect` class

---

### 4. Pointcut Expression Completion

**Steps:**
1. Type a new advice method:
   ```java
   @Before("")
   public void newAdvice() {}
   ```
2. Place cursor inside the empty string: `@Before("|")`
3. Trigger completion (Ctrl+Space / Cmd+Space)

**Expected:**
- Completion popup appears
- Shows: `execution(...)`, `within(...)`, `@annotation(...)`, etc.
- Shows logical operators: `&&`, `||`, `!`
- Shows expression templates

**Steps (continued):**
4. Type `exec` and trigger completion

**Expected:**
- `execution(...)` is suggested and prioritized

---

### 5. PointcutSyntax Inspection

**Steps:**
1. Add an invalid pointcut:
   ```java
   @Before("execution(* *(..)) ||")
   public void invalidAdvice() {}
   ```
2. Observe the warning

**Expected:**
- Warning appears on the string literal
- Message: "Pointcut expression cannot end with a logical operator"

**Steps (continued):**
3. Add another invalid pointcut:
   ```java
   @Before("unknown(* *(..))")
   public void unknownDesignator() {}
   ```

**Expected:**
- Warning appears
- Message: "Unknown pointcut designator: 'unknown'"

---

### 6. AspectNotBean Inspection

**Steps:**
1. Create an aspect without Spring bean annotation:
   ```java
   import org.aspectj.lang.annotation.Aspect;
   
   @Aspect
   public class BareAspect {}
   ```
2. Observe the warning

**Expected:**
- Warning appears on `@Aspect`
- Message: "@Aspect class 'BareAspect' is not a Spring Bean — Spring AOP will not apply it"

**Steps (continued):**
3. Trigger quick fix (Alt+Enter / Opt+Enter)
4. Select "Add @Component"

**Expected:**
- `@Component` annotation is added above `@Aspect`
- Warning disappears

---

### 7. Inspection Settings

**Steps:**
1. Go to `Settings` → `Editor` → `Inspections`
2. Search for "AOP"

**Expected:**
- "AOP" group appears
- Two inspections listed:
  - "Invalid pointcut expression syntax"
  - "@Aspect class is not a Spring Bean"
- Both enabled by default

---

## Blocker Criteria

Mark as **blocker** if:
- Plugin fails to load
- IDE crashes when using any feature
- Core feature completely non-functional (no gutter icons, no completion, no inspections)
- Quick fix corrupts code

Mark as **non-blocker** if:
- Minor UI glitch
- Edge case false positive/negative
- Performance issue (unless >5s delay)

## Smoke Test Checklist

- [ ] Plugin loads successfully
- [ ] AOP annotation highlighting works
- [ ] Gutter icons appear for aspect class
- [ ] Gutter icons appear for advice methods
- [ ] Navigation from class to methods works
- [ ] Navigation from method to class works
- [ ] Pointcut completion appears
- [ ] Pointcut completion suggests designators
- [ ] Pointcut completion suggests operators
- [ ] PointcutSyntax inspection detects trailing operator
- [ ] PointcutSyntax inspection detects unknown designator
- [ ] AspectNotBean inspection detects missing bean annotation
- [ ] AspectNotBean quick fix adds @Component
- [ ] Inspections appear in Settings
- [ ] No IDE crashes or errors

## Running Smoke Tests

### Option 1: Sandbox IDE
```bash
./gradlew runIde
```
Then follow the test scenarios above.

### Option 2: Installed Plugin
1. Build: `./gradlew buildPlugin`
2. Install from `build/distributions/AOP-1.0.0-beta.1.zip`
3. Restart IDE
4. Follow test scenarios

## Reporting Issues

If smoke tests fail, document:
- Which scenario failed
- Actual vs expected behavior
- IDE version and build number
- Any error messages or stack traces

Create an issue using the bug report template.
