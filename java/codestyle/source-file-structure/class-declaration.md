---
agent-desc: "
Обязательные правила объявления классов, вложенных классов.

Использовать при создании, изменении и ревью классов - при использовании extends и implements, переносе длинного объявления, при использовании static nested class, настройке его видимости и расположения внутри исходного файла).

Как правильно объявить внутренний класс.
Как правильно перечислить все классы и интерфейсы от которых наследуется класс
"
---

# Правила объявления класса

## Наследование

`extends` и `implements` лучше всего располагать в одной строке с объявлением. 
Если интерфейсов несколько, то надо разместить их друг за другом по-смыслу: от самого важного до вспомогательного.

Корректный пример:

```java
public class UserServiceImpl extends BaseService implements UserSalaryService, Serializable {
}
```

Если строка объявления становится слишком длинной, то стоит перенести интерфейсы и класс на новую строку.

Корректный пример:

```java
public class UserServiceImpl 
        extends BaseService 
        implements UserSalaryService, UserValidator, UserProvider {
}
```

## Вложенные классы

- `static nested class` предпочтительнее `обычного inner class`. 
- Вложенный класс необходимо размещать после основных полей, конструкторов, методов - ближе к концу класса, про структуру класса [читать тут](/java/codestyle/source-file-structure/java-file-organisation.md). 
- Если вложенный класс используется только как деталь реализации - делать его `private`.

Корректный пример объявления вложенного класса:

```java
public final class UserService {

    // fields
    // constructors
    // methods

    private static class InnerHelper {
    }
}
```