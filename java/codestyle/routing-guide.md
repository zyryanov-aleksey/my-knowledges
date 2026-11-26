---
agent-desc: "
Навигация по обязательным правилам написания и ревью кода на Java.
Структура исходных файлов, форматирование, именование, языковые конструкции и документация.
Организация пакетов в слоистой и модульной архитектуре.
"
---

# Правила написания кода на Java

## Навигация по обязательным правилам

### Архитектура приложения

* [Слоистая архитектура](/java/codestyle/programming-practices/application-architecture/package-by-layer.md) описывает организацию пакетов по техническим слоям и направление зависимостей между ними.
* [Модульная архитектура](/java/codestyle/programming-practices/application-architecture/packge-by-feature.md) описывает деление приложения на модули по функциям предметной области, публичные контракты модулей и правила их взаимодействия.

### Структура исходного файла

Порядок элементов исходного файла включает лицензию при необходимости, объявление пакета, импорты и класс.

* [Оформление лицензии](/java/codestyle/source-file-structure/license-guide.md)
* [Группировка и порядок импортов](/java/codestyle/source-file-structure/imports-structure.md)
* [Порядок полей, конструкторов и методов в классе](/java/codestyle/source-file-structure/java-file-organisation.md)
* [Объявление классов](/java/codestyle/source-file-structure/class-declaration.md)

### Форматирование

* [Общие правила форматирования](/java/codestyle/source-file-structure/code-formatting/source-file-formatting.md)
* [Разделение кода пустыми строками](/java/codestyle/source-file-structure/code-formatting/blank-lines.md)
* [Расстановка фигурных скобок](/java/codestyle/source-file-structure/code-formatting/curly-braces-usage.md)
* [Перенос длинных строк](/java/codestyle/source-file-structure/code-formatting/line-wrapping.md)

### Именование

* [Общие правила выбора имён](/java/codestyle/source-file-structure/naming-rules/global-naming-rules.md)
* [Имена классов](/java/codestyle/source-file-structure/naming-rules/class-naming-rules.md)
* [Имена методов](/java/codestyle/source-file-structure/naming-rules/method-naming-rules.md)
* [Имена полей, констант, параметров и локальных переменных](/java/codestyle/source-file-structure/naming-rules/field-naming-rules.md)

### Объявления и языковые конструкции

* [Объявление переменных](/java/codestyle/programming-practices/core/variable-declarations.md)
* [Выбор между `var` и явным типом](/java/codestyle/programming-practices/core/var-usage.md)
* [Объявление массивов и коллекций](/java/codestyle/programming-practices/core/array-declarations.md)
* [Использование `this` и `super`](/java/codestyle/programming-practices/core/this-and-super-usage.md)
* [Использование `switch` и `case`](/java/codestyle/programming-practices/core/switch-case-practices.md)
* [Выбор между тернарным оператором и `if-else`](/java/codestyle/programming-practices/core/ternary-operator.md)

### Практики программирования

* [Обработка исключений](/java/codestyle/programming-practices/core/exception-handling.md)
* [Проверки на `null` и аннотации nullability](/java/codestyle/programming-practices/nullsafety/nullsafety.md)
* [Логирование](/java/codestyle/programming-practices/logging.md)
* [Утилитарные классы](/java/codestyle/programming-practices/utility-classes.md)

### Комментарии и документация

* [Документирование кода с помощью Javadoc](/java/codestyle/programming-practices/comments-and-docs/javadoc-core.md)
* [Оформление комментариев `TODO`](/java/codestyle/programming-practices/comments-and-docs/todo-comments.md)
