---
agent-desc: "
Обязательные правила группировки, сортировки и оформления импортов в Java. 

Как объявлять импорты, как группировать импорты, сортировать импорты, как использовать wildcard-импорты, импорт static элементов, импорт при написании javadoc.

Как правильно импортировать статические классы, методы, переменные (константы, енамы).
"
---

# Импорты

* Блок импортов располагается после объявления пакета и перед объявлением класса.
* Импорты делятся на группы, между непустыми группами располагается ровно одна пустая строка.
* Внутри группы пустых строк нет.
* Каждый импорт записывается отдельным объявлением на одной строке, без переноса импортируемого имени.
* Импорты с `*` запрещены, включая статические импорты и импорты в тестах.
* Внутри каждой группы пакетов имена сортируются по алфавиту

## Расположение и порядок групп

Порядок:

1. Все обычные импорты, кроме javax.* и java.*. Сюда входят jakarta, Spring и классы проекта.
2. Пустая строка.
3. Импорты javax.*, затем java.* — без пустой строки между ними.
4. Пустая строка.
5. Все статические импорты.

Пример правильного объявления импорта:

```java
import com.example.Order;
import jakarta.validation.Valid;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.sql.DataSource;
import java.time.Clock;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
```

Пример недопустимого/неправильного объявления импортов:

```java
import java.util.*;
import my.proj.MyClass.myProductionMethod;
import org.*;
import static org.assertj.core.api.Assertions.*;
```

## Статические импорты

- *Обычные статические методы*: статический импорт не используется; вызов записывается через имя класса
- *Константы, включая enum-константы*: Статический импорт допустим

Пример импорта статического метода:

```java
// Правильно:
import java.time.Duration;
Duration timeout = Duration.ofSeconds(30);

// Неправильно:
import my.proj.MyClass.myProductionMethod;
MyProdClass class = myProductionMethod("any args");
```

Примеры импорта констант/enum-констант:

```java
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.core.annotation.MergedAnnotations.SearchStrategy.TYPE_HIERARCHY;
```

## Вложенные статические типы

Вложенный статический тип импортируется обычным импортом. Модификатор `static`
в объявлении самого типа не требует `import static`.

Пример:

```java
// Правильно:
import com.example.Container.Nested;

// Неправильно: 
import static com.example.Container.Nested;
```

## Типы, упоминаемые только в Javadoc

Если тип используется только в `{@link }`, то необходимо добавить этот тип в импорты

```java
// Правильно:
import java.time.Clock;
/**
 * Используйте {@link Clock} для управления источником времени.
 */

// Неправильно:
/**
 * Используйте {@link java.time.Clock} для управления источником времени.
 */
```