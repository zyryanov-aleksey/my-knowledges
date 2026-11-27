---
agent-desc: "
Обязательные правила при написании Java-кода.

Правила применения модульной/package-by-feature архитектуры.
Правила организации бизнес-модулей и их взаимодействия.
Правила расположения классов внутри бизнес-модулей.
Правила инкапсуляции классов, конструкторов и методов.

(Стандарты написания и ревью Java-кода;
Верификация модульной архитектуры;
Контроль межмодульных зависимостей;
Контроль visibility/access modifiers;
Spring Modulith / Modular Monolith;)
"
---

# Модульная/package-by-feature архитектура

Модульная архитектура замечательно подходит для болиших приложений (более 200 классов).

Важная статья для изучения и пониманя принципов модульной архитектуры, **обязательно к прочтению**: https://phauer.com/2020/package-by-feature/

Чего необходимо придерживаться:

- Каждый модуль владеет своей бизнес-логикой, моделями и доступом к данным.
- Внутри модуля необходимо придерживаться правил package-by-layer архитектуры, за исключением компонентов, требующих package-private инкапсуляции.
- Если domain пакет сильно разросся(более 20 классов, то необходимо предложить разделить модуль на 2 части)
- Не выделять модули исключительно по наличию Entity или таблицы в БД.

## Структура package-by-feature приложения

Как должна выглядеть идеальная архитектура:

```
├── order/
│   ├── package-info.java
│   ├── OrderModuleApi.java                     # Публичный контракт модуля (либо синхронный метод, либо асинхронный listener)
│   ├── OrderModuleData.java                    # Публичная модель данных
│   ├── OrderRequstModuleData.java              # Публичная модель запроса
│   ├── OrderResponseModuleData.java            # Публичная модель ответа
│   ├── OrderModuleEventData.java               # Публичное бизнес-событие
│   ├── OrderModuleException.java               # Публичное исключение для обработки
│   ├── domain/                                 # Бизнес-ядро модуля
│   │   ├── OrderService.java
│   │   ├── OrderJdbcRepository.java
│   │   ├── OrderCrudRepository.java
│   │   ├── OrderEntity.java
│   │   ├── OrderMapper.java
│   │   ├── OrderDetailsDomain.java
│   │   ├── OrderStatusEnum.java
│   │   ├── ..BatchReader.java                       # Все что угодно связанное со spring-batch, например Job, Step, Reader, Processor, Writer, etc
│   │   └── OrderException.java
│   ├── restapi/
│   │   ├── dto/
│   │   │   └── OrderDto.java
│   │   ├── restcontroller/
│   │   │   ├── OrderRestController.java
│   │   │   └── OrderRestExceptionHandler.java
│   │   └── OrderRestApi.java
│   ├── grpchandler/
│   │   ├── OrderGrpcHandler.java
│   │   └── OrderGrpcExceptionHandler.java
│   ├── service/
│   │   └── integration/
│   │       ├── OrderIntegrationService.java
│   │       └── OrderIntegrationApi.java
│   ├── mapper/                                 # Транспортные мапперы
│   │   └── OrderRestMapper.java
│   ├── configuration/
│   │   ├── properties/
│   │   │   └── OrderProperties.java
│   │   └── OrderConfiguration.java
│   ├── scheduler/
│   │   └── OrderScheduler.java
│   └── utils/
├── ../
│   ├── ..ModuleApi.java
│   ├── ..ModuleDto.java
│   ├── ..ModuleEventDto.java
│   └── domain/
│       └── ...
├── customer/
│   ├── CustomerModuleApi.java
│   └── domain/
│       └── ...
├── shared/                           # Общие технические компоненты
├── configuration/                    # Глобальная Spring-конфигурация
└── Application.java
```


## Направление зависимостей

### Внутри модуля

```
ModuleApi ───────┐
RestController ──┤
GrpcHandler ─────┼──> Service ──┬──> Repository
Scheduler ───────┤              ├──> IntegrationService ──> IntegrationApi
Batch ───────────┘              └──> Другой ModuleApi
```

### Между модулями

```
order.domain.OrderService
    └──> payment.PaymentModuleApi
             └──> payment.domain.PaymentService
                      └──> payment.domain.PaymentCrudRepository
```

Правила:

- Межмодульные зависимости - только через публичный контракт модуля.
- Не обращаться к Service, Repository, Entity, Mapper и т.д. другого модуля напрямую.
- Не допускать циклических зависимостей между модулями.
- Entity и транспортные модели не должны пересекать границы модулей.
- Не расширять visibility компонента исключительно ради удобства вызова.

## Правила модулей и пакетов

### Application

- `@SpringBootApplication` в корне приложения.
- Дополнительные `@Enable*` и настройки — в Configuration.
- Без бизнес-логики.

### Module root / ModuleApi

- `..ModuleApi` — публичный межмодульный контракт (фасад).
- Вход/выход: публичные `..EventModuleData`, `..ModuleData`, `..ModuleException`.
- Делегировать бизнес-операции в Service.
- Не создавать Interface + Impl без необходимости.
- В корне модуля размещать только публичный контракт, его модели и бизнес-события.
- Не создавать ModuleApi, если отсутствуют внешние потребители модуля.

### domain

Бизнес-ядро модуля.

Service, Repository, Entity и внутренние Mapper размещать
в одном Java-пакете для package-private инкапсуляции.

#### Service

- `..Service` — бизнес-операции, валидации, оркестрация.
- Транзакционные границы (`@Transactional`).
- Вход/выход: Domain-объекты.
- Constructor injection, зависимости `private final`.
- Конструкторы — package-private.
- Public-методы — только для вызовов из других пакетов своего модуля.
- Не создавать Interface + Impl без необходимости.
- Не удерживать DB-транзакцию во время сетевых вызовов без обоснования.

#### Repository

- `..JdbcRepository`, `..CrudRepository` — package-private.
- SQL, JPQL, CRUD, projections.
- Возвращать Entity или внутренние read models.
- Без бизнес-логики и бизнес-транзакционных границ.
- Каждый модуль владеет своими репозиториями.
- Не переиспользовать Repository между модулями.
- Не раскрывать Repository через публичные методы Service или ModuleApi.
- Не выносить Repository в отдельный пакет, если это требует public.

#### Entity

- `..Entity` - package-private модель хранения.
- Межмодульные связи хранить через идентификаторы.

#### Domain

- `..Domain` — внутренняя бизнес-модель.
- Использовать Lombok, Builder.
- Предпочитать иммутабельные модели.
- Package-private по умолчанию.

#### Mapper

- `..Mapper` - package-private для внутренних преобразований.
- Domain ↔ Entity.
- MapStruct или ручной mapping.
- Без бизнес-логики, I/O и side effects.

#### Const / Enum

- `..Enum`, `..Const` — минимально необходимая visibility.
- Локальные константы — private static final в классе-владельце.
- Не хранить конфигурацию окружения.

#### Exception

- `..Exception` — именовать по причине ошибки.

### restapi

- `..RestApi` — mappings, OpenAPI, транспортный контракт.
- `..Dto` — предпочтительно record + Bean Validation.
- `..RestController` — mapping, вызов Service, формирование ответа.
- `..RestExceptionHandler` — конвертация Exception в HTTP response.
- Контроллеры и обработчики — package-private.
- Никакой бизнес-логики и транзакций.

### grpchandler

- `..GrpcHandler` — mapping Protobuf в Domain, вызов Service.
- `..GrpcExceptionHandler` — конвертация Exception в gRPC Status.
- Минимально необходимая visibility.
- Никакой бизнес-логики и транзакций.

### service/integration

- `..IntegrationApi` — технический контракт внешней системы, package-private, если позволяет framework.
- `..IntegrationService` — адаптация внешнего контракта, mapping, error handling.
- Использовать IntegrationApi только через IntegrationService.
- Timeout, retry, idempotency.
- Не раскрывать внешние транспортные модели за пределами адаптера.

### mapper

- Транспортные преобразования.
- Dto ↔ Domain.
- Protobuf ↔ Domain.
- Не обращаться к Entity и Repository.
- Без бизнес-логики, I/O и side effects.

### configuration / configuration/properties

- Bean definitions, `@Enable*`, инфраструктурные настройки.
- `@ConfigurationProperties` + validation.
- Configuration — package-private по умолчанию.
- Настройки модуля хранить внутри модуля.
- Не хардкодить параметры окружения и секреты.
- Без бизнес-логики.

### scheduler

- Обращение из `@Scheduled` метода в Service своего модуля.
- Cron/interval — в Properties.
- Учитывать concurrency, distributed locking, idempotency.

### utils

- Stateless технические функции.
- Без бизнес-логики и зависимостей от компонентов приложения.
- Не выносить локальные функции в utils без необходимости.

### shared

- Только действительно общие технические компоненты.
- Без бизнес-логики отдельных модулей.
- Без общих бизнес-репозиториев, Entity и Service.
- Shared не должен зависеть от бизнес-модулей.
- Не выносить компонент в shared только из-за похожего кода.

## Visibility / Access modifiers

Инкапсуляция на уровне Java обязательна.

### Правила visibility

| Компонент          | Класс/Interface | Конструктор     | Методы              |
|--------------------|-----------------|-----------------|---------------------|
| ModuleApi          | public          | package-private | public / private    |
| Service            | package-private*| package-private | package-private*    |
| CrudRepository     | package-private | —               | public (interface)  |
| JdbcRepository     | package-private | package-private | package-private     |
| Entity             | package-private | protected (JPA) | minimum necessary   |
| Internal Domain    | package-private*| minimum necessary| minimum necessary  |
| Internal Mapper    | package-private | package-private | package-private*    |
| RestController     | package-private | package-private | package-private*    |
| RestApi            | public*         | —               | public (interface)  |
| Dto                | package-private*| minimum necessary| —                  |
| GrpcHandler        | package-private*| package-private | public*             |
| IntegrationService | public*         | package-private | public*             |
| IntegrationApi     | package-private*| —               | public (interface)  |
| Configuration      | package-private | package-private | package-private*    |
| ExceptionHandler   | package-private | package-private | package-private*    |
| Scheduler          | package-private | package-private | package-private*    |

* Расширять visibility только при необходимости межпакетного доступа
  или согласно требованиям framework.

## Владение данными

- Каждый модуль владеет своими данными и операциями их изменения.
- Не изменять данные другого модуля напрямую через SQL/JPA.
- При одинаковых именах Spring-компонентов учитывать конфликты bean names, JPA entity names и qualifiers.
- Общие схемы БД не отменяют границы владения данными.

## Межмодульное взаимодействие

- Синхронные бизнес-операции — через ModuleApi.
- Реакция на бизнес-факты — через события при необходимости.
- События именовать по произошедшему факту:`OrderCreatedModuleEventData`, `PaymentCompletedModuleEventData`.

## Spring Modulith / Architecture verification

Обязательно изучить документацию Spring Modulith: https://docs.spring.io/spring-modulith/reference/fundamentals.html

- Обязательно именовать модули с помощью `@ApplicationModule(...)`
- Не использовать allowedDependencies у `@ApplicationModule(...)`

## How-to: добавление новой функциональности в package-by-feature приложение

1. Определить бизнес-модуль — владелец функциональности.
2. Найти существующие бизнес-операции, Domain-модели и ModuleApi.
3. Реализовать бизнес-операцию в Service внутри пакета domain.
4. Добавить Repository, Entity, Domain и Mapper при необходимости.
5. При взаимодействии с внешней системой использовать IntegrationService + IntegrationApi.
6. При взаимодействии с другим модулем использовать его ModuleApi или бизнес-события.
7. При необходимости расширить публичный контракт своего модуля: ModuleApi, ModuleData, ModuleEventData, ModuleException.
8. Реализовать входной адаптер (REST, gRPC, Scheduler, Batch) и транспортный mapping.
9. Проверить visibility классов, конструкторов, методов и полей.
10. Проверить направление зависимостей, владение данными и транзакционные границы.
11. Добавить релевантные тесты и запустить архитектурную верификацию Spring Modulith.
12. Если domain содержит более 20 классов — предложить разделение модуля по бизнес-ответственности.