---
agent-desc: "
Как правильно форматировать Java код.
Правила для корректного ревью кода.
Правила для понятного форматирования.
Правила для повышения человеко-читаемости кода.
Навигация по остальным правилам форматирования при написании/рефакторинге кода.
Стандарты форматирования исходного кода.
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

- Как объявлять лицензию: [license-guide.md](/java/codestyle/source-file-structure/license-guide.md)
- Как правильно использовать импорты: [imports-structure.md](/java/codestyle/source-file-structure/imports-structure.md)
- Как правильно организовать Java-файл: [java-file-organisation.md](/java/codestyle/source-file-structure/java-file-organisation.md)
- Форматирование исходного кода: [source-file-formatting.md](/java/codestyle/source-file-structure/source-file-formatting.md)
- Правила объявления классов: [class-declaration.md](source-file-structure/class-declaration.md)
- Правила именования классов/методов/переменных/etc: [global-naming-rules.md](/java/codestyle/source-file-structure/naming-rules/global-naming-rules.md)