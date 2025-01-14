package ru.tinkoff.qa.neptune.spring.web.testclient;

import org.springframework.test.web.reactive.server.StatusAssertions;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.tinkoff.qa.neptune.core.api.localization.LocalizationByResourceBundle;
import ru.tinkoff.qa.neptune.core.api.localization.StepLocalization;

import java.util.Locale;

import static org.apache.commons.lang3.LocaleUtils.toLocale;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static ru.tinkoff.qa.neptune.core.api.properties.general.localization.DefaultLocaleProperty.DEFAULT_LOCALE_PROPERTY;
import static ru.tinkoff.qa.neptune.core.api.properties.general.localization.DefaultLocalizationEngine.DEFAULT_LOCALIZATION_ENGINE;
import static ru.tinkoff.qa.neptune.spring.web.testclient.DescribedConsumerBuilder.describeConsumer;
import static ru.tinkoff.qa.neptune.spring.web.testclient.LogWebTestClientExpectation.logExpectation;
import static ru.tinkoff.qa.neptune.spring.web.testclient.SendRequestAction.send;
import static ru.tinkoff.qa.neptune.spring.web.testclient.WebTestClientContext.webTestClient;

public class LogWebTestClientExpectationTest extends BaseTest {

    private final static Locale RUSSIAN = toLocale("ru_RU");

    @DataProvider
    public static Object[][] data() {
        return new Object[][]{
                {null, null, "Response status. 200"},
                {RUSSIAN, LocalizationByResourceBundle.class, "Статус ответа. 200"},
        };
    }

    @DataProvider
    public static Object[][] data2() {
        return new Object[][]{
                {null, null, "Response header(s). 'Fake-Header'. Does not exist"},
                {RUSSIAN, LocalizationByResourceBundle.class, "Заголовок(ки) ответа. 'Fake-Header'. Не существует"},
        };
    }

    @DataProvider
    public static Object[][] data3() {
        return new Object[][]{
                {null, null, "Response header(s). 'Test-Header'. Value is {1,2,3}"},
                {RUSSIAN, LocalizationByResourceBundle.class, "Заголовок(ки) ответа. 'Test-Header'. Значение {1,2,3}"},
        };
    }

    @DataProvider
    public static Object[][] data4() {
        return new Object[][]{
                {null, null, "Body. Json path 'some.path'. Arguments '{1,2,3,4}'. Some description"},
                {RUSSIAN, LocalizationByResourceBundle.class, "Тело ответа. Json path 'some.path'. Аргументы '{1,2,3,4}'. Some description"},
        };
    }

    @DataProvider
    public static Object[][] data5() {
        return new Object[][]{
                {null, null, "Body as json-string is equal to or contains same key-value pairs as '{}'"},
                {RUSSIAN, LocalizationByResourceBundle.class, "json-строка тела ответа равна или содержит те же пары ключ-значение, что и '{}'"},
        };
    }

    @Test(dataProvider = "data")
    public void test(Locale l,
                     Class<StepLocalization> localizationClass,
                     String expected) {

        DEFAULT_LOCALE_PROPERTY.accept(l);
        DEFAULT_LOCALIZATION_ENGINE.accept(localizationClass);

        var send = send(client, WebTestClient::get)
                .expectStatus(StatusAssertions::isOk);

        webTestClient(send);
        var expectation = send.assertions.getFirst();

        expectation.verify(responseSpec);
        assertThat(logExpectation(expectation).toString(), is(expected));
    }

    @Test(dataProvider = "data2")
    public void test2(Locale l,
                      Class<StepLocalization> localizationClass,
                      String expected) {

        DEFAULT_LOCALE_PROPERTY.accept(l);
        DEFAULT_LOCALIZATION_ENGINE.accept(localizationClass);

        var send = send(client, WebTestClient::get)
                .expectHeader(headerAssertions -> headerAssertions.doesNotExist("Fake-Header"));

        webTestClient(send);
        var expectation = send.assertions.getFirst();
        expectation.verify(responseSpec);
        assertThat(logExpectation(expectation).toString(), is(expected));
    }

    @Test(dataProvider = "data3")
    public void test3(Locale l,
                      Class<StepLocalization> localizationClass,
                      String expected) {

        DEFAULT_LOCALE_PROPERTY.accept(l);
        DEFAULT_LOCALIZATION_ENGINE.accept(localizationClass);

        var send = send(client, WebTestClient::get)
                .expectHeader(headerAssertions -> headerAssertions.valueEquals("Test-Header", "1", "2", "3"));

        webTestClient(send);
        var expectation = send.assertions.getFirst();

        expectation.verify(responseSpec);
        assertThat(logExpectation(expectation).toString(), is(expected));
    }

    @Test(dataProvider = "data4")
    public void test4(Locale l,
                      Class<StepLocalization> localizationClass,
                      String expected) {

        DEFAULT_LOCALE_PROPERTY.accept(l);
        DEFAULT_LOCALIZATION_ENGINE.accept(localizationClass);

        var send = send(client, WebTestClient::get).expectBodyJsonPath("some.path",
                jsonPathAssertions -> jsonPathAssertions.value(describeConsumer("Some description")
                        .of(Integer.class)
                        .consume(integer -> {
                        })
                        .get()),
                "1", "2", "3", "4");

        try {
            webTestClient(send);
        } catch (AssertionError ignored) {

        }

        var expectation = send.assertions.getFirst();

        expectation.verify(responseSpec);
        assertThat(logExpectation(expectation).toString(), is(expected));
    }

    @Test(dataProvider = "data5")
    public void test5(Locale l,
                      Class<StepLocalization> localizationClass,
                      String expected) {


        DEFAULT_LOCALE_PROPERTY.accept(l);
        DEFAULT_LOCALIZATION_ENGINE.accept(localizationClass);

        var expectation = send(client, WebTestClient::get)
                .expectBodyJson("{}")
                .assertions.getFirst();

        expectation.verify(responseSpec);
        assertThat(logExpectation(expectation).toString(), is(expected));
    }

    @AfterMethod
    public void afterMethod() {
        DEFAULT_LOCALE_PROPERTY.accept(null);
        DEFAULT_LOCALIZATION_ENGINE.accept(null);
    }
}
