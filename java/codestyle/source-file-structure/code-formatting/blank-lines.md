---
agent-desc: "
Как правильно разделять код на смысловые блоки при написании java-кода.
Как поддерживать легкое чтение кода. 
Как правильно разделять код на смысловые блоки для повышения читаемости кода. 
Использование смысловых разделителей в коде.
Как правильно форматировать основные java-конструкции.

(
Обязательные правила форматирования java-кода;
Стандарты и правила написания java-кода;
Стандарты и правила ревью java-кода;
Рекомендации для соблюдения стилистики java-кода;
)
"
---

# Пустые строки

Правильный код должен читаться по-диагонали, то есть читающий должен сразу понять
где находятся основные логические части кода.

Для этого очень важно разбивать код на подобные блоки, например:
- разбиение основных элементов класса на "смысловые блоки", подробнее про основные элементы класса - [java-file-organisation.md](/java/codestyle/source-file-structure/java-file-organisation.md)
- разбиение строк в коде на "смысловые блоки", где каждый "смысловой блок" отвечает за одну понятную задачу, операцию или группирует поля по определенному назначению


## Пустые строки в методах

Правила для поддержки хорошей читаемости кода внутри метода:

- разделять объявление метода и его тело
- разделять основные этапы алгоритма
- разделять независимые операции/задачи
- разделять подготовку и использование данных
- разделять проверки и основную логику
- разделять обработку и возврат результата
- разделять логирование с основной логикой
- разделять код внутри управляющих конструкций (`if`, `for`, `while`, `try`, `catch`) по правилам описанным выше

Пример с основными правилами приведен далее:

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

Пустые строки разделяют следующие этапы/смысловые блоки:

* Объявление метода - разделение сигнатуры метода и тела метода
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

## Пустые строки в классах

- разделять объявление класса и его содержимое
- разделять основные элементы класса (константы, поля, конструкторы, методы, вложенные классы)
- разделять группы полей по их назначению (репозитории, сервисы и т. д.)
- разделять группы констант по их назначению
- разделять группы логически связанных методов
- разделять отдельные методы и конструкторы
- разделять сигнатуру многострочного метода или конструктора и его тело
- разделять вложенные классы и другие элементы класса

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
    
    
    static class InnerClass {
        
    }

}
```

Здесь пустые строки разделяют различные смысловые блоки:

* константы;
* репозитории;
* сервисы;
* группу перегруженных конструкторов;
* отдельные методы;
* сигнатуру многострочного конструктора или метода и его тело.