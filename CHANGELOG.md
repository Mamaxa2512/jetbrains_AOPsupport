# Changelog

All notable changes to the AOP Support plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed
- AspectJ lexer now emits whitespace tokens so punctuation is tokenized cleanly and pointcut expressions parse reliably.
- Cross-file pointcut reference parsing no longer drops named designators in advice expressions.

### Improved
- Registered the AspectJ file type via the modern `fileType` extension for consistent `.aj` recognition.
- Startup association of `.aj` extension is now wrapped in a write action for thread safety.
- Test fixture setup explicitly registers the AspectJ parser definition and file type.

## [1.1.0-beta.1] - 2026-04-26

### Added
- **Детальний синтаксичний аналіз pointcut виразів** (`PointcutParser`)
  - Валідація структури `execution()` patterns (return type, declaring type, method name, parameters)
  - Валідація `within()` type patterns
  - Валідація `@annotation()`, `@within()`, `@target()` annotation types
  - Валідація `bean()` name patterns
  - Валідація `args()` та `@args()` parameter patterns
  - Валідація `this()` та `target()` type references
  - Перевірка wildcards (`*`, `..`) у всіх контекстах
  - Детальні повідомлення про помилки з вказанням типу проблеми

### Improved
- **PointcutSyntax інспекція** тепер використовує детальний парсер
  - Показує як errors (червоні), так і warnings (жовті)
  - Performance warnings для занадто широких patterns (`execution(* *(..))`, `within(..*)`)
  - Більш специфічні повідомлення про помилки (Invalid return type, Invalid method pattern, тощо)

### Added (Tests)
- 30+ нових тестів для `PointcutParser`
- Покриття всіх designators
- Тести для edge cases та складних виразів

### Documentation
- Додано `docs/advanced-pointcut-analysis.md` з детальним описом нового аналізу
- Приклади валідних/невалідних pointcut виразів
- Best practices для написання pointcuts

### Internal
- Загальна кількість тестів: 81 (51 старих + 30 нових)
- Всі тести проходять успішно

## [1.0.0-beta.1] - 2026-04-26

### Added
- **Gutter icons** for `@Aspect` classes and advice methods (`@Before`, `@After`, `@Around`, `@AfterReturning`, `@AfterThrowing`)
  - Navigate from aspect class to advice methods
  - Navigate from advice method back to aspect class
- **AOP annotation highlighting** for `@Aspect`, `@Before`, `@After`, `@Around`, `@AfterReturning`, `@AfterThrowing`, `@Pointcut`
- **Code completion** for pointcut expressions in advice and `@Pointcut` annotations
  - Pointcut designators: `execution`, `within`, `@annotation`, `@within`, `this`, `target`, `args`, `@args`, `@target`, `bean`
  - Logical operators: `&&`, `||`, `!`
  - Expression templates for common patterns
- **Inspections**:
  - `AspectNotBean`: Warns when `@Aspect` class is not a Spring bean (`@Component`, `@Service`, etc.)
    - Quick fix: Add `@Component` annotation
  - `PointcutSyntax`: Validates pointcut expression syntax
    - Detects: empty expressions, unbalanced parentheses, unknown designators, trailing/leading operators, consecutive operators, missing operators between clauses
- **Spring bean detection** with meta-annotation support (custom annotations composed from Spring stereotypes)

### Supported
- IntelliJ IDEA Community/Ultimate 2024.3 (IC-243.x / IU-243.x)
- Java 8+ source projects
- Spring AOP and AspectJ annotations in Java files

### Known Limitations
- Kotlin source files are not inspected (Java PSI only)
- XML-based Spring AOP configuration is not supported
- Full AspectJ compile-time weaving features are out of scope for this beta
- No cross-file pointcut usage analysis yet

### Internal
- 45 automated tests (unit + fixture-based)
- CI pipeline with build, static checks, and plugin verifier
- Compatibility matrix documented

[Unreleased]: https://github.com/anonim/aop-support/compare/v1.1.0-beta.1...HEAD
[1.1.0-beta.1]: https://github.com/anonim/aop-support/releases/tag/v1.1.0-beta.1
[1.0.0-beta.1]: https://github.com/anonim/aop-support/releases/tag/v1.0.0-beta.1
