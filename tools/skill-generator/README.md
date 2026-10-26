# Собрать Agent Skills из заметок

Требуется JDK 21. Gradle Wrapper загружает Gradle 8.14.5 и проверяет SHA-256
дистрибутива. Версии Java-зависимостей закреплены в `build.gradle.kts`.

## Настроить выборку

Измените `skills-config.yaml` в корне репозитория. Единственный параметр
`ignore` содержит пути относительно корня:

- `notes/private.md` исключает конкретный файл.
- `notes/private/` исключает каталог со всем содержимым.
- `.gitignore` не влияет на генератор.
- Каждый неигнорируемый `.md` становится отдельным навыком, включая `AGENTS.md`.
- Корневой `SKILL.md` исключён явно.
- Неигнорируемый файл другого формата копируется только при наличии ссылки
  из заметки. Каждый использующий его навык получает собственную копию.

Оставьте `build/` и `tools/skill-generator/` в `ignore`, чтобы результаты и
документация генератора не участвовали в выборке. Символические ссылки
не обходятся; ссылка из заметки на символическую ссылку вызывает ошибку.

## Выполнить сборку

Из корня репозитория:

```bash
tools/skill-generator/gradlew -p tools/skill-generator check generateSkills
tools/skill-generator/gradlew -p tools/skill-generator validateSkills
tools/skill-generator/gradlew -p tools/skill-generator packageSkills
```

`generateSkills` каждый раз удаляет прежний набор, отчёт и архив. Исходные
файлы не изменяются. `validateSkills` и `packageSkills` работают с уже
сгенерированным набором, поэтому можно проверить его внешним валидатором
до упаковки. При вызове `packageSkills` проверка локальных ссылок обязательна.

Результаты:

- `build/skills/<name>/SKILL.md`: frontmatter с `name` и `description`, затем
  исходный текст с заменёнными адресами локальных ссылок.
- `build/skills/<name>/assets/`: побайтовые копии ресурсов с сохранением
  исходного пути от корня репозитория.
- `build/skills/manifest.json`: исходные пути, имена, описания и SHA-256
  исходных Markdown-файлов.
- `build/skills-report.json`: состояния `ignored`, `skill`, `resource`,
  `unused`, число навыков и предупреждения. Исключённый каталог представлен
  одной записью с завершающим `/`; его содержимое не перечисляется.
- `build/distributions/skills.zip` и `skills.zip.sha256`: архив набора
  и контрольная сумма. Файлы отсортированы, время записей фиксировано.

Распакуйте все каталоги навыков рядом. Между ними работают относительные
ссылки `../<name>/SKILL.md`, в том числе взаимные и циклические.

## Проверить спецификацию Agent Skills

Pipeline использует официальный [skills-ref](https://github.com/agentskills/agentskills/tree/main/skills-ref)
из ревизии `69ef37e9424c0a7ea9dd2293b559e43ec8176379`. Для такой же локальной
проверки нужны Git, Python 3.12 и uv 0.11.28:

```bash
git clone https://github.com/agentskills/agentskills.git build/skills-ref-source
git -C build/skills-ref-source checkout 69ef37e9424c0a7ea9dd2293b559e43ec8176379
uv sync --locked --no-dev --project build/skills-ref-source/skills-ref
python3 tools/skill-generator/scripts/validate_skills.py build/skills \
  --validator build/skills-ref-source/skills-ref/.venv/bin/skills-ref
```

Скрипт вызывает `skills-ref validate` отдельно для каждого навыка.
`validateSkills` дополнительно проверяет существование всех локальных целей
в готовом наборе без обращения к исходным заметкам.

## Исправить ошибку сборки

Диагностика ссылки содержит категорию, исходный файл, строку и адрес:

- `IGNORED_TARGET`: уберите ссылку или измените `ignore`.
- `MISSING_TARGET`: восстановите файл или исправьте путь.
- `UNSUPPORTED_TARGET`: укажите обычный файл вместо каталога, символической
  ссылки или другой файловой сущности.
- `OUTSIDE_ROOT`: переместите цель в репозиторий или исправьте путь.
- `EMPTY_DESCRIPTION`: добавьте непустой H1 или текст вне блока кода.
- `NAME_COLLISION`: разные пути получили одно имя, генерация прервана.

Пути с `/` в начале разрешаются от корня репозитория, остальные от каталога
заметки. Query и fragment сохраняются. Обрабатываются обычные ссылки,
изображения и reference-style определения. Внешние URL, внутренние якоря
и содержимое блоков кода сохраняются. HTML-ссылки не входят в обработку
Markdown-ссылок.

Рекомендации [Agent Skills](https://agentskills.io/specification) по размеру
инструкций дают предупреждение при 500 строках или приблизительно 5000
токенах, оцениваемых как 20 000 Unicode-символов. Текст не сокращается.

## Изменить получение описания

Код утилиты находится в одном пакете `io.github.myknowledges.skills`:

```text
DescriptionProvider
HeadingDescriptionProvider
MarkdownSupport
SkillGenerator
BundleValidator
ReproducibleArchive
SkillGeneratorApplication
```

`HeadingDescriptionProvider` использует первый H1, иначе первую непустую
текстовую строку вне кода. Markdown-разметка удаляется только из описания,
его длина ограничивается 1024 Unicode-символами. Для будущей интеграции с LLM
реализуйте `DescriptionProvider` и передайте реализацию в `SkillGenerator`.

Имя строится из пути без `.md`: ASCII lowercase, разделители-дефисы,
префикс до 51 символа и первые 12 hex-символов SHA-256 полного пути с
расширением. Для пустого префикса используется `skill`.

## Выпустить набор

GitHub Actions запускает тесты и сборку при pull request и push в `master`.
Для выпуска создайте и отправьте тег вида `skills-v1.0.0`. После проверок
отдельная job с `contents: write` публикует ZIP и SHA-256 в GitHub Release.
Обычная сборка имеет только `contents: read`.

Если под тем же тегом уже опубликованы одинаковые файлы, публикация
пропускается. Отличающиеся байты архива или контрольной суммы вызывают ошибку.
При незавершённой загрузке скрипт добавляет отсутствующие файлы после
сравнения уже существующих; опубликованные файлы не перезаписываются.

Проверить сценарии публикации без доступа к GitHub:

```bash
python3 -m unittest discover -s tools/skill-generator/scripts -p 'test_*.py'
```
