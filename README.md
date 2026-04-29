# AOP Support Plugin for IntelliJ IDEA

[![Version](https://img.shields.io/badge/version-1.2.0--beta.1-blue.svg)](CHANGELOG.md)
[![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-2025.1-orange.svg)](https://www.jetbrains.com/idea/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

A plugin for IntelliJ IDEA that enhances development with Spring AOP and AspectJ by providing editor support, navigation, and code inspections.

> **⚠️ Beta Version**: This plugin is currently in internal beta testing. Functionality may change.

## 🎯 Key Features

### 📍 Gutter Icons and Navigation

* Icons next to `@Aspect` classes for quick navigation to advice methods
* Icons next to advice methods for returning to the aspect class
* Support for all advice types: `@Before`, `@After`, `@Around`, `@AfterReturning`, `@AfterThrowing`
* Icons in `.aj` files for aspect declarations, pointcuts, `declare` statements, per-clauses, and inter-type declarations

### 🎨 Annotation Highlighting

* Visual highlighting of AOP annotations in the editor
* Quick identification of aspect-related code throughout the project

### ⚡ Pointcut Expression Autocompletion

* Smart autocompletion inside pointcut strings
* Suggestions include:

  * Pointcut designators: `execution`, `within`, `@annotation`, `@target`, `this`, `target`, `args`, `bean`, and others
  * Logical operators: `&&`, `||`, `!`
  * Expression templates for common scenarios
* Works in `@Before`, `@After`, `@Around`, `@AfterReturning`, `@AfterThrowing`, and `@Pointcut` annotations
* For `.aj` files, also suggests `declare` kinds, per-clauses, and key AspectJ constructs

### 🧩 Native `.aj` Support

* Parsing of AspectJ aspects with `privileged`, per-clauses, `declare` statements, and inter-type declarations
* PSI navigation for pointcuts, `declare parents`, and target types in inter-type declarations
* Project-wide indexing for aspect names, pointcut names, `declare` kinds, and inter-type targets
* Hover documentation for pointcut declarations, `declare` statements, and per-clauses
* Inspections for unresolved pointcuts, unresolved `declare parents` / inter-type target types, and empty `declare warning/error` messages

### 🤖 AI Assistants for Pointcuts and AspectJ

* **Generate Pointcut from Description** — generates a pointcut from a text description
* **Explain Pointcut** — explains the selected pointcut or AspectJ snippet
* **Optimize Pointcut** — suggests a narrower and more efficient alternative
* **Fix with AI** — automatically suggests fixes for the selected pointcut / AspectJ fragment
* Works with Java/Kotlin string literals and selected AspectJ fragments in `.aj`

### 🔍 Code Inspections

**AspectNotBean**

* Warns when an `@Aspect` class is not registered as a Spring bean
* Quick fix: automatically adds the `@Component` annotation
* Recognizes all Spring stereotype annotations and meta-annotations

**PointcutSyntax** ⭐ NEW in 1.1.0

* **Detailed syntax validation** for pointcut expressions
* Validates the structure of each designator:

  * `execution()` - validates method signatures, return types, parameters
  * `within()` - validates type patterns
  * `@annotation()`, `@within()`, `@target()` - validates annotation types
  * `bean()` - validates bean name patterns
  * `args()`, `@args()` - validates parameter patterns
* **Performance warnings** for overly broad patterns:

  * `execution(* *(..))` - Warning: matches ALL methods
  * `within(..*)` - Warning: matches ALL types
* Detects errors such as:

  * Empty expressions
  * Unbalanced parentheses
  * Unknown designators
  * Invalid logical operators
  * Missing operators between expressions
  * Invalid type/method/parameter patterns

## 📦 Installation

### From File

1. Download `AOP-1.2.0-beta.1.zip` from [releases](../../releases)
2. In IntelliJ IDEA: `Settings` → `Plugins` → `⚙️` → `Install Plugin from Disk...`
3. Select the downloaded ZIP file
4. Restart the IDE

### Requirements

* IntelliJ IDEA Community or Ultimate 2025.1 (build 251.x)
* Java 8+ projects
* Spring Framework or AspectJ dependencies in the project

## 🚀 Quick Start

### 1. Create an aspect class

```java
package com.example.aspects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    
    @Before("execution(* com.example.service.*.*(..))")
    public void logBefore() {
        System.out.println("Method execution started");
    }
}
```

### 2. Use the plugin features

* **Gutter icons**: Click the icon next to a class or method for navigation
* **Autocompletion**: Inside `"execution(...)"`, press `Ctrl+Space`
* **Inspections**: The plugin automatically checks your code and displays warnings

### 3. Verify settings

`Settings` → `Editor` → `Inspections` → `AOP` - ensure inspections are enabled

## 📋 Supported Features

### ✅ Supported in Beta

| Feature             | Status                                                  |
| ------------------- | ------------------------------------------------------- |
| Java files          | ✅ Full support                                          |
| Kotlin files        | ✅ Basic support (inspections, highlighting, completion) |
| Spring AOP          | ✅ Primary focus                                         |
| AspectJ `.aj` files | ✅ Extended beta support                                 |
| AspectJ annotations | ✅ Subset + native `.aj` PSI                             |
| Gutter navigation   | ✅                                                       |
| Autocompletion      | ✅                                                       |
| Inspections         | ✅                                                       |

### Supported Pointcut Designators

`execution`, `within`, `this`, `target`, `args`, `@target`, `@within`, `@annotation`, `@args`, `bean`, `cflow`, `cflowbelow`, `initialization`, `preinitialization`, `staticinitialization`, `handler`, `adviceexecution`

### ❌ Out of Beta Scope

* Spring AOP XML configuration
* Full support for AspectJ compile-time weaving / LTW configuration
* Semantic validation of every ITD member signature and all `declare precedence` ordering conflicts
* Visualization tool window

## 🧪 Testing

### Smoke Tests

See the detailed guide: [`docs/smoke-test-guide.md`](docs/smoke-test-guide.md)

Quick checklist:

* [ ] Plugin loads without errors
* [ ] Gutter icons appear
* [ ] Navigation works
* [ ] Autocompletion shows designators
* [ ] Inspections detect issues
* [ ] Quick fix adds `@Component`

### Running Tests

```bash
# Full build with tests
./gradlew clean build test

# Tests only
./gradlew test

# Run in sandbox IDE
./gradlew runIde
```

## 🐛 Known Limitations

* **Kotlin**: basic editor features for annotation-based pointcut expressions are supported; advanced cross-file analysis is still limited
* **No XML**: Spring AOP XML configuration is not supported
* **`.aj` support is beta**: grammar/indexing/navigation already cover major constructs, but advanced semantic checks are still expanding
* **AspectJ weaving**: Full compile-time weaving capabilities are out of scope

## 📝 Feedback

We are looking for feedback on:

* Feature usability
* False positives/negatives in inspections
* Performance issues
* Missing features that would improve your workflow

### How to Report an Issue

Use the [issue templates](.github/ISSUE_TEMPLATE/):

* 🐛 Bug Report
* ✨ Feature Request
* ⚠️ False Positive/Negative

### Please Include:

* Plugin version (1.2.0-beta.1)
* IntelliJ IDEA version
* Steps to reproduce
* Code example (if possible)

## 🗺️ Roadmap

See [`ROADMAP.md`](ROADMAP.md) for the full development plan.

### Next Steps After Beta:

* Tool window for aspect/pointcut overview
* Cross-file pointcut usage analysis
* Improved navigation to matched methods
* Expanded Kotlin support (on demand)

## 🏗️ Development

### Building the Plugin

```bash
# Full build
./gradlew clean build ktlintCheck

# Fast build without tests
./gradlew buildPlugin -x test

# Compatibility verification
./gradlew verifyPlugin
```

Artifact: `build/distributions/AOP-1.2.0-beta.1.zip`

### Project Structure

```text
src/
├── main/
│   ├── kotlin/org/example/aop/
│   │   ├── annotator/      # Annotation highlighting
│   │   ├── completion/     # Autocompletion
│   │   ├── inspection/     # Code inspections
│   │   └── marker/         # Gutter icons
│   └── resources/
│       └── META-INF/
│           └── plugin.xml  # Plugin configuration
└── test/
    ├── kotlin/             # Unit tests
    └── resources/fixtures/ # Test files
```

### Technologies

* **Language**: Kotlin
* **Build**: Gradle 8.x
* **Platform**: IntelliJ Platform SDK 2025.1
* **Testing**: JUnit 5 + IntelliJ Platform Test Framework

## 📄 License

[MIT License](LICENSE)

## 👥 Authors

Developed as part of the AOP tooling initiative.

---

**Version**: 1.2.0-beta.1
**Release Date**: 2026-04-28
**Status**: Internal Beta Testing

## 🆕 What's New in 1.1.0

### Detailed Syntax Analysis for Pointcut Expressions

The plugin now includes an enhanced parser that deeply analyzes the structure of each pointcut designator:

* ✅ Validation of `execution()` patterns - checks return types, method names, parameters
* ✅ Validation of `within()` type patterns
* ✅ Validation of annotation types in `@annotation()`, `@within()`, `@target()`
* ✅ Validation of bean name patterns in `bean()`
* ✅ Validation of parameter patterns in `args()` and `@args()`
* ⚠️ Performance warnings for overly broad patterns

**Example:**

```java
// ⚠️ Warning
@Before("execution(* *(..))")
// Warning: execution(* *(..)) matches ALL methods - this may cause performance issues

// ✅ Better
@Before("execution(* com.example.service.*.*(..))")
```

Learn more: [`docs/advanced-pointcut-analysis.md`](docs/advanced-pointcut-analysis.md)

Thank you for testing the AOP Support plugin! 🚀
