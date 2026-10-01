---
name: java-spring-framework-code-style
description: "Применять русскоязычные правила оформления исходного кода Spring Framework. Использовать при написании, изменении и ревью Java-кода, тестов и Javadoc в проектах, следующих Spring Framework Code Style."
---

# Java and Spring Framework Code Style

## Введение

Документ определяет стандарты написания кода для приложений на Java и Spring Framework.
Сейчас это является набором правил заимствованных и модифицированных у [Spring team](https://github.com/spring-projects/spring-framework/wiki/Code-Style) и [Google team](https://google.github.io/styleguide/javaguide.html)

## Основы исходных файлов

### Кодировка: UTF-8

Исходные файлы должны быть закодированы в `UTF-8`.

### Отступы

- Используйте пробелы, а не табуляции.
- Используйте окончания строк Unix (LF), а не DOS (CRLF).
- Удаляйте конечные пробелы. В Linux/macOS: `find . -type f -name "*.java" -exec perl -p -i -e "s/[ \t]$//g" {} \;`.

## Структура исходного файла

Части следуют строго в этом порядке: лицензия, объявление пакета, импорты, ровно один класс верхнего уровня. Каждый раздел отделяется от следующего ровно одной пустой строкой.

### Лицензия

Если лицензия указана в корне проекта, то каждый исходный файл обязан содержать следующую лицензию в самом верху:

```java
/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```

### Импорты

Порядок групп: `java.*`, пустая строка, `javax.*` и `jakarta.*`, пустая строка, все прочие импорты, пустая строка, `org.springframework.*`, пустая строка, статические импорты остальных типов.

Статические импорты не используются в production-коде, но используются в тестах, особенно для `import static org.assertj.core.api.Assertions.assertThat;`. В production-коде они допустимы для констант (включая enum-константы), например из `java.nio.charset.StandardCharsets` или `org.springframework.core.annotation.MergedAnnotations.SearchStrategy`, а также для статических фабрик сторонних DSL - например методов
`org.junit.platform.engine.discovery.DiscoverySelectors` при использовании JUnit Platform `Launcher` API. Wildcard-импорты (`import java.util.*`, `import static org.assertj.core.api.Assertions.*`) запрещены даже в тестах.

Импорты не переносите. Внутри каждой указанной группы сортируйте импортируемые
имена в ASCII-порядке. Вложенный статический тип импортируйте обычным, а не
статическим импортом. `import module` не используйте.

```java
import java.time.Clock;
import java.util.List;

import com.example.Container.Nested;
import com.example.Order;
```

### Организация Java-файла

Элементы располагаются так:

1. статические поля;
2. обычные поля;
3. конструкторы;
4. приватные методы, вызываемые из конструкторов;
5. статические фабричные методы;
6. свойства JavaBean (геттеры и сеттеры);
7. реализации методов интерфейсов;
8. приватные или защищённые шаблонные методы, вызываемые из этих реализаций;
9. остальные методы;
10. `equals`, `hashCode`, `toString`.

Приватный или защищённый метод, вызываемый из реализации метода интерфейса, ставьте непосредственно после места использования. Иными словами, при трёх реализациях интерфейса и трёх приватных методах (по одному на реализацию) порядок должен быть «реализация → её приватный метод» три раза, а не три реализации с последующими тремя приватными методами внизу. В целом организация кода должна выглядеть естественно.

Перегруженные методы и конструкторы не разделяйте другими членами, даже если у
перегрузок различаются модификаторы.

```java
public void send(String value) {
    // ...
}

public void send(byte[] value) {
    // ...
}
```

## Форматирование

### Фигурные скобки

Непустые блоки и блокоподобные конструкции в основном используют стиль K&R («египетские скобки»): перед `{` один пробел, но нет переноса; после `{` и перед `}` есть перенос; после `}` он ставится, если скобка завершает оператор либо тело метода, конструктора или именованного класса. `else`, `catch` и `finally` размещайте на одной строке с предшествующей закрывающей скобкой `}`, отделяя одним пробелом.

```java
return new MyClass() {

    @Override
    public void method() {
        if (condition()) {
            something();
        } else {
            try {
                alternative();
            } catch (ProblemException ex) {
                recover();
            }
        }
    }
};
```

### Обязательные скобки

Всегда используйте фигурные скобки для `if`, `else`, `for`, `do` и `while`, в том
числе для пустого блока или одного оператора.

```java
if (ready) {
    start();
}
```

### Перенос строк

Предпочтительная длина строки - 90 символов. Иногда этого можно добиться небольшой переработкой кода, иногда это невозможно. Это не жёсткий лимит: 90–105 символов допустимы, если перенос ухудшает читаемость. Здесь требуется здравое суждение, единообразие и ориентация на решения в других частях кода. 105–120 символов допустимы, но не рекомендуются и должны быть редкими. Строки не должны превышать 120 символов. Для Javadoc ориентируйтесь примерно на 80 символов, чтобы его было удобно читать в разных контекстах, например на GitHub и телефоне.

При переносе длинных выражений ориентируйтесь на 90 символов и ставьте разделители в конце текущей строки, а не в начале следующей. Разделители: `,`, `+`, `?`, `:`, `&&`, `||`.

```java
if (thisLengthyMethodCall(param1, param2) &&
        anotherCheck() &&
        yetAnotherCheck()) {
    // ....
}
```

### Пустые строки

Добавляйте две пустые строки перед блоком `static {}`, списком полей, конструкторами и внутренними классами. После многострочной сигнатуры метода добавляйте одну пустую строку. Во внутренних классах дополнительные пустые строки вокруг полей и конструкторов обычно не нужны, если класс не настолько велик, что это улучшает читаемость.

```java
private Object invoke(
        FooBarOperationContext context,
        AnotherSuperLongName name
) {

    // Здесь размещается код
}
```

#### Пустые строки в методах

Вставляйте пустые строки как смысловые разделители, пример в методе:

```java
public User createUser(CreateUserRequest request) {

    log.debug("Начало работы метода createUser, request.id = {}", request.getId());
    log.trace("Начало работы метода createUser, подробности: request = {}", request);

    boolean isValid = validateRequest(request);

    if (isValid) {
        log.debug("Данные запроса корректны, request.id = {}", request.getId());
    } else {
        throw new InvalidRequestException(
                ("Данные запроса на сохранение невалидны, " +
                        "request.id = %s").formatted(request.getId()));
    }

    User user = mapToUser(request);
    user.setCreatedAt(Instant.now());

    User savedUser;
    try {
        savedUser = userRepository.save(user);

        successfulAdminNotificationService.send(savedUser, request);

        log.debug("Пользователь успешно сохранен, request.id = {}, user.id = {}",
                request.getId(), savedUser.getId()
        );

    } catch (Exception exception) {
        errorAdminNotificationService.send(exception);

        throw new UserSaveException(("Получена ошибка при сохранении данных " +
                "нового пользователя, request.id = %s").formatted(request.getId()));
    }

    notificationService.sendWelcomeEmail(savedUser);

    return savedUser;
}
```

Пустые строки разделяют следующие этапы:

* Логирование - начало работы метода.
* Валидация - проверка входных данных.
* Проверка результата - обработка результата валидации.
* Маппинг - создание и подготовка `User`.
* Сохранение - запись пользователя в БД.
* Уведомление - сообщение об успешном сохранении.
* Логирование результата - фиксация успешного сохранения.
* Обработка ошибки - уведомление и выброс исключения.
* Welcome-уведомление - отправка письма пользователю.
* Возврат результата - `return savedUser`.

#### Пустые строки в классах

Вставляйте пустые строки как смысловые разделители в структуре класса, пример:

```java
public class UserApplicationService {


    private static final int DEFAULT_BATCH_SIZE = 100;
    private static final int MAX_BATCH_SIZE = 1_000;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private UserRepository userRepository;
    private UserProfileRepository userProfileRepository;
    private UserAuditRepository userAuditRepository;

    private NotificationService notificationService;
    private UserValidationService userValidationService;
    private UserAuditService userAuditService;


    public UserApplicationService() {
        // Здесь размещается код конструктора.
    }

    public UserApplicationService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserAuditRepository userAuditRepository,
            NotificationService notificationService,
            UserValidationService userValidationService,
            UserAuditService userAuditService
    ) {

        // Здесь размещается код конструктора.
    }

    public void createUser(CreateUserRequest request) {
        // Здесь размещается код метода.
    }

    public void updateUser(
            long userId,
            UpdateUserRequest request
    ) {

        // Здесь размещается код метода.
    }

}
```

Здесь пустые строки разделяют:

* константы;
* репозитории;
* сервисы;
* группу перегруженных конструкторов;
* отдельные методы;
* сигнатуру многострочного конструктора или метода и его тело.

### Горизонтальное выравнивание

Не добавляйте переменное количество пробелов ради выравнивания соседних строк:
оно создаёт лишние изменения при правках.

```java
private int count;
private String description;
```

## Объявление класса

По возможности размещайте части `implements` и `extends` в одной строке с объявлением класса. Самые важные классы должны идти первыми.

## Именование

Имена классов, методов и переменных должны ясно передавать их назначение без необходимости изучать реализацию. Следует предпочитать простые, общеупотребительные английские слова и устоявшиеся термины предметной области. Не следует использовать редкие слова, неоднозначные сокращения и излишне абстрактные названия.

Главный концепт хорошего имени - по имени можно понять что оно делает не открывая реализацию

### Имена констант

Константы используют `CONSTANT_CASE`: все буквы прописные, слова разделены `_`. Все константы являются `static final`, но не каждое `static final`-поле - константа, поэтому этот стиль применяйте только к настоящим константам.

```java
// Константы
private static final Object NULL_HOLDER = new NullHolder();
public static final int DEFAULT_PORT = -1;

// Не константы
private static final ThreadLocal<Executor> executorHolder =
        new ThreadLocal<Executor>();
private static final Set<String> internalAnnotationAttributes = new HashSet<String>();
```

### Имена переменных

Избегайте однобуквенных имён: предпочтительнее `Method method`, а не `Method m`.

## Практики программирования

### Объявления переменных и массивов

Объявляйте по одной переменной, как можно ближе к первому использованию, обычно
сразу с инициализацией. Квадратные скобки массива относятся к типу.

```java
Order order = repository.findById(id);
String[] tags = order.getTags();
```

Не используйте `Order first, second;` и `String tags[];`. Несколько переменных
допустимы только в заголовке `for`.

### История файла

- Файл должен выглядеть как работа одного автора, а не история изменений.
- Не разделяйте искусственно элементы, которые должны быть рядом.

### Сеттеры

Новый сеттер не следует просто добавлять в конец. Размещайте его рядом со связанными методами или группой. Порядок сеттеров отражает важность, а не историю добавления, и согласуется с порядком полей.

### Тернарный оператор

Заключайте его в скобки: `return (foo != null ? foo : "default");`. Условие «не равно `null`» должно идти первым.

### `switch`

Делайте каждый `switch` исчерпывающим: перечислите все возможные значения либо
добавьте `default`. Для `switch`-выражений используйте форму `->`. Осознанный
переход между ветками старого `switch` помечайте комментарием `// продолжение в следующей ветке`.

```java
return switch (statusCode) {
    case 200 -> Status.SUCCESS;
    case 404 -> Status.NOT_FOUND;
    default -> Status.UNKNOWN;
};
```

При работе со switch+enum на случай непредвиденного поведения при неожиданном
добавлении нового enum-значения стоит добавить ветку default с явным выбросом исключения, пример:

```java
switch (enumCode) {
    case SUCCESS -> processEnumCodeSuccess(enumCode);
    case NOT_FOUND -> processEnumCodeNotFound(enumCode);
    default -> throw new RuntimeException("Неподдерживаемое значение EnumCode: %s"
            .formatted(enumCode));
}
```

### Проверки `null`

Для аргумента метода используйте `org.springframework.util.Assert.notNull`, выбрасывающий `IllegalArgumentException`; сообщение начинается с русского названия параметра с прописной первой буквы и заканчивается словами `не должен быть null` или `не должна быть null`.

```java
public void handle(Event event) {

    Assert.notNull(event, "Событие не должно быть null");
    //...
}
```

В остальных случаях используйте `Assert.state` для проверки переменной и выбрасывайте `IllegalStateException`; сообщение строится аналогично.

```java
// ...
Event event = obtainEvent();
Assert.state(event != null, "Событие не должно быть null");
// ...
```

### Null-безопасность API и полей

Null-безопасность Spring API и полей указывается аннотациями JSpecify и проверяется при сборке NullAway. В `package-info.java` устанавливайте non-null по умолчанию:

```java
@NullMarked
package org.springframework.mypackage;

import org.jspecify.annotations.NullMarked;
```

Nullable-поля, параметры и возвращаемые значения явно помечайте `@Nullable`:

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

Аннотация Spring [`@Contract`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/lang/Contract.html) может описывать аспекты поведения метода, зависящие от аргументов, которые учитывает NullAway. Например:

```java
public abstract class Assert {
    // ...

    @Contract("false, _ -> fail")
    public static void state(
            boolean expression,
            String message
    ) {

        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    // ...

    @Contract("null, _ -> fail")
    public static void notNull(
            @Nullable
            Object object,
            String message
    ) {

        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    // ...
}
```

Эти контракты позволяют NullAway понимать, что после `Assert.state(value != null, "Значение не должно быть null")` и `Assert.notNull(value, "Значение не должно быть null")` nullable-переменную `value` можно считать non-null. При переопределении переносите аннотации null-безопасности из супертиипа, если не меняете этот контракт. Для нерелевантных ошибок, ошибок NullAway или ограничений анализа используйте `@SuppressWarnings("NullAway")`.

### `@Override` и `@since`

Всегда добавляйте `@Override` методу, который переопределяет или реализует метод супертипа. `@since` не использовать.

### Перехваченные исключения

Не оставляйте `catch` пустым. Обработайте исключение, пробросьте его или поясните
комментарием, почему его можно игнорировать.

```java
try {
    return Integer.parseInt(value);
} catch (NumberFormatException ex) {
    // Нечисловое значение означает отсутствие ограничения.
    return DEFAULT_LIMIT;
}
```

### Статические члены

Если статический член не импортирован статически, обращайтесь к нему через имя
класса, а не через экземпляр.

```java
Duration timeout = Duration.ofSeconds(30);
```

### Комментарии `TODO`

Начинайте `TODO` с ссылки или идентификатора задачи и после `-` кратко указывайте
условие удаления. Не используйте имя сотрудника или команды вместо задачи.

```java
// TODO: PROJECT-123 - Удалить после перехода всех клиентов на API v2.
```

### Модификаторы и литералы

Ставьте модификаторы в порядке Java Language Specification:
`public protected private abstract default static final sealed non-sealed transient volatile synchronized native strictfp`.
Для литерала `long` используйте прописную `L`, чтобы не спутать её с `1`.

```java
private static final long MAX_TIMEOUT = 3_000_000_000L;
```

### Утилитарные классы

Класс, содержащий только статические утилитарные методы, должен иметь суффикс `Utils`, быть `abstract`, а так же использовать аннотацию `@UtilityClass`:

```java
@UtilityClass
public abstract class MyUtils {

    // Статические вспомогательные методы
}
```

### Ссылки и вывод типов

К полям класса обращайтесь без `this`, если это не создаёт неоднозначности.
Используйте `this` для разрешения конфликта имён, например в конструкторе или
сеттере. Не используйте `this` при обычном вызове методов экземпляра.

`var` для локальных переменных запрещён: используйте конкретный тип или интерфейс.
В тестах `var` разрешён при последовательном применении в тесте и желательно во
всём тестовом классе.

### Аббревиатуры и параметры типов

В camel case рассматривайте аббревиатуру как слово: `XmlHttpRequest`,
`customerId`, а не `XMLHTTPRequest`, `customerID`. Параметр типа называйте одной
прописной буквой с необязательной цифрой либо именем класса с суффиксом `T`.

```java
final class XmlHttpRequest<RequestT> {

}
```

## Javadoc

Документируйте каждый публичный класс верхнего уровня, его публичные и защищённые
члены и компоненты публичных `record`. Javadoc можно не дублировать для очевидного
аксессора или переопределения без нового контракта. Описание общего назначения
класса или метода оформляйте как Javadoc, а не как обычный комментарий.

У Javadoc есть конкретная роль: объяснить контракт и смысл элемента, не заставляя читателя разбираться в коде. Javadoc не должен переводить имя на человеческий язык и тем более пересказывать реализацию.

```java
/**
 * Найти заказ по идентификатору.
 *
 * @param id идентификатор заказа
 * @return найденный заказ
 */
public Order findOrder(long id);
```

### Форматирование

Javadoc всегда пишется на русском языке, если не указано обратное.

Следующий шаблон показывает типичное использование Javadoc для метода:

```java
/**
 * Распарсить указанный {@link Element} и зарегистрировать полученные
 * {@link BeanDefinition BeanDefinition(ы)}.
 * <p>
 * Реализации должны вернуть основное определение {@link BeanDefinition},
 * полученное в результате анализа, если они когда-либо будут использоваться
 * во вложенном виде (например, как внутренний тег в теге {@code <property/>}).
 * Реализации могут вернуть {@code null}, если они <strong>не</strong> будут
 * использоваться во вложенном виде.
 *
 * @param element       элемент, который необходимо проанализировать в
 *                      одно или несколько {@link BeanDefinition BeanDefinition}
 * @param parserContext объект, инкапсулирующий текущее состояние процесса
 *                      анализа; предоставляет доступ к
 *                      {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
 * @return основное определение {@link BeanDefinition}
 */
BeanDefinition parse(
        Element element,
        ParserContext parserContext
);
```

Для первого предложения используйте повелительный стиль: `Return`, а не `Returns`. Ставьте пустую строку между описанием и `@param`; каждый дополнительный абзац начинайте с `<p>`. Продолжения описания параметра получают дополнительный отступ и могут быть перенесены

Для Javadoc класса применяются дополнительные правила, показанные в примере:

```java
/**
 * Интерфейс, используемый {@link DefaultBeanDefinitionDocumentReader} для обработки
 * пользовательских тегов верхнего уровня (непосредственно внутри {@code <beans/>}).
 * <p>
 * Реализации могут преобразовать метаданные пользовательского тега в любое
 * необходимое количество {@link BeanDefinition BeanDefinition}.
 * <p>
 * Парсер получает {@link BeanDefinitionParser} из связанного
 * {@link NamespaceHandler} для пространства имён, в котором находится пользовательский тег.
 *
 * @see NamespaceHandler
 * @see AbstractBeanDefinitionParser
 */
```

Теги Javadoc уровня типа идут в порядке `@param`, `@see`, `@deprecated`; уровня конструктора, метода и поля - `@param`, `@return`, `@throws`, `@see`, `@deprecated`. В описании класса абзацы разделяются пустыми строками. Используйте `{@code}` для кода и значений (`null`); если тип упоминается только в `{@link}`, указывайте полное имя, чтобы избежать лишнего `import`.

## Тесты

### Фреймворк

Тесты пишутся с JUnit Jupiter (тестовый фреймворк JUnit 6). Помимо JUnit Jupiter допускаются классы модуля spring-test-*

### Именование, утверждения и моки

Имя каждого тестового класса оканчивается на `Tests`. Для утверждений используйте AssertJ. Для моков и spy-объектов - Mockito.
