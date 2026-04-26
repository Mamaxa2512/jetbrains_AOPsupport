# Changelog

All notable changes to the AOP Support plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

[Unreleased]: https://github.com/anonim/aop-support/compare/v1.0.0-beta.1...HEAD
[1.0.0-beta.1]: https://github.com/anonim/aop-support/releases/tag/v1.0.0-beta.1
