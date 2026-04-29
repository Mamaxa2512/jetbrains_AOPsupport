# Release Notes: AOP Support Plugin 1.1.0-beta.1

**Дата релізу**: 2026-04-26  
**Тип релізу**: Beta  
**Попередня версія**: 1.0.0-beta.1

## 🎉 Основні зміни

### Детальний синтаксичний аналіз pointcut виразів

Найбільше покращення в цій версії - це **повністю переписаний аналізатор pointcut виразів**, який тепер виконує детальну валідацію структури кожного designator.

#### Що покращено:

**До (1.0.0):**
```java
@Before("execution()")  // ❌ Помилка: порожній designator
@Before("execution(* *(..))")  // ✅ OK (але структура не перевірялась)
```

**Після (1.1.0):**
```java
@Before("execution()")  
// ❌ Error: execution() requires a method signature pattern

@Before("execution(* *(..))")  
// ⚠️ Warning: execution(* *(..)) matches ALL methods - performance issue

@Before("within(..*)")
// ⚠️ Warning: within(..*) matches ALL types - consider narrowing scope

@Before("bean()")
// ❌ Error: bean() requires a bean name or pattern
```

### Нові можливості

#### 1. Валідація execution() patterns
- Перевірка наявності method signature
- Валідація балансу дужок
- Performance warnings для занадто широких patterns

#### 2. Валідація within() patterns
- Перевірка type patterns
- Валідація package patterns
- Warnings для `within(..*)`

#### 3. Валідація annotation designators
- `@annotation()`, `@within()`, `@target()`
- Перевірка, що annotation type вказаний
- Валідація формату annotation type

#### 4. Валідація bean() patterns
- Перевірка bean name patterns
- Підтримка wildcards в bean names

#### 5. Валідація args() та @args()
- Перевірка parameter patterns
- Підтримка `..` wildcard

#### 6. Performance Warnings
Плагін тепер попереджає про patterns, які можуть викликати проблеми з продуктивністю:
- `execution(* *(..))` - перехоплює ВСІ методи
- `within(..*)`  - перехоплює ВСІ типи

### Технічні деталі

#### Новий код:
- `PointcutParser.kt` - детальний парсер pointcut виразів (400+ рядків)
- `PointcutParserTest.kt` - 30 нових тестів
- `docs/advanced-pointcut-analysis.md` - повна документація

#### Покращений код:
- `PointcutSyntaxInspection.kt` - тепер використовує новий парсер
- Показує як errors (червоні), так і warnings (жовті)

#### Статистика:
- **Нових рядків коду**: ~500
- **Нових тестів**: 30
- **Загальна кількість тестів**: 75 (було 45)
- **Всі тести**: ✅ Проходять

## 📦 Встановлення

### Оновлення з 1.0.0-beta.1

1. Видаліть стару версію (опціонально)
2. Завантажте `AOP-1.1.0-beta.1.zip`
3. `Settings` → `Plugins` → `⚙️` → `Install Plugin from Disk...`
4. Виберіть новий ZIP
5. Перезапустіть IDE

### Нова установка

1. Завантажте `AOP-1.1.0-beta.1.zip` з releases
2. `Settings` → `Plugins` → `⚙️` → `Install Plugin from Disk...`
3. Виберіть ZIP файл
4. Перезапустіть IDE

## 🧪 Що тестувати

### Основні сценарії

1. **Performance warnings**
   ```java
   @Before("execution(* *(..))")  // Має показати warning
   ```

2. **Порожні designators**
   ```java
   @Before("execution()")  // Має показати error
   @Before("within()")     // Має показати error
   @Before("bean()")       // Має показати error
   ```

3. **Складні вирази**
   ```java
   @Before("execution(* com.example..*(..)) && within(com.example.service..*)")
   // Має валідувати обидва designators
   ```

4. **Wildcards**
   ```java
   @Before("execution(* com.example..*Service.*(..))")  // OK
   @Before("within(com.example.service..*)")            // OK
   @Before("bean(*Service)")                            // OK
   ```

### Що очікувати

- **Більше попереджень** - плагін тепер більш "розмовний" і попереджає про потенційні проблеми
- **Детальніші повідомлення** - замість "Invalid pointcut" тепер "execution() requires a method signature pattern"
- **Performance insights** - нові warnings про занадто широкі patterns

## 🐛 Відомі проблеми

Немає критичних проблем. Всі тести проходять.

## 📚 Документація

- [`CHANGELOG.md`](CHANGELOG.md) - повний список змін
- [`docs/advanced-pointcut-analysis.md`](docs/advanced-pointcut-analysis.md) - детальна документація нового аналізу
- [`README.md`](README.md) - оновлений з новими можливостями

## 🔜 Наступні кроки

Після збору feedback від beta-тестерів:

### Версія 1.2 (планується)
- Kotlin підтримка
- Live Templates для швидкого створення aspects
- Quick Documentation (Ctrl+Q) для pointcut designators

### Версія 2.0 (довгострокова перспектива)
- Cross-file аналіз (знаходження методів, що відповідають pointcut)
- Tool window для огляду всіх aspects
- Refactoring support

## 💬 Зворотній зв'язок

Будь ласка, повідомляйте про:
- Помилки та проблеми
- False positives/negatives в новій валідації
- Ідеї для покращення
- Проблеми з продуктивністю

Використовуйте issue templates в `.github/ISSUE_TEMPLATE/`

---

**Дякуємо за тестування!** 🙏

Ваш feedback допоможе зробити плагін кращим для всіх розробників, які працюють з AOP.
