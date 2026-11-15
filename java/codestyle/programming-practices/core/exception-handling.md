---
agent-desc: "
Обязательные правила при написании java кода.

Обработка исключений.
Как правильно обработать исключение/ошибку.
Правильное использование try-catch.
Как правильно перехватить ошибку.

(
Стандарты и правила написания java-кода;
Стандарты и правила ревью java-кода;
Рекомендации для соблюдения стилистики java-кода;
Основные практики при написании java-кода;
)
"
---

# Правила при обработке исключений (try-catch)

## Перехват исключений

### Не оставлять пустой catch блок

Нельзя игнорировать пойманное исключение, это приводит к ошибкам 

```java
void badExample(){
    // НЕПРАВИЛЬНО
    try {
        process();
    } catch (IOException e) {
        // НЕЛЬЗЯ ОСТАВЛЯТЬ ПУСТОЙ CATCH
    }
}

void goodExample(String filePath){
    // Правильно
    // Ошибка не будет потеряна и будет корректно обработана далее по стеку (например в ServerExceptionHandler)
    try {
        process();
    } catch (IOException exception) {
        String message = "Получена ошибка доступа к файлу, путь к файлу %s".formatted(filePath);
        throw new CustomFileProcessingException(message, exception);
    }
}
```

### Избегать использование слишком общих типов, таких как: `Throwable`, `Exception`, `RuntimeException`, `Error` и т.д.

Стоит избегать перехват слишком общих типов, а так же перехватывать ошибки JVM вместе с обычными исключениями.

Никогда не перехватывать Throwable (OutOfMemoryError, StackOverflowError, LinkageError) - это не обычные ошибки бизнес-логики, и обработка этих ошибок может привести к непредсказуемому результату.

Важно, что не всегда стоит придерживаться этого правила, так как на верхнем уровне обработки (контроллере, листенере или другом верхнеуровневом обработчике) зачастую необходимо гарантировать логирование или преобразование ошибки в ответ. 
Но это должно быть согласованное архитектурное решение!

Пример неправильного использования:

```java
// Неправильно
void badExample(){
    try {
        save();
    } catch (Throwable e) {
        log.error("Something went wrong");
    }
}
```

Неправильно, потому что:
- можно случайно скрыть ошибки программирования (например, скрыть NullPointerException, OutOfMemoryError и т.д.)
- программист не может понять какие ошибки можно тут действительно ожидать
- все ошибки обрабатываются одинаково - это говорит о плохом коде

Пример правильного использования:

```java
// Правильно, потому что поймано специализированное исключение
void goodExample(){
    try {
        save();
    } catch (SQLException exception) {
        throw new CustomDataAccessException("Ошибка при сохранении данных", exception);
    }
}
```

### Не забывать пойманное(исходное) исключение и передавать его дальше по стеку (то есть передавать его в `cause`)

Пример плохой обработки, так как первоначальная причина ошибки будет потеряна:
```java
// плохо
void badExample(){
    try {
        execute();
    } catch (IOException e) {
        throw new RuntimeException("Операция прервана");
    }
}
```

```java
// хорошо
void goodExample(){
    try {
        execute();
    } catch (IOException e) {
        throw new RuntimeException("Операция прервана", e);
    }
}
```

### Не использовать ненужные `try-catch`: например, перехват исключения с его последующим немедленным повторным выбрасыванием

Пример плохой обработки, так как никакой дополнительной обработки нет
```java
// плохо
void badExample(){
    try {
        execute();
    } catch (IOException e) {
        throw e;
    }
}
```

```java
// хорошо
void goodExample(){
    try {
        execute();
    } catch (IOException exception) {
        log.error("Поймано io-исключение", exception);
        throw e;
    }
}
```

### Не использовать проверки `instanceof` внутри `catch` - использовать catch для разных типов

Использование instanceof может привести к путанице и потенциальным ошибкам - лучше использовать несколько `catch` блоков для более явной обработки различных типов ошибок


Пример плохой обработки нескольких ошибок:

```java
void badExample(){
    try {
        execute();
    } catch (Exception e) {
        if (e instanceof IOException) {
            handleIo();
        } else if (e instanceof SQLException) {
            handleSql();
        }
    }
}
```

Пример хорошей обработки нескольких ошибок:

```java
void goodExample(){
    try {
        execute();
    } catch (IOException e) {
        handleIo();
    } catch (SQLException e) {
        handleSql();
    }
}
```

## Создание классов исключений

Необходимо наследоваться от Exception или RuntimeException, в зависимости от того, является ли исключение checked или unchecked.

Пример плохого объявления кастомного исключения:
```java
// неправильно
public class CustomOperationException extends Throwable {

    public CustomOperationException(String message) {
        super(message);
    }
}
```

Пример правильного объявления кастомного исключения:

```java
// правильно
public class CustomOperationException extends Exception {

    public CustomOperationException(String message) {
        super(message);
    }

    public CustomOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

## Выброс исключений

### Инициализация конкретных типов исключений

Требуется инициализировать и выбрасывать конкретные типы исключений, так как:

- тип исключения описывает характер ошибки
- ошибку легче перехватить, код становится понятнее

Пример неправильного выбросы исключения

```java
// неправильно
public void badExample() {
    throw new RuntimeException("Failed");
}
```

Пример правильного выброса ошибки:

```java
// правильно
public void goodExample() {
    throw new CustomOperationException("ВОзникла специализированная ошибка для обработки");
}
```

## finally блок

1) Нельзя использовать return в блоке finally
2) Нельзя выбрасывать исключение в блоке finally