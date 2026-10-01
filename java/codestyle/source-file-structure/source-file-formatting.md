# Форматирование

## Фигурные скобки

Правильное форматирование фигурной скобки:

- перед `{` один пробел, но нет переноса
- после `{` и перед `}` есть перенос
- после `}` пробел ставится, если скобка завершает оператор либо тело метода, конструктора или именованного класса. `else`, `catch` и `finally` размещайте на одной строке с предшествующей закрывающей скобкой `}`, отделяя одним пробелом

Пример корректного использования:

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

## Обязательные скобки

Всегда используйте фигурные скобки для `if`, `else`, `for`, `do` и `while`, в том числе для пустого блока или одного оператора.

Пример корректного использования:
```java
// один оператор
    if (ready) {
        start();
    }
    
// пустой блок
    if (ready) {
    }
```

## Перенос строк

Для понимания как правильно переносить строки требуется здравое суждение, единообразие и ориентация на решения в других частях кода.

- Предпочтительная длина строки - 90 символов (Иногда этого можно добиться небольшой переработкой кода, иногда это невозможно).
- Жесткий лимит - 100 символов.
- Для Javadoc ориентируйтесь примерно на 80 символов, чтобы его было удобно читать в разных контекстах, например на GitHub и телефоне.

При переносе длинных выражений ориентируйтесь на 90 символов и ставьте разделители в конце текущей строки, а не в начале следующей. Разделители: `,`, `+`, `?`, `:`, `&&`, `||`.

```java
if (thisLengthyMethodCall(param1, param2) &&
        anotherCheck() &&
        yetAnotherCheck()) {
        // ....
}
```

## Пустые строки

Добавляйте 2 пустые строки перед:

- блоком `static {}`
- списком полей
- конструкторами 
- внутренними классами

После многострочной сигнатуры метода добавляйте одну пустую строку, пример:

```java
private Object invoke(
        FooBarOperationContext context,
        AnotherSuperLongName name
) {

    // Здесь размещается код
}
```

### Пустые строки в методах

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

### Пустые строки в классах

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

## Горизонтальное выравнивание

Не добавляйте переменное количество пробелов ради выравнивания соседних строк:
оно создаёт лишние изменения при правках.

Пример:

```java
// Правильно:
private int count;
private String description;

// Неправильно:
private int     count2;
private String  description2;
```

## Форматирование исходного кода в среде разработки

При разработке в таких инструментах как IntelliJ Idea рекомендуется пользоваться автоматическим форматированием кода. 

Если в проекте/приложении нет своего .editorconfig, то рекомендуется его добавить.

Образец, который вы можете скопировать к себе, соответствующий стилю из данного руководства - [.editorconfig](/.editorconfig)