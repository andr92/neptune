# Внедрение зависимостей

Neptune имеет свой собственный механизм внедрения зависимостей.

Тестовый класс, как и любой класс, может содержать поля, которые могут быть заполнены в ходе выполнения `@Before*`-методов.
Но бывает так, что механизм / логика / условия заполнения некоторых из этих полей всегда одни и те же, что ведет к 
дублированию кода в `@Before*`-методах или чрезмерному использованию механизма наследования. 

Чтобы этого избежать, можно поступить следующим образом:

- создать класс

```java
package org.my.pack;

import ru.tinkoff.qa.neptune.core.api.dependency.injection.DependencyInjector;

import java.lang.reflect.Field;

public class MyDependencyInjector implements DependencyInjector {

    @Override //прежде чем этот метод вызывается, происходит
    //проверка на то, чтобы поле не было статическим и финальным,
    //так же заполнению подлежат поля, значения которых в данный момент == null,
    //либо поля примитивных типов (хотя данный механизм внедрения зависимостей для полей примитивных типов
    // реализовывать не рекомендуется)
    public boolean toSet(Field field) {
        return //проверяется дополнительное условие, что можно заполнять поле;
    }

    @Override
    public Object getValueToSet(Field field) {
        return //если поле заполнять можно, то тогда формируется значение для этого поля,
        //используя аннотации над полем или какую-либо другую метаинформацию (java.reflection) данного поля ;
    }
}
```

- Настроить [SPI](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html). В `resources` директорий `main` или `test`
  создать следующие директории и файлы

```
    META-INF
    |
    +---- services
             |
             +----ru.tinkoff.qa.neptune.core.api.dependency.injection.DependencyInjector
```

- В файл `ru.tinkoff.qa.neptune.core.api.dependency.injection.DependencyInjector` внести запись
  `org.my.pack.MyDependencyInjector`

Данный механизм используют:
- [TestNG](./../../../testng.integration/doc/rus/README.MD#Внедрение-зависимостей)
- [JUnit5](./../../../jupiter.integration/doc/rus/README.MD#Внедрение-зависимостей)

[к списку документов](README.MD#Оглавление)