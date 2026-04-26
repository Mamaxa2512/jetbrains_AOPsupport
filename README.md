# AOP Support Plugin for IntelliJ IDEA

[![Version](https://img.shields.io/badge/version-1.1.0--beta.1-blue.svg)](CHANGELOG.md)
[![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-2025.1-orange.svg)](https://www.jetbrains.com/idea/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

Плагін для IntelliJ IDEA, який покращує розробку з Spring AOP та AspectJ, надаючи підтримку редактора, навігацію та інспекції коду.

> **⚠️ Beta версія**: Цей плагін знаходиться на етапі внутрішнього бета-тестування. Функціонал може змінюватись.

## 🎯 Основні можливості

### 📍 Gutter іконки та навігація
- Іконки біля `@Aspect` класів для швидкого переходу до advice методів
- Іконки біля advice методів для повернення до aspect класу
- Підтримка всіх типів advice: `@Before`, `@After`, `@Around`, `@AfterReturning`, `@AfterThrowing`

### 🎨 Підсвічування анотацій
- Візуальне виділення AOP анотацій в редакторі
- Швидка ідентифікація aspect-коду в проекті

### ⚡ Автодоповнення pointcut виразів
- Розумне автодоповнення всередині pointcut рядків
- Пропозиції:
  - Pointcut designators: `execution`, `within`, `@annotation`, `@target`, `this`, `target`, `args`, `bean` та інші
  - Логічні оператори: `&&`, `||`, `!`
  - Шаблони виразів для типових сценаріїв
- Працює в анотаціях `@Before`, `@After`, `@Around`, `@AfterReturning`, `@AfterThrowing`, `@Pointcut`

### 🔍 Інспекції коду

**AspectNotBean**
- Попереджає, коли `@Aspect` клас не зареєстрований як Spring bean
- Quick fix: автоматично додає `@Component` анотацію
- Розпізнає всі Spring stereotype анотації та мета-анотації

**PointcutSyntax** ⭐ НОВЕ в 1.1.0
- **Детальна валідація синтаксису** pointcut виразів
- Перевіряє структуру кожного designator:
  - `execution()` - валідація method signatures, return types, parameters
  - `within()` - валідація type patterns
  - `@annotation()`, `@within()`, `@target()` - валідація annotation types
  - `bean()` - валідація bean name patterns
  - `args()`, `@args()` - валідація parameter patterns
- **Performance warnings** для занадто широких patterns:
  - `execution(* *(..))` - Warning: matches ALL methods
  - `within(..*)` - Warning: matches ALL types
- Виявляє помилки:
  - Порожні вирази
  - Незбалансовані дужки
  - Невідомі designators
  - Некоректні логічні оператори
  - Відсутні оператори між виразами
  - Невалідні type/method/parameter patterns

## 📦 Встановлення

### З файлу
1. Завантажте `AOP-1.1.0-beta.1.zip` з [releases](../../releases)
2. В IntelliJ IDEA: `Settings` → `Plugins` → `⚙️` → `Install Plugin from Disk...`
3. Виберіть завантажений ZIP файл
4. Перезапустіть IDE

### Вимоги
- IntelliJ IDEA Community або Ultimate 2025.1 (build 251.x)
- Java 8+ проекти
- Spring Framework або AspectJ залежності в проекті

## 🚀 Швидкий старт

### 1. Створіть aspect клас

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

### 2. Використовуйте можливості плагіна

- **Gutter іконки**: Клікніть на іконку біля класу або методу для навігації
- **Автодоповнення**: Всередині `"execution(...)"` натисніть `Ctrl+Space`
- **Інспекції**: Плагін автоматично перевірить ваш код та покаже попередження

### 3. Перевірте налаштування

`Settings` → `Editor` → `Inspections` → `AOP` - переконайтесь, що інспекції увімкнені

## 📋 Підтримувані можливості

### ✅ Підтримується в Beta

| Функція | Статус |
|---------|--------|
| Java файли | ✅ Повна підтримка |
| Spring AOP | ✅ Основний фокус |
| AspectJ анотації | ✅ Підмножина |
| Gutter навігація | ✅ |
| Автодоповнення | ✅ |
| Інспекції | ✅ |

### Підтримувані pointcut designators

`execution`, `within`, `this`, `target`, `args`, `@target`, `@within`, `@annotation`, `@args`, `bean`, `cflow`, `cflowbelow`, `initialization`, `preinitialization`, `staticinitialization`, `handler`, `adviceexecution`

### ❌ Поза scope Beta

- Kotlin файли (планується в майбутніх версіях)
- XML-конфігурація Spring AOP
- Повна підтримка AspectJ compile-time weaving
- Cross-file аналіз використання pointcut
- Tool window для візуалізації

## 🧪 Тестування

### Smoke тести

Дивіться детальний гайд: [`docs/smoke-test-guide.md`](docs/smoke-test-guide.md)

Швидкий чеклист:
- [ ] Плагін завантажується без помилок
- [ ] Gutter іконки з'являються
- [ ] Навігація працює
- [ ] Автодоповнення показує designators
- [ ] Інспекції виявляють помилки
- [ ] Quick fix додає `@Component`

### Запуск тестів

```bash
# Повна збірка з тестами
./gradlew clean build test

# Тільки тести
./gradlew test

# Запуск в sandbox IDE
./gradlew runIde
```

## 🐛 Відомі обмеження

- **Тільки Java**: Kotlin файли не інспектуються в цій beta версії
- **Без XML**: XML-конфігурація Spring AOP не підтримується
- **Без cross-file аналізу**: Використання pointcut між файлами не відстежується
- **AspectJ weaving**: Повні можливості compile-time weaving поза scope

## 📝 Зворотній зв'язок

Ми шукаємо відгуки про:
- Зручність використання функцій
- False positives/negatives в інспекціях
- Проблеми з продуктивністю
- Відсутні функції, які покращили б ваш workflow

### Як повідомити про проблему

Використовуйте [issue templates](.github/ISSUE_TEMPLATE/):
- 🐛 Bug Report
- ✨ Feature Request
- ⚠️ False Positive/Negative

### Включіть:
- Версію плагіна (1.1.0-beta.1)
- Версію IntelliJ IDEA
- Кроки для відтворення
- Приклад коду (якщо можливо)

## 🗺️ Roadmap

Дивіться [`ROADMAP.md`](ROADMAP.md) для повного плану розробки.

### Наступні кроки після beta:
- Tool window для огляду aspects/pointcuts
- Cross-file аналіз використання pointcut
- Покращена навігація до matched методів
- Підтримка Kotlin (за запитом)

## 🏗️ Розробка

### Збірка плагіна

```bash
# Повна збірка
./gradlew clean build ktlintCheck

# Швидка збірка без тестів
./gradlew buildPlugin -x test

# Перевірка сумісності
./gradlew verifyPlugin
```

Артефакт: `build/distributions/AOP-1.1.0-beta.1.zip`

### Структура проекту

```
src/
├── main/
│   ├── kotlin/org/example/aop/
│   │   ├── annotator/      # Підсвічування анотацій
│   │   ├── completion/     # Автодоповнення
│   │   ├── inspection/     # Інспекції коду
│   │   └── marker/         # Gutter іконки
│   └── resources/
│       └── META-INF/
│           └── plugin.xml  # Конфігурація плагіна
└── test/
    ├── kotlin/             # Unit тести
    └── resources/fixtures/ # Тестові файли
```

### Технології

- **Мова**: Kotlin
- **Build**: Gradle 8.x
- **Platform**: IntelliJ Platform SDK 2025.1
- **Testing**: JUnit 5 + IntelliJ Platform Test Framework

## 📄 Ліцензія

[MIT License](LICENSE)

## 👥 Автори

Розроблено в рамках ініціативи AOP tooling.

---

**Версія**: 1.1.0-beta.1  
**Дата релізу**: 2026-04-26  
**Статус**: Internal Beta Testing

## 🆕 Що нового в 1.1.0

### Детальний синтаксичний аналіз pointcut виразів

Плагін тепер включає покращений парсер, який детально аналізує структуру кожного pointcut designator:

- ✅ Валідація `execution()` patterns - перевіряє return types, method names, parameters
- ✅ Валідація `within()` type patterns
- ✅ Валідація annotation types в `@annotation()`, `@within()`, `@target()`
- ✅ Валідація bean name patterns в `bean()`
- ✅ Валідація parameter patterns в `args()` та `@args()`
- ⚠️ Performance warnings для занадто широких patterns

**Приклад:**
```java
// ⚠️ Warning
@Before("execution(* *(..))")
// Warning: execution(* *(..)) matches ALL methods - this may cause performance issues

// ✅ Краще
@Before("execution(* com.example.service.*.*(..))")
```

Детальніше: [`docs/advanced-pointcut-analysis.md`](docs/advanced-pointcut-analysis.md)

Дякуємо за тестування AOP Support плагіна! 🚀
