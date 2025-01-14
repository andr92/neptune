package ru.tinkoff.qa.neptune.retrofit2.tests;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.tinkoff.qa.neptune.retrofit2.service.setup.ApiService;
import ru.tinkoff.qa.neptune.retrofit2.steps.ExpectedHttpResponseHasNotBeenReceivedException;
import ru.tinkoff.qa.neptune.retrofit2.tests.retrofit.suppliers.GsonRetrofitBuilderSupplier;
import ru.tinkoff.qa.neptune.retrofit2.tests.services.common.CallService;
import wiremock.org.eclipse.jetty.util.StringUtil;

import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.lang.System.currentTimeMillis;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.AssertJUnit.fail;
import static ru.tinkoff.qa.neptune.core.api.dependency.injection.DependencyInjector.injectValues;
import static ru.tinkoff.qa.neptune.retrofit2.RetrofitContext.retrofit;
import static ru.tinkoff.qa.neptune.retrofit2.properties.DefaultRetrofitProperty.DEFAULT_RETROFIT_PROPERTY;
import static ru.tinkoff.qa.neptune.retrofit2.properties.DefaultRetrofitURLProperty.DEFAULT_RETROFIT_URL_PROPERTY;
import static ru.tinkoff.qa.neptune.retrofit2.steps.GetArraySupplier.callArray;
import static ru.tinkoff.qa.neptune.retrofit2.steps.GetIterableSupplier.callIterable;
import static ru.tinkoff.qa.neptune.retrofit2.steps.GetObjectFromArraySupplier.callArrayItem;
import static ru.tinkoff.qa.neptune.retrofit2.steps.GetObjectFromIterableSupplier.callIterableItem;
import static ru.tinkoff.qa.neptune.retrofit2.steps.GetObjectSupplier.callBody;
import static ru.tinkoff.qa.neptune.retrofit2.steps.GetObjectSupplier.callObject;

public class HttpBodyDataTestFromCall extends BaseBodyDataTest {

    private static final String LOCALHOST = "127.0.0.1";
    private static WireMockServer wireMockServer;
    @ApiService
    private CallService callService;

    @BeforeClass
    public static void preparation() {
        wireMockServer = new WireMockServer(options().port(8089));
        wireMockServer.start();
        configureFor(LOCALHOST, 8089);
        prepareMock();
    }

    @AfterClass
    public static void tearDown() {
        wireMockServer.stop();
    }

    @BeforeClass
    public void beforeClass() throws Exception {
        DEFAULT_RETROFIT_URL_PROPERTY.accept(new URL("http://" + LOCALHOST + ":8089/"));
        DEFAULT_RETROFIT_PROPERTY.accept(GsonRetrofitBuilderSupplier.class);
        injectValues(this);
    }

    @Test
    public void objectFromBodyTest1() {
        var result = retrofit().get(callBody(() -> callService.getJson())
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("Size == 2", r -> r.size() == 2));

        assertThat(result, hasSize(2));
    }

    @Test
    public void objectFromBodyTest2() {
        var result = retrofit().get(callBody(() -> callService.getJson())
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("Size > 2", r -> r.size() > 2));

        assertThat(result, nullValue());
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void objectFromBodyTest3() {
        try {
            retrofit().get(callBody(() -> callService.getJson())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size > 2", r -> r.size() > 2)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), nullValue());
            throw e;
        }

        fail("Exception was expected");
    }


    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void objectFromBodyTest4() {
        try {
            retrofit().get(callBody(() -> callService.getXml())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size == 2", r -> r.size() == 2)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), instanceOf(RuntimeException.class));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test
    public void objectFromBodyTest5() {
        var start = currentTimeMillis();
        retrofit().get(callBody(() -> callService.getJson())
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("Size > 2", r -> r.size() > 2)
                .retryTimeOut(ofSeconds(5))
                .pollingInterval(ofMillis(500)));

        var stop = currentTimeMillis();
        var time = stop - start;
        assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
        assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void objectFromBodyTest6() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callBody(() -> callService.getJson())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size > 2", r -> r.size() > 2)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void objectFromBodyTest7() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callBody(() -> callService.getXml())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size == 2", r -> r.size() == 2)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }


    @Test
    public void calculatedObjectFromBodyTest1() {
        var result = retrofit().get(callObject(
                "Values of string fields",
                () -> callService.getJson(),
                dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                .criteria("Size == 2", r -> r.size() == 2));

        assertThat(result, hasSize(2));
    }

    @Test
    public void calculatedObjectFromBodyTest2() {
        var result = retrofit().get(callObject(
                "Values of string fields",
                () -> callService.getJson(),
                dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                .criteria("Size > 2", r -> r.size() > 2));

        assertThat(result, nullValue());
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedObjectFromBodyTest3() {
        try {
            retrofit().get(callObject(
                    "Values of string fields",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size > 2", r -> r.size() > 2)
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), nullValue());
            throw e;
        }

        fail("Exception was expected");
    }


    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedObjectFromBodyTest4() {
        try {
            retrofit().get(callObject(
                    "Values of string fields",
                    () -> callService.getXml(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .criteria("Size == 2", r -> r.size() == 2)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), instanceOf(RuntimeException.class));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedObjectFromBodyTest5() {
        try {
            retrofit().get(callObject(
                    "Values of string fields",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size > 2", dtoObjects -> dtoObjects.size() > 2)
                    .criteria("Size == 2", r -> r.size() == 2)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), nullValue());
            throw e;
        }

        fail("Exception was expected");
    }

    @Test
    public void calculatedObjectFromBodyTest6() {
        var start = currentTimeMillis();
        retrofit().get(callObject(
                "Values of string fields",
                () -> callService.getJson(),
                dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("Size > 2", r -> r.size() > 2)
                .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                .retryTimeOut(ofSeconds(5))
                .pollingInterval(ofMillis(500)));

        var stop = currentTimeMillis();
        var time = stop - start;
        assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
        assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedObjectFromBodyTest7() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callObject(
                    "Values of string fields",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .criteria("Size > 2", r -> r.size() > 2)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedObjectFromBodyTest8() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callObject(
                    "Values of string fields",
                    () -> callService.getXml(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .criteria("Size == 2", r -> r.size() == 2)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedObjectFromBodyTest9() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callObject(
                    "Values of string fields",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size > 2", dtoObjects -> dtoObjects.size() > 2)
                    .criteria("Size == 2", r -> r.size() == 2)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }


    @Test
    public void getIterableTest1() {
        var result = retrofit().get(callIterable("Result list",
                () -> callService.getJson())
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("Size of 'object' == 2", r -> r.getObject().size() == 2));

        assertThat(result, hasSize(2));
    }

    @Test
    public void getIterableTest2() {
        var result = retrofit().get(callIterable("Result list",
                () -> callService.getJson())
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("Size of 'object' > 2", r -> r.getObject().size() > 2));

        assertThat(result, nullValue());
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void getIterableTest3() {
        try {
            retrofit().get(callIterable("Result list",
                    () -> callService.getJson())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size of 'object' > 2", r -> r.getObject().size() > 2)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), nullValue());
            throw e;
        }

        fail("Exception was expected");
    }


    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void getIterableTest4() {
        try {
            retrofit().get(callIterable("Result list",
                    () -> callService.getXml())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size of 'object' == 2", r -> r.getObject().size() == 2)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), instanceOf(RuntimeException.class));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test
    public void getIterableTest5() {
        var start = currentTimeMillis();
        retrofit().get(callIterable("Result list",
                () -> callService.getJson())
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("Size of 'object' > 2", r -> r.getObject().size() > 2)
                .retryTimeOut(ofSeconds(5))
                .pollingInterval(ofMillis(500)));

        var stop = currentTimeMillis();
        var time = stop - start;
        assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
        assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void getIterableTest6() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callIterable("Result list",
                    () -> callService.getJson())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size of 'object' > 2", r -> r.getObject().size() > 2)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void getIterableTest7() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callIterable("Result list",
                    () -> callService.getXml())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size of 'object' == 2", r -> r.getObject().size() == 2)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }


    @Test
    public void calculatedIterableTest1() {
        var result = retrofit().get(callIterable(
                "Values of string fields",
                () -> callService.getJson(),
                dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                .criteria("Not a blank string", StringUtil::isNotBlank));

        assertThat(result, hasSize(2));
    }

    @Test
    public void calculatedIterableTest2() {
        var result = retrofit().get(callIterable(
                "Values of string fields",
                () -> callService.getJson(),
                dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                .criteria("is a blank string", StringUtil::isBlank));

        assertThat(result, nullValue());
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedIterableTest3() {
        try {
            retrofit().get(callIterable(
                    "Values of string fields",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("is a blank string", StringUtil::isBlank)
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), nullValue());
            throw e;
        }

        fail("Exception was expected");
    }


    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedIterableTest4() {
        try {
            retrofit().get(callIterable(
                    "Values of string fields",
                    () -> callService.getXml(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .criteria("Not a blank string", StringUtil::isNotBlank)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), instanceOf(RuntimeException.class));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedIterableTest5() {
        try {
            retrofit().get(callIterable(
                    "Values of string fields",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size > 2", dtoObjects -> dtoObjects.size() > 2)
                    .criteria("Not a blank string", StringUtil::isNotBlank)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), nullValue());
            throw e;
        }

        fail("Exception was expected");
    }

    @Test
    public void calculatedIterableTest6() {
        var start = currentTimeMillis();
        retrofit().get(callIterable(
                "Values of string fields",
                () -> callService.getJson(),
                dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("is a blank string", StringUtil::isBlank)
                .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                .retryTimeOut(ofSeconds(5))
                .pollingInterval(ofMillis(500)));

        var stop = currentTimeMillis();
        var time = stop - start;
        assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
        assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedIterableTest7() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callIterable(
                    "Values of string fields",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .criteria("is a blank string", StringUtil::isBlank)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedIterableTest8() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callIterable(
                    "Values of string fields",
                    () -> callService.getXml(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .criteria("Not a blank string", StringUtil::isNotBlank)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedIterableTest9() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callIterable(
                    "Values of string fields",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size > 2", dtoObjects -> dtoObjects.size() > 2)
                    .criteria("Not a blank string", StringUtil::isNotBlank)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }


    @Test
    public void getArrayTest1() {
        var result = retrofit().get(callArray("Result array",
                () -> callService.getJsonArray())
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("Size of 'object' == 2", r -> r.getObject().size() == 2));

        assertThat(result, arrayWithSize(2));
    }

    @Test
    public void getArrayTest2() {
        var result = retrofit().get(callArray("Result array",
                () -> callService.getJsonArray())
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("Size of 'object' > 2", r -> r.getObject().size() > 2));

        assertThat(result, nullValue());
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void getArrayTest3() {
        try {
            retrofit().get(callArray("Result array",
                    () -> callService.getJsonArray())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size of 'object' > 2", r -> r.getObject().size() > 2)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), nullValue());
            throw e;
        }

        fail("Exception was expected");
    }


    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void getArrayTest4() {
        try {
            retrofit().get(callArray("Result array",
                    () -> callService.getXmlArray())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size of 'object' == 2", r -> r.getObject().size() == 2)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), instanceOf(RuntimeException.class));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test
    public void getArrayTest5() {
        var start = currentTimeMillis();
        retrofit().get(callArray("Result array",
                () -> callService.getJsonArray())
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("Size of 'object' > 2", r -> r.getObject().size() > 2)
                .retryTimeOut(ofSeconds(5))
                .pollingInterval(ofMillis(500)));

        var stop = currentTimeMillis();
        var time = stop - start;
        assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
        assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void getArrayTest6() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callArray("Result array",
                    () -> callService.getJsonArray())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size of 'object' > 2", r -> r.getObject().size() > 2)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void getArrayTest7() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callArray("Result array",
                    () -> callService.getXmlArray())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size of 'object' == 2", r -> r.getObject().size() == 2)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }


    @Test
    public void calculatedArrayTest1() {
        var result = retrofit().get(callArray(
                "Values of string fields",
                () -> callService.getJson(),
                dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()).toArray(new String[]{}))
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                .criteria("Not a blank string", StringUtil::isNotBlank));

        assertThat(result, arrayWithSize(2));
    }

    @Test
    public void calculatedArrayTest2() {
        var result = retrofit().get(callArray(
                "Values of string fields",
                () -> callService.getJson(),
                dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()).toArray(new String[]{}))
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                .criteria("is a blank string", StringUtil::isBlank));

        assertThat(result, nullValue());
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedArrayTest3() {
        try {
            retrofit().get(callArray(
                    "Values of string fields",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()).toArray(new String[]{}))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("is a blank string", StringUtil::isBlank)
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), nullValue());
            throw e;
        }

        fail("Exception was expected");
    }


    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedArrayTest4() {
        try {
            retrofit().get(callArray(
                    "Values of string fields",
                    () -> callService.getXml(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()).toArray(new String[]{}))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .criteria("Not a blank string", StringUtil::isNotBlank)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), instanceOf(RuntimeException.class));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedArrayTest5() {
        try {
            retrofit().get(callArray(
                    "Values of string fields",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()).toArray(new String[]{}))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size > 2", dtoObjects -> dtoObjects.size() > 2)
                    .criteria("Not a blank string", StringUtil::isNotBlank)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), nullValue());
            throw e;
        }

        fail("Exception was expected");
    }

    @Test
    public void calculatedArrayTest6() {
        var start = currentTimeMillis();
        retrofit().get(callArray(
                "Values of string fields",
                () -> callService.getJson(),
                dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()).toArray(new String[]{}))
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("is a blank string", StringUtil::isBlank)
                .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                .retryTimeOut(ofSeconds(5))
                .pollingInterval(ofMillis(500)));

        var stop = currentTimeMillis();
        var time = stop - start;
        assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
        assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedArrayTest7() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callArray(
                    "Values of string fields",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()).toArray(new String[]{}))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .criteria("is a blank string", StringUtil::isBlank)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedArrayTest8() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callArray(
                    "Values of string fields",
                    () -> callService.getXml(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()).toArray(new String[]{}))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .criteria("Not a blank string", StringUtil::isNotBlank)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedArrayTest9() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callArray(
                    "Values of string fields",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()).toArray(new String[]{}))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size > 2", dtoObjects -> dtoObjects.size() > 2)
                    .criteria("Not a blank string", StringUtil::isNotBlank)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }


    @Test
    public void getFromIterableTest1() {
        var result = retrofit().get(callIterableItem("Result",
                () -> callService.getJson())
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("Size of 'object' == 2", r -> r.getObject().size() == 2));

        assertThat(result, not(nullValue()));
    }

    @Test
    public void getFromIterableTest2() {
        var result = retrofit().get(callIterableItem("Result",
                () -> callService.getJson())
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("Size of 'object' > 2", r -> r.getObject().size() > 2));

        assertThat(result, nullValue());
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void getFromIterableTest3() {
        try {
            retrofit().get(callIterableItem("Result",
                    () -> callService.getJson())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size of 'object' > 2", r -> r.getObject().size() > 2)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), nullValue());
            throw e;
        }

        fail("Exception was expected");
    }


    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void getFromIterableTest4() {
        try {
            retrofit().get(callIterableItem("Result",
                    () -> callService.getXml())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size of 'object' == 2", r -> r.getObject().size() == 2)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), instanceOf(RuntimeException.class));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test
    public void getFromIterableTest5() {
        var start = currentTimeMillis();
        retrofit().get(callIterableItem("Result",
                () -> callService.getJson())
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("Size of 'object' > 2", r -> r.getObject().size() > 2)
                .retryTimeOut(ofSeconds(5))
                .pollingInterval(ofMillis(500)));

        var stop = currentTimeMillis();
        var time = stop - start;
        assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
        assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void getFromIterableTest6() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callIterableItem("Result",
                    () -> callService.getJson())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size of 'object' > 2", r -> r.getObject().size() > 2)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void getFromIterableTest7() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callIterableItem("Result",
                    () -> callService.getXml())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size of 'object' == 2", r -> r.getObject().size() == 2)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }


    @Test
    public void calculatedFromIterableTest1() {
        var result = retrofit().get(callIterableItem(
                "Value of string field",
                () -> callService.getJson(),
                dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                .criteria("Not a blank string", StringUtil::isNotBlank));

        assertThat(result, not(nullValue()));
    }

    @Test
    public void calculatedFromIterableTest2() {
        var result = retrofit().get(callIterableItem(
                "Value of string field",
                () -> callService.getJson(),
                dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                .criteria("is a blank string", StringUtil::isBlank));

        assertThat(result, nullValue());
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedFromIterableTest3() {
        try {
            retrofit().get(callIterableItem(
                    "Value of string field",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("is a blank string", StringUtil::isBlank)
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), nullValue());
            throw e;
        }

        fail("Exception was expected");
    }


    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedFromIterableTest4() {
        try {
            retrofit().get(callIterableItem(
                    "Value of string field",
                    () -> callService.getXml(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .criteria("Not a blank string", StringUtil::isNotBlank)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), instanceOf(RuntimeException.class));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedFromIterableTest5() {
        try {
            retrofit().get(callIterableItem(
                    "Value of string field",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size > 2", dtoObjects -> dtoObjects.size() > 2)
                    .criteria("Not a blank string", StringUtil::isNotBlank)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), nullValue());
            throw e;
        }

        fail("Exception was expected");
    }

    @Test
    public void calculatedFromIterableTest6() {
        var start = currentTimeMillis();
        retrofit().get(callIterableItem(
                "Value of string field",
                () -> callService.getJson(),
                dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("is a blank string", StringUtil::isBlank)
                .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                .retryTimeOut(ofSeconds(5))
                .pollingInterval(ofMillis(500)));

        var stop = currentTimeMillis();
        var time = stop - start;
        assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
        assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedFromIterableTest7() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callIterableItem(
                    "Value of string field",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .criteria("is a blank string", StringUtil::isBlank)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedFromIterableTest8() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callIterableItem(
                    "Value of string field",
                    () -> callService.getXml(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .criteria("Not a blank string", StringUtil::isNotBlank)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedFromIterableTest9() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callIterableItem(
                    "Value of string field",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size > 2", dtoObjects -> dtoObjects.size() > 2)
                    .criteria("Not a blank string", StringUtil::isNotBlank)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }


    @Test
    public void getArrayItemTest1() {
        var result = retrofit().get(callArrayItem("Result",
                () -> callService.getJsonArray())
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("Size of 'object' == 2", r -> r.getObject().size() == 2));

        assertThat(result, not(nullValue()));
    }

    @Test
    public void getArrayItemTest2() {
        var result = retrofit().get(callArrayItem("Result",
                () -> callService.getJsonArray())
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("Size of 'object' > 2", r -> r.getObject().size() > 2));

        assertThat(result, nullValue());
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void getArrayItemTest3() {
        try {
            retrofit().get(callArrayItem("Result",
                    () -> callService.getJsonArray())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size of 'object' > 2", r -> r.getObject().size() > 2)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), nullValue());
            throw e;
        }

        fail("Exception was expected");
    }


    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void getArrayItemTest4() {
        try {
            retrofit().get(callArrayItem("Result",
                    () -> callService.getXmlArray())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size of 'object' == 2", r -> r.getObject().size() == 2)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), instanceOf(RuntimeException.class));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test
    public void getArrayItemTest5() {
        var start = currentTimeMillis();
        retrofit().get(callArrayItem("Result",
                () -> callService.getJsonArray())
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("Size of 'object' > 2", r -> r.getObject().size() > 2)
                .retryTimeOut(ofSeconds(5))
                .pollingInterval(ofMillis(500)));

        var stop = currentTimeMillis();
        var time = stop - start;
        assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
        assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void getArrayItemTest6() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callArrayItem("Result",
                    () -> callService.getJsonArray())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size of 'object' > 2", r -> r.getObject().size() > 2)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void getArrayItemTest7() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callArrayItem("Result",
                    () -> callService.getXmlArray())
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("Size of 'object' == 2", r -> r.getObject().size() == 2)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }


    @Test
    public void calculatedArrayItemTest1() {
        var result = retrofit().get(callArrayItem(
                "Value of string field",
                () -> callService.getJson(),
                dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()).toArray(new String[]{}))
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                .criteria("Not a blank string", StringUtil::isNotBlank));

        assertThat(result, not(nullValue()));
    }

    @Test
    public void calculatedArrayItemTest2() {
        var result = retrofit().get(callArrayItem(
                "Value of string field",
                () -> callService.getJson(),
                dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()).toArray(new String[]{}))
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                .criteria("is a blank string", StringUtil::isBlank));

        assertThat(result, nullValue());
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedArrayItemTest3() {
        try {
            retrofit().get(callArrayItem(
                    "Value of string field",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()).toArray(new String[]{}))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .criteria("is a blank string", StringUtil::isBlank)
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), nullValue());
            throw e;
        }

        fail("Exception was expected");
    }


    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedArrayItemTest4() {
        try {
            retrofit().get(callArrayItem(
                    "Value of string field",
                    () -> callService.getXml(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()).toArray(new String[]{}))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .criteria("Not a blank string", StringUtil::isNotBlank)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), instanceOf(RuntimeException.class));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedArrayItemTest5() {
        try {
            retrofit().get(callArrayItem(
                    "Value of string field",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()).toArray(new String[]{}))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size > 2", dtoObjects -> dtoObjects.size() > 2)
                    .criteria("Not a blank string", StringUtil::isNotBlank)
                    .throwOnNoResult());
        } catch (Exception e) {
            assertThat(e.getCause(), nullValue());
            throw e;
        }

        fail("Exception was expected");
    }

    @Test
    public void calculatedArrayItemTest6() {
        var start = currentTimeMillis();
        retrofit().get(callArrayItem(
                "Value of string field",
                () -> callService.getJson(),
                dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()).toArray(new String[]{}))
                .responseStatusCodeIs(200)
                .responseHeaderValueIs("custom header", "true")
                .responseHeaderValueMatches("custom header", "Some")
                .responseMessageIs("Successful json")
                .responseMessageMatches("Successful")
                .criteria("is a blank string", StringUtil::isBlank)
                .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                .retryTimeOut(ofSeconds(5))
                .pollingInterval(ofMillis(500)));

        var stop = currentTimeMillis();
        var time = stop - start;
        assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
        assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedArrayItemTest7() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callArrayItem(
                    "Value of string field",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()).toArray(new String[]{}))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .criteria("is a blank string", StringUtil::isBlank)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedArrayItemTest8() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callArrayItem(
                    "Value of string field",
                    () -> callService.getXml(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()).toArray(new String[]{}))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size == 2", dtoObjects -> dtoObjects.size() == 2)
                    .criteria("Not a blank string", StringUtil::isNotBlank)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }

    @Test(expectedExceptions = ExpectedHttpResponseHasNotBeenReceivedException.class)
    public void calculatedArrayItemTest9() {
        var start = currentTimeMillis();
        try {
            retrofit().get(callArrayItem(
                    "Value of string field",
                    () -> callService.getJson(),
                    dtoObjects -> dtoObjects.stream().map(DtoObject::getString).collect(toList()).toArray(new String[]{}))
                    .responseStatusCodeIs(200)
                    .responseHeaderValueIs("custom header", "true")
                    .responseHeaderValueMatches("custom header", "Some")
                    .responseMessageIs("Successful json")
                    .responseMessageMatches("Successful")
                    .callBodyCriteria("Body size > 2", dtoObjects -> dtoObjects.size() > 2)
                    .criteria("Not a blank string", StringUtil::isNotBlank)
                    .retryTimeOut(ofSeconds(5))
                    .pollingInterval(ofMillis(500))
                    .throwOnNoResult());
        } catch (Exception e) {
            var stop = currentTimeMillis();
            var time = stop - start;
            assertThat(time, lessThanOrEqualTo(ofSeconds(5).toMillis() + 850));
            assertThat(time, greaterThanOrEqualTo(ofSeconds(5).toMillis()));
            throw e;
        }

        fail("Exception was expected");
    }


    @AfterClass
    public void afterClass() {
        DEFAULT_RETROFIT_URL_PROPERTY.accept(null);
        DEFAULT_RETROFIT_PROPERTY.accept(null);
    }
}
