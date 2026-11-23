---
agent-desc: "
Обязательные правила при написании java кода.

Как правильно логировать события
Где и когда использовать логирование
Логирование основных компонентов программы
Когда и как правильно использовать уровни логирования
Использование log: error, warn, info, debug, trace
Обязательные правила логирования в: rest controllers, grpc handlers, services, utils, listeners, event handles, schedulers

(
Стандарты и правила написания java-кода;
Стандарты и правила ревью java-кода;
Рекомендации для соблюдения стилистики java-кода;
Основные практики при написании java-кода;
)
"
---

# Правила логирования

Логирование должно:

- позволять восстановить ход выполнения операции
- понять причины отклонений
- диагностировать ошибки без повторного воспроизведения проблемы.

При этом логи не должны создавать избыточный шум, раскрывать чувствительные данные или существенно влиять на производительность приложения.

Основные правила при написании логов:

- Основной инструмент для логирования: `SLF4J` - если не указан иной
- Сообщения должны быть понятными и содержать необходимый контекст, логов без параметров быть не должно.
- Использовать идентификаторы запросов (requestId, executionId, runId, etc) и бизнес-сущностей (orderId, paymentId).
- Использовать параметризованные сообщения: `log.info("Начало работы createOrder: orderId={}", orderId)`
- "одна ошибка - один ответственный уровень логирования" - иначе в Java-приложениях с архитектурой Controller-Service-Repository одно исключение часто порождает три или четыре одинаковых ERROR-лога

## Описания уровней логирования

### Trace:

Зачем и когда использовать:

- использовать для логирования персональных данных
- подразумевается, что это очень подробный лог, имеющий тысячи сообщений за одну активность - используется для получения подробной технической информации
- использовать для отображения важной информации которую нельзя отобразить в log.info или log.debug

### Debug:

Зачем и когда использовать:

- использовать везде где нельзя поставить log.info
- для отображения подробной информации о начале и конце каждого бизнес-процесса
- для отображения важных ветвлений бизнес-процесса
- для логирования всех важных промежуточных точек бизнес-процесса
- запуск периодических задач (schedulers)
- конец и начало сервисного метода, утилитарного метода (там где неуместен log.info)

### Info:

Лог который сообщает что приложение живет и работает. Таких логов не должно быть много, только самые важные части бизнес процесса.

Зачем и когда использовать:

- для получения информации о начале работы важных процессов
- использовать если запущен новыйх бизнес процесс или закончен текущий

### Warn:

Зачем и когда использовать:

- Произошло что-то неожиданное, но система может продолжить работу.
- Использование fallback-механизмов.
- Произошло что то странное, но не критичное (не приведшее к окончательному сбою)

### Error:

Зачем и когда использовать:

- Ошибка, из-за которой операция не может быть успешно завершена.
- Необработанные исключения.
- Исчерпаны попытки повторного выполнения операции.
- Критические ошибки компонентов системы.
- Логировать исключение со stack trace **всегда**.

## Примеры как правильно и неправильно использовать синтаксис логирования:

```java
void exampleMethod() {
    // ПРАВИЛЬНО: есть контекст
    log.info("Начало обработки заказа: orderId={}", orderId);

    // ПРАВИЛЬНО: несколько параметров
    log.debug("Изменение статуса заказа: orderId={}, oldStatus={}, newStatus={}",
            orderId, oldStatus, newStatus);

    // ПРАВИЛЬНО: логирование всего объекта на trace-уровне
    log.trace("Запрос: {}", request);

    // НЕПРАВИЛЬНО: нет контекста
    log.info("Начало обработки");

    // НЕПРАВИЛЬНО: конкатенация строк
    log.info("Начало обработки заказа: " + orderId);

    // НЕПРАВИЛЬНО: логирование всего объекта на info-уровне
    log.info("Запрос: {}", request);

    // НЕПРАВИЛЬНО: дублирование ошибки при повторном пробросе
    try {
        // smth work
    } catch (Exception e) {
        log.error("Ошибка обработки заказа", e);
        throw e;
    }
}
```

## Примеры где и как правильно логировать

### Правильное логирование controller/grpc handler/listener или любого другого интеграционного обработчика:

```java
@PostMapping("/orders/{orderId}/process")
public ResponseEntity<Void> processOrder(
        OrderDto orderDto
) {
    log.info("Получен запрос на обработку заказа: orderId={}", orderDto.getId());
    log.trace("Получен запрос на обработку заказа, подробности: order={}", orderDto)

    orderService.processOrder(orderId);

    log.info("Запрос зарегистрирован успешно, orderId={}", orderDto.getId())
    return ResponseEntity.ok().build();
}

@KafkaListener(topics = "order-events")
public void handle(OrderEventDto event) {
    log.info("Получено событие: eventId={}, orderId={}", event.getId(), event.getOrderId());
    log.trace("Получено событие, начало обработки, подробности: event={}", event);

    if (eventService.isProcessed(event.getId())) {
        log.debug("Повторное событие пропущено: eventId={}", event.getId());
        return;
    }
    
    eventService.process(event);

    log.info("Событие успешно обработано: eventId={}, orderId={}", event.getId(), event.getOrderId());
}
```

### Правильное логирование на примере Service:

```java
public void processOrder(UUID orderId) {
    log.debug("Начало обработки заказа: orderId={}", orderId);

    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

    log.debug("Заказ найден: orderId={}, status={}",
            orderId, order.getStatus());

    if (order.isAlreadyProcessed()) {
        log.debug("Заказ уже обработан, повторная обработка не требуется: orderId={}",
                orderId);
        return;
    }

    if (order.isExpired()) {
        log.warn("Обнаружен просроченный заказ: orderId={}", orderId);
        order.setStatus(OrderStatus.CANCELLED);
    } else {
        log.debug("Выбрана стандартная обработка заказа: orderId={}", orderId);
        order.setStatus(OrderStatus.PROCESSED);
    }

    orderRepository.save(order);

    log.debug("Обработка заказа завершена: orderId={}, status={}",
            orderId, order.getStatus());
}
```

### Правильное логирование в обработчике исключений:

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * javadoc
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Void> handleNotFound(OrderNotFoundException e) {
        log.warn("Заказ не найден: orderId={}", e.getOrderId());
        return ResponseEntity.notFound().build();
    }

    /**
     * javadoc
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleUnexpected(Exception e) {
        log.error("Непредвиденная ошибка обработки HTTP-запроса", e);
        return ResponseEntity.internalServerError().build();
    }

}
```

### Правильное логирование в Scheduler:

```java
@Scheduled(scheduler = EXTRACTION_TASK_SCHEDULER_NAME,
           fixedRateString = "#{templateExtractionSchedulingProperties.fixedRate}",
           initialDelayString = "#{templateExtractionSchedulingProperties.initialDelay}")
public void startTemplateExtraction() {
    UUID runId = UUID.randomUUID();
    log.debug("Начат периодический запуск экстракции: runId={}", runId);
    try {
        extractionService.extractReadyTemplates(runId, OffsetDateTime.now(clock));
        log.debug("Периодическая экстракция завершена успешно, runId={}", runId)
    } catch (RuntimeException exception) {
        log.warn("Экстракция отложена до следующего сигнала планировщика: runId={}",
                runId,
                exception);
    }
}
```