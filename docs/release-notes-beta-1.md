# AOP Support Plugin — Beta 1 Release Notes

**Version:** 1.0.0-beta.1  
**Release Date:** 2026-04-26  
**Target:** Internal beta testing

## What's New

This is the first internal beta of the AOP Support plugin for IntelliJ IDEA. The plugin enhances the development experience for Spring AOP and AspectJ projects by providing editor assistance, navigation, and code inspections.

### Key Features

#### 1. Gutter Icons and Navigation
- **Aspect class markers**: Click the gutter icon on an `@Aspect` class to navigate to all advice methods
- **Advice method markers**: Click the gutter icon on advice methods to jump back to the aspect class
- Supports: `@Before`, `@After`, `@Around`, `@AfterReturning`, `@AfterThrowing`

#### 2. AOP Annotation Highlighting
- Visual highlighting for AOP annotations in the editor
- Helps quickly identify aspect-related code

#### 3. Pointcut Expression Completion
- Smart code completion inside pointcut strings
- Suggests:
  - Pointcut designators (`execution`, `within`, `@annotation`, etc.)
  - Logical operators (`&&`, `||`, `!`)
  - Common expression templates
- Works in `@Before`, `@After`, `@Around`, `@AfterReturning`, `@AfterThrowing`, and `@Pointcut` annotations

#### 4. Code Inspections

**AspectNotBean Inspection**
- Warns when an `@Aspect` class is not registered as a Spring bean
- Quick fix: Automatically adds `@Component` annotation
- Recognizes all Spring stereotype annotations and meta-annotations

**PointcutSyntax Inspection**
- Validates pointcut expression syntax
- Catches common errors:
  - Empty expressions
  - Unbalanced parentheses
  - Unknown designators
  - Malformed logical operators
  - Missing operators between clauses

## Installation

### From Build Artifact
1. Download the plugin ZIP from the internal distribution channel
2. In IntelliJ IDEA: `Settings` → `Plugins` → `⚙️` → `Install Plugin from Disk...`
3. Select the downloaded ZIP file
4. Restart IDE

### Requirements
- IntelliJ IDEA Community or Ultimate 2024.3 (build 243.x)
- Java 8+ projects (plugin inspects Java source files)

## Known Limitations

- **Java only**: Kotlin source files are not inspected in this beta
- **No XML config**: XML-based Spring AOP configuration is not supported
- **No cross-file analysis**: Pointcut usage across files is not tracked yet
- **AspectJ weaving**: Full compile-time weaving features are out of scope

## Feedback

We're looking for feedback on:
- Feature usability and discoverability
- False positives/negatives in inspections
- Performance issues with large projects
- Missing features that would improve your workflow

### How to Report Issues
- Use the internal issue tracker: [link to issue tracker]
- Include:
  - Plugin version (1.0.0-beta.1)
  - IntelliJ IDEA version and build number
  - Steps to reproduce
  - Sample code (if applicable)

### Response SLA
- Critical bugs (plugin crash, data loss): 1 business day
- High priority (broken core feature): 3 business days
- Medium/Low priority: Best effort, tracked for next release

## What's Next

After beta feedback, we plan to:
- Add tool window for aspect/pointcut overview
- Implement cross-file pointcut usage analysis
- Improve navigation to matched methods
- Consider Kotlin support based on demand

## Credits

Developed as part of the AOP tooling initiative. See `ROADMAP.md` for the full development plan.

---

Thank you for testing the AOP Support plugin beta!
