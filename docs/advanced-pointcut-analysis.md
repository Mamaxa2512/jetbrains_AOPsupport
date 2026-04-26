# Покращений синтаксичний аналіз Pointcut виразів

**Дата**: 2026-04-26  
**Версія**: 1.1.0 (планується)

## Огляд

Плагін тепер включає детальний парсер та валідатор pointcut виразів, який виходить за межі базової перевірки синтаксису та аналізує структуру кожного designator.

## Що покращено

### До (версія 1.0.0-beta.1)

Базова валідація перевіряла тільки:
- ✅ Порожні вирази
- ✅ Незбалансовані дужки
- ✅ Невідомі designators
- ✅ Логічні оператори на початку/кінці
- ✅ Послідовні оператори

**Приклад:**
```java
@Before("execution()")  // ❌ Помилка: порожній designator
@Before("execution(* *(..))")  // ✅ OK (але не перевіряється структура)
```

### Після (версія 1.1.0)

Детальний аналіз перевіряє:
- ✅ **Структуру execution patterns**
- ✅ **Валідність type patterns**
- ✅ **Валідність method signatures**
- ✅ **Валідність parameter patterns**
- ✅ **Валідність annotation types**
- ✅ **Валідність bean name patterns**
- ✅ **Wildcards та їх використання**
- ⚠️ **Performance warnings** для занадто широких patterns

## Детальна валідація по designators

### 1. execution()

**Формат:**
```
execution(modifiers? return-type declaring-type? method-name(params) throws?)
```

**Що перевіряється:**

#### Return Type
```java
// ✅ Валідні
execution(void doSomething())
execution(* doSomething())
execution(String getName())
execution(com.example.User getUser())

// ❌ Невалідні
execution(123invalid doSomething())  // Помилка: Invalid return type pattern
execution(void-invalid doSomething())  // Помилка: Invalid return type pattern
```

#### Declaring Type
```java
// ✅ Валідні
execution(* com.example.UserService.createUser(..))
execution(* com.example..*Service.*(..))
execution(* *Service.*(..))

// ❌ Невалідні
execution(* 123invalid.method(..))  // Помилка: Invalid declaring type pattern
execution(* com..123.Service.*(..))  // Помилка: Invalid declaring type pattern
```

#### Method Name
```java
// ✅ Валідні
execution(* getName())
execution(* get*())
execution(* *Service())
execution(* set*Name())

// ❌ Невалідні
execution(* 123invalid())  // Помилка: Invalid method name pattern
execution(* get-name())  // Помилка: Invalid method name pattern (дефіс не дозволений)
```

#### Parameters
```java
// ✅ Валідні
execution(* save(..))
execution(* save())
execution(* save(String))
execution(* save(String, int))
execution(* save(com.example.User, ..))
execution(* save(*, String))

// ❌ Невалідні
execution(* save(123invalid))  // Помилка: Invalid parameter pattern
execution(* save(String, 123))  // Помилка: Invalid parameter pattern
```

#### Performance Warnings
```java
// ⚠️ Попередження
@Before("execution(* *(..))")
// Warning: execution(* *(..)) matches ALL methods - this may cause performance issues

// ✅ Краще
@Before("execution(* com.example.service.*.*(..))")
```

### 2. within()

**Формат:**
```
within(type-pattern)
```

**Що перевіряється:**

#### Type Patterns
```java
// ✅ Валідні
within(com.example.UserService)
within(com.example.service..*)
within(com.example..*)
within(*Service)

// ❌ Невалідні
within(123invalid)  // Помилка: Invalid type pattern
within(com.123.Service)  // Помилка: Invalid type pattern
within()  // Помилка: within() requires a type pattern
```

#### Performance Warnings
```java
// ⚠️ Попередження
@Before("within(..*)")
// Warning: within(..*) matches ALL types - consider narrowing the scope

@Before("within(*)")
// Warning: within(*) matches ALL types - consider narrowing the scope

// ✅ Краще
@Before("within(com.example.service..*)")
```

### 3. @annotation(), @within(), @target()

**Формат:**
```
@annotation(annotation-type)
@within(annotation-type)
@target(annotation-type)
```

**Що перевіряється:**

```java
// ✅ Валідні
@annotation(Transactional)
@annotation(org.springframework.transaction.annotation.Transactional)
@within(Service)
@target(org.springframework.stereotype.Component)

// ❌ Невалідні
@annotation()  // Помилка: @annotation() requires an annotation type
@annotation(123invalid)  // Помилка: Invalid annotation type
@within(com.123.Invalid)  // Помилка: Invalid annotation type
```

### 4. this(), target()

**Формат:**
```
this(type)
target(type)
```

**Що перевіряється:**

```java
// ✅ Валідні
this(UserService)
this(com.example.UserService)
target(com.example.service..*)

// ❌ Невалідні
this()  // Помилка: this() requires a type
this(123invalid)  // Помилка: Invalid type
target(com.123.Service)  // Помилка: Invalid type
```

### 5. args()

**Формат:**
```
args(param-types)
```

**Що перевіряється:**

```java
// ✅ Валідні
args(..)
args(String)
args(String, int)
args(String, ..)
args(*, String)

// ❌ Невалідні
args(123invalid)  // Помилка: Invalid parameter pattern
args(String, 123)  // Помилка: Invalid parameter pattern
```

### 6. @args()

**Формат:**
```
@args(annotation-types)
```

**Що перевіряється:**

```java
// ✅ Валідні
@args(Validated)
@args(Validated, ..)
@args(.., Validated)

// ❌ Невалідні
@args()  // Помилка: @args() requires annotation types
@args(123invalid)  // Помилка: Invalid annotation type
@args(Validated, 123)  // Помилка: Invalid annotation type
```

### 7. bean() (Spring-specific)

**Формат:**
```
bean(name-or-pattern)
```

**Що перевіряється:**

```java
// ✅ Валідні
bean(userService)
bean(*Service)
bean(user-service)  // Дефіси дозволені в bean names
bean(user_service)  // Підкреслення дозволені

// ❌ Невалідні
bean()  // Помилка: bean() requires a bean name or pattern
bean(user@service)  // Помилка: Invalid bean name pattern
bean(user#service)  // Помилка: Invalid bean name pattern
```

## Wildcards

### Підтримувані wildcards

#### `*` - Single element wildcard
```java
// В типах
execution(* com.example.*.UserService.*(..))  // Один рівень package
execution(* *Service.*(..))  // Будь-який клас, що закінчується на Service

// В методах
execution(* get*())  // Методи, що починаються з get
execution(* *Name())  // Методи, що закінчуються на Name
execution(* set*Name())  // Методи з set в середині
```

#### `..` - Multiple elements wildcard
```java
// В типах
execution(* com.example..*.*(..))  // Будь-яка кількість рівнів package

// В параметрах
execution(* save(..))  // Будь-які параметри
execution(* save(String, ..))  // String + будь-які інші
```

### Валідація wildcards

```java
// ✅ Валідні
execution(* com.example..*Service.*(..))
execution(* get*Name())
within(com.example..*)

// ❌ Невалідні (поки що не підтримується, але планується)
execution(* com.*.*.Service.*(..))  // Множинні * в одному рівні
execution(* get**Name())  // Подвійні wildcards
```

## Складні вирази

### Логічні оператори

```java
// ✅ Валідні
execution(* *(..)) && within(com.example..*)
execution(* get*(..)) || execution(* set*(..))
execution(* *(..)) && !within(com.example.internal..*)
(execution(* *(..)) && within(com.example..*)) || bean(*Service)

// ❌ Невалідні
execution(* *(..)) &&  // Помилка: Trailing operator
&& within(com.example..*)  // Помилка: Leading operator
execution(* *(..)) && && within(*)  // Помилка: Consecutive operators
```

### Вкладені дужки

```java
// ✅ Валідні
(execution(* *(..)) && within(com.example..*)) || bean(*Service)
((execution(* get*(..)) || execution(* set*(..))) && @annotation(Transactional))

// ❌ Невалідні
(execution(* *(..))  // Помилка: Unbalanced parentheses
execution(* *(..)))  // Помилка: Unbalanced parentheses
```

## Типи повідомлень

### Errors (червоні)

Критичні помилки, які роблять pointcut невалідним:
- Invalid type patterns
- Invalid method signatures
- Invalid parameter patterns
- Empty designator arguments
- Unbalanced parentheses
- Unknown designators

### Warnings (жовті)

Попередження про потенційні проблеми:
- Overly broad patterns (performance concerns)
- Deprecated designators
- Redundant wildcards

## Приклади з реального життя

### ✅ Добрі практики

```java
// Специфічний package
@Before("execution(* com.example.service.*.*(..))")

// Специфічний тип методів
@Before("execution(public * com.example.service.*.*(..))")

// З annotation
@Before("execution(* *(..)) && @annotation(Transactional)")

// З bean name pattern
@Before("execution(* *(..)) && bean(*Service)")

// Комбінація умов
@Before("execution(* com.example.service.*.*(..)) && !within(com.example.service.internal..*)")
```

### ⚠️ Погані практики

```java
// Занадто широкий - перехоплює ВСЕ
@Before("execution(* *(..))")
// Warning: matches ALL methods - performance issue

// Занадто широкий within
@Before("within(..*)")
// Warning: matches ALL types

// Краще обмежити scope
@Before("execution(* com.example.service.*.*(..))")
```

## Інтеграція з IDE

### В редакторі

```java
@Before("execution(123invalid getName())")
         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
         Error: Invalid return type pattern: '123invalid'
```

### Quick Documentation (Ctrl+Q)

При наведенні на designator показується:
- Формат синтаксису
- Приклади використання
- Посилання на документацію

### Code Completion

Автодоповнення тепер враховує контекст:
- Після `execution(` пропонує return types
- Після `within(` пропонує package patterns
- Після `@annotation(` пропонує annotation types

## Продуктивність

### Оптимізації

- Парсинг виконується тільки при зміні коду
- Результати кешуються
- Валідація виконується асинхронно
- Не блокує UI thread

### Обмеження

- Максимальна довжина виразу: 10000 символів
- Максимальна глибина вкладеності: 50 рівнів
- Timeout для складних виразів: 1 секунда

## Майбутні покращення

### Версія 1.2

- [ ] Валідація pointcut references
- [ ] Перевірка існування типів в проекті
- [ ] Перевірка існування методів
- [ ] Підказки з автовиправленням

### Версія 2.0

- [ ] Semantic analysis (чи дійсно pointcut матчить методи)
- [ ] Cross-file аналіз
- [ ] Performance profiling
- [ ] Refactoring support

## Порівняння з іншими інструментами

### IntelliJ IDEA Ultimate

| Функція | AOP Plugin | IDEA Ultimate |
|---------|-----------|---------------|
| Базова валідація | ✅ | ✅ |
| Детальний парсинг | ✅ | ✅ |
| Performance warnings | ✅ | ❌ |
| Type checking | ❌ (планується) | ✅ |
| Cross-file analysis | ❌ (планується) | ✅ |

### Eclipse AJDT

| Функція | AOP Plugin | Eclipse AJDT |
|---------|-----------|--------------|
| Базова валідація | ✅ | ✅ |
| Детальний парсинг | ✅ | ✅ |
| AspectJ native syntax | ❌ | ✅ |
| Spring AOP focus | ✅ | ❌ |

## Висновки

Покращений синтаксичний аналіз робить плагін значно кориснішим:

1. **Раннє виявлення помилок** - помилки виявляються під час написання коду
2. **Кращі повідомлення** - зрозумілі пояснення що не так
3. **Performance insights** - попередження про повільні patterns
4. **Краща підтримка** - детальна валідація кожного designator

Це робить розробку з AOP більш безпечною та продуктивною.
