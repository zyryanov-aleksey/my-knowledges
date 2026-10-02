# Правила именования полей

## Имена констант

- имя пишут ЗАГЛАВНЫМИ БУКВАМИ;
- слова разделяют символом `_`;
- обычно константа объявляется как static final;
- имя должно описывать смысл значения, а не просто его тип.

Пример:

```java
// Правильно:
private static final Object NULL_HOLDER = new NullHolder();
public static final int DEFAULT_PORT = -1;

// Неправильно:
private static final Object myNewObject = new NullHolder();
int DEFAULT_PORT = -1;
```

## Имена переменных

Правило относится к: полям классов, параметрам метода, локальным переменным

- Имена переменных должны отражать смысл и состояние, а не тип. 
Пример: `pendingOrders`, `normalizedEmail`, `existingUser`.
- для boolean значений имена должны содержать `is`/`has`/`can`/`should`, пример: `isActive`, `hasAccess`
- Коллекции именуются так же по смыслу или состоянию, но во множественном числе, пример: `activeUsers`, `validatedOrders`, `invalidEmails`
- Если однотипных сущностей несколько - уточнять роль: `sourceAccount`, `targetAccount`, `sender`, `recipient`.
