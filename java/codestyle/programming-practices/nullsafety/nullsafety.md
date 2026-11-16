---
agent-desc: "
Обязательные правила при написании java кода.

Проверка аргументов функции на null.
Использовать каждый раз при написании методов-обработчиков.
Как правильно принять значения переданные в метод/функцию.

Как правильно описать поле класса, если оно может быть null.

1. Null Checks and Assertions
2. Nullability Annotations
3. Method Parameter Validation
4. Nullable Fields and Return Values
5. NullAway Static Analysis
6. Fail-Fast Principles
7. Defensive Programming

(
Стандарты и правила написания java-кода;
Стандарты и правила ревью java-кода;
Рекомендации для соблюдения стилистики java-кода;
Основные практики при написании java-кода;
)
"
---

# Null Safety and Nullability Guidelines

Null Safety необходим для предотвращения ошибок возникающих из-за null на этапе разработки кода.

Очень важно придерживаться практик: 

1) `Defensive Programming` - защитное программирование, то есть проверка данных и состояний для предотвращения некорректного поведения
2) `Fail-Fast` - принцип раннего обнаружения ошибки (ранний выброс исключений при нарушении контракта)

Для поддержки этих практик в коде необходимо:

- проверять входные аргументы методов (сервисные методы, входящие данные из event/jms/mq listener`ов)
- проверять результаты вызовов api (вызовы сервисов, модулей, rest/grpc api и других сторонних сервисов)
- обязательно помечать возможные null-поля в написанном коде


## Как проверять Null в коде

Для проверки аргументов метода стоит использовать `org.springframework.util.Assert.notNull`, выбрасывающий `IllegalArgumentException`. Сообщение об ошибке должно быть обязательным и должно содержать в себе строку: "... не должен быть null"

Пример корректного объявления метода с поддержкой Null Safety:

```java
public void processPayment(Payment payment, Long userId) {
    Assert.notNull(payment, "Платёж не должен быть null");
    Assert.notNull(userId, "Идентификатор пользователя не должен быть null");

    // Бизнес-логика.
}
```

В остальных случаях стоит использовать `org.springframework.util.Assert.state` выбрасывающий `IllegalStateException`. Сообщение должно содержать причину ошибки несоответствия

```java

void createEvent() {
    // ...
    Event event = obtainEvent();
    Assert.state(event != null, "Событие не должно быть null");
    Assert.state(event.getId() != null, "Id события не должно быть null");
    // ...
}

```

## Как отметить возможный Null (Null-безопасность API и полей)

**По умолчанию мы считаем что поле в любом классе не может быть null** - так же мы указываем это и программно: В `package-info.java` устанавливая non-null по умолчанию:

```java
@NullMarked
package org.springframework.mypackage;

import org.jspecify.annotations.NullMarked;
```

Если какое-то поле может быть `null`, то его стоит пометить как `@Nullable`:

```java
import org.jspecify.annotations.Nullable;

public class MyClass {


    private @Nullable Class<?> nullableType;

    public void setNullableType(@Nullable Class<?> nullableType) {
        this.nullableType = nullableType;
    }

    public @Nullable Class<?> getNullableType() {
        return nullableType;
    }

}
```

Приоритетно использовать именно JSpecify-аннотации (если есть в проекте как зависимость, если нет - добавить)

## @Contract аннотация

К слову, я не особо уверен, что аннотацию `@Contract` стоит использовать, так как поддержка этой самой аннотации достаточно дорогая, ибо ни idea, ни компилятор не смогут как то корректно подсветить если логика в аннотации и методе разошлась


.
