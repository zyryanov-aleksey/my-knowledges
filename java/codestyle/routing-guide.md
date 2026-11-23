---
agent-desc: "
Как правильно форматировать Java код.
Правила для корректного ревью кода.
Правила для понятного форматирования.
Правила для повышения человеко-читаемости кода.
Навигация по остальным правилам форматирования при написании/рефакторинге кода.
Стандарты форматирования исходного кода.

(Стандарты и правила написания java-кода)
"
---

# Правила написания кода

## Основы

### Кодировка

Все исходные файлы должны быть написаны с использованием `UTF-8` кодировки.

### Отступы

Рекомендуется использование пробелов, а не табуляции.

### Структура исходного файла

- лицензия (при необходимости)
- объявление пакета
- импорты
- класс

### Работа в IntelliJ Idea

#### Автоматическое форматирование в IntelliJ Idea

Общепринятый [.editorconfig](/.editorconfig) для IntelliJ Idea находится в корне базы знаний. 
Рекомендуется поддерживать его в актуальном виде.

#### Автоматическая инспекция кода

Автоматическая инспекция кода в IntelliJ Idea осуществляется с помощью файла: [code-inspections.xml](/code-inspections.xml)

## Навигация по обязательным правилам написания кода

### Структура исходного файла

- [Оформление лицензии](/java/codestyle/source-file-structure/license-guide.md)
- [Группировка и порядок импортов](/java/codestyle/source-file-structure/imports-structure.md)
- [Порядок полей, конструкторов и методов в классе](/java/codestyle/source-file-structure/java-file-organisation.md)
- [Объявление классов](/java/codestyle/source-file-structure/class-declaration.md)

### Форматирование

- [Общие правила форматирования](/java/codestyle/source-file-structure/code-formatting/source-file-formatting.md)
- [Разделение кода пустыми строками](/java/codestyle/source-file-structure/code-formatting/blank-lines.md)
- [Расстановка фигурных скобок](/java/codestyle/source-file-structure/code-formatting/curly-braces-usage.md)
- [Перенос длинных строк](/java/codestyle/source-file-structure/code-formatting/line-wrapping.md)

### Именование

- [Общие правила выбора имён](/java/codestyle/source-file-structure/naming-rules/global-naming-rules.md)
- [Имена классов](/java/codestyle/source-file-structure/naming-rules/class-naming-rules.md)
- [Имена методов](/java/codestyle/source-file-structure/naming-rules/method-naming-rules.md)
- [Имена полей, констант, параметров и локальных переменных](/java/codestyle/source-file-structure/naming-rules/field-naming-rules.md)

### Объявления и языковые конструкции

- [Объявление переменных](/java/codestyle/programming-practices/core/variable-declarations.md)
- [Выбор между `var` и явным типом](/java/codestyle/programming-practices/core/var-usage.md)
- [Объявление массивов и коллекций](/java/codestyle/programming-practices/core/array-declarations.md)
- [Использование `this` и `super`](/java/codestyle/programming-practices/core/this-and-super-usage.md)
- [Использование `switch` и `case`](/java/codestyle/programming-practices/core/switch-case-practices.md)
- [Выбор между тернарным оператором и `if-else`](/java/codestyle/programming-practices/core/ternary-operator.md)

### Практики программирования

- [Обработка исключений](/java/codestyle/programming-practices/core/exception-handling.md)
- [Проверки на `null` и аннотации nullability](/java/codestyle/programming-practices/nullsafety/nullsafety.md)
- [Логирование](/java/codestyle/programming-practices/logging.md)
- [Утилитарные классы](/java/codestyle/programming-practices/utility-classes.md)

### Комментарии и документация

- [Документирование кода с помощью Javadoc](/java/codestyle/programming-practices/comments-and-docs/javadoc-core.md)
- [Оформление комментариев `TODO`](/java/codestyle/programming-practices/comments-and-docs/todo-comments.md)