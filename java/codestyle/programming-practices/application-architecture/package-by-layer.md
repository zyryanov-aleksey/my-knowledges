---
agent-desc: "
Обязательные правила при написании java кода.

Правила применения слоистой/package-by-layer архитектурой.
Правила работы слоистой/package-by-layer архитектурой.
Как написать приложение со слоистой/package-by-layer архитектурой
Как правильно расположить класс в слоистой/package-by-layer архитектуре.
Как правильно вносить изменения и решать задачи в рамках слоистой/package-by-layer архитектуры.

(Стандарты и правила написания java-кода;
Стандарты и правила ревью java-кода;
Рекомендации для соблюдения стилистики java-кода;
Основные практики при написании java-кода;
)
"
---

# Слоеная/слоистая/package-by-layer архитектура

Подходит для небольших приложений и модулей.
При разрастании пакета более чем на 10 классов рассмотреть переход на package-by-feature.

Не перестраивать существующую архитектуру проекта. Не создавать пакеты, слои и абстракции заранее.

## Структура package-by-layer приложения

Как даолжна выглядеть идеальная архитектура:

```
├── rest-api/                    # REST-контракт и Dto
│   ├── dto                      # Dto
│   │   └── ..Dto.java
│   ├── rest-controller/         # REST-контроллеры, обработчики ошибок
│   │   ├── ..RestController.java
│   │   └── ..RestExceptionHandler.java
│   └── ..RestApi.java           # REST-контракт
├── grpc-handler/                # gRPC-контроллеры/хендлеры, обработчики ошибок
│   ├── ..GrpcHandler.java
│   └── ..GrpcExceptionHandler.java
├── service/                     # сервисы с бизнес-логикой (бизнес операции, валидации, оркестрации, и т.п.)
│   ├── integration/             # интеграционные сервисы
│   │   ├── ..IntegrationService.java
│   │   └── ..IntegrationApi.java
│   └── ..Service.java
├── repository/                  # Доступ к БД, репозитории
│   ├── ..JdbcRepository.java
│   └── ..CrudRepository.java 
├── data/
│   ├── domain/                  # Внутренняя модель
│   │   └── ..Domain.java    
│   ├── entity/                  # Модель хранения
│   │   └── ..Entity.java 
│   └── const/                   # Константы, енамы
│       └── ..Enum.java
├── mapper/                      # Преобразования Dto, protobuf и внутренних моделей
│   └── ..Mapper.java
├── configuration/               # Spring-конфигурация и настройки
│   ├── properties/              # настройки переменных окружения
│   │   └── ..Properties.java    
│   └── ..Configuration.java     
├── scheduler/                   # Запуск задач по расписанию
│   └── ..Scheduler.java
├── batch/                       # Задачи Spring Batch
├── exception/                   # Исключения приложения
│   └── ..Exception.java
├── utils/                       # Вспомогательные функции
│   └── ..Util.java
└── ..Application.java
```

## Направление зависимостей

```
RestController ──┐
GrpcHandler ─────┤
Scheduler ───────┼──> Service ──┬──> Repository
Batch ───────────┘              └──> IntegrationService ──> IntegrationApi
```

Правила:

- Batch Reader/Writer может использовать Repository напрямую.
- Entity и транспортные модели не должны выходить дальше Service

## Правила пакетов

### Application

- `@SpringBootApplication` в корне приложения.
- Дополнительные `@Enable*` и настройки - в Configuration.

### rest-api

- `..RestApi` - mappings, OpenAPI, транспортный контракт.
- `..Dto` - предпочтительно record + Bean Validation.
- `..RestController` - mapping, вызов Service, формирование ответа.
- `..RestExceptionHandler` - конвертация Exception в HTTP response.
- Никакой бизнес-логики и транзакций.

### grpc-handler

- `..GrpcHandler` - mapping Protobuf в Domain, транспортный контракт, вызов Service
- `..GrpcExceptionHandler` - конвертация Exception в gRPC Status.
- Никакой бизнес-логики

### service

- Бизнес-операции, валидации, оркестрацияи тому подобное.
- Транзакционные границы (`@Transactional`).
- Вход/выход: Domain-объекты.
- Constructor injection, зависимости `private final`.
- Не создавать Interface + Impl без необходимости.
- Не удерживать DB-транзакцию во время сетевых вызовов без обоснования.

### service/integration

- `..IntegrationApi` - технический контракт внешней системы, нельзя использовать без `..IntegrationService`.
- `..IntegrationService` - адаптация внешнего контракта, mapping, error handling.
- Timeout, retry, idempotency.
- `..IntegrationApi` скрывать внутри пакета integration.

### repository

- SQL, JPQL, CRUD, projections.
- Возвращает Entity или внутренние read models.
- Без бизнес-логики и бизнес-транзакционных границ.

### data/domain

- Внутренние модели `..Domain` - используются для работы внутри `..Service`.
- Без Spring, JPA, Jackson, Protobuf.
- Использовать Lombok, Builder

### data/entity

- Модель хранения `..Entity`.

### data/const

- Только общие константы и enum `..Enum` или `..Const`.
- Локальные константы - `private static final` в классе-владельце.
- Не хранить конфигурацию окружения.

### mapper

- Конвертация/маппинг данных - `..Mapper`
- Dto ↔ Domain.
- Protobuf ↔ Domain.
- Domain ↔ Entity.
- MapStruct или ручной mapping.
- Без бизнес-логики, без I/O и без side effects.

### configuration

- Bean definitions, `@Enable*`, инфраструктурные настройки.
- Группировать по ответственности.
- Без бизнес-логики.

### configuration/properties

- `@ConfigurationProperties` + validation.
- Не хардкодить параметры окружения и секреты.

### scheduler

- Обращение из `@Scheduled` метода в Service.
- Cron/interval - в Properties.
- Учитывать concurrency, distributed locking, idempotency.

### batch

- Job, Step, Reader, Processor, Writer, Listener.
- Бизнес-операции - в Service+Domain.
- Учитывать chunk, retry, skip, restart и transaction boundaries.

### exception

- Именовать по причине ошибки, не по HTTP/gRPC status.
- Сохранять cause.
- Преобразовывать в транспортные ошибки через ExceptionHandler.

### utils

- Stateless технические функции.
- Без бизнес-логики и зависимостей от компонентов приложения.

## Visibility / Access modifiers

- Использовать минимально необходимую видимость.
- Spring-компоненты и их конструкторы — package-private,
  если не требуется доступ из других пакетов.
- Public — только межпакетные контракты.
- IntegrationApi — package-private, если позволяет framework.
- Учитывать ограничения Spring AOP и JPA.

Public не означает разрешённую архитектурную зависимость.

## How-to: добавление новой функциональности в package-by-layer приложение

1. Найти существующий бизнес-контракт и компоненты.
2. Определить Domain и бизнес-операцию в Service.
3. Добавить Repository/Integration при необходимости.
4. Реализовать mapping и входной адаптер.
5. Проверить зависимости, visibility и транзакции.
6. Добавить релевантные тесты.
