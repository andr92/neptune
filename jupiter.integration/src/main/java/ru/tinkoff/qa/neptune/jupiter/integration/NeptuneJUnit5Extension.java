package ru.tinkoff.qa.neptune.jupiter.integration;

import org.junit.jupiter.api.extension.*;
import ru.tinkoff.qa.neptune.core.api.cleaning.ContextRefreshable;
import ru.tinkoff.qa.neptune.core.api.hooks.ExecutionHook;

import java.lang.reflect.Method;
import java.util.List;

import static ru.tinkoff.qa.neptune.core.api.cleaning.ContextRefreshable.REFRESHABLE_CONTEXTS;
import static ru.tinkoff.qa.neptune.core.api.dependency.injection.DependencyInjector.injectValues;
import static ru.tinkoff.qa.neptune.core.api.hooks.ExecutionHook.getHooks;
import static ru.tinkoff.qa.neptune.jupiter.integration.properties.Junit5RefreshStrategyProperty.*;


public final class NeptuneJUnit5Extension implements BeforeAllCallback, TestInstancePostProcessor,
        BeforeEachCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback, InvocationInterceptor {

    private final List<ExecutionHook> hooks = getHooks();
    private boolean isRefreshed;

    @Override
    public void afterTestExecution(ExtensionContext context) {
        isRefreshed = false;
    }

    private void refresh(boolean condition) {
        if (!isRefreshed && condition) {
            REFRESHABLE_CONTEXTS.forEach(ContextRefreshable::refreshContext);
            isRefreshed = true;
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        refresh(isBeforeAll());
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        refresh(isBeforeEach());
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        refresh(isBeforeTest());
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        injectValues(testInstance);
    }

    private <T> T invokeHooksAndProceed(InvocationInterceptor.Invocation<T> invocation,
                                        ReflectiveInvocationContext<Method> invocationContext,
                                        boolean isTest) throws Throwable {
        hooks.forEach(executionHook -> executionHook
                .executeMethodHook(invocationContext.getExecutable(),
                        invocationContext.getTarget().orElseGet(invocationContext::getTarget),
                        isTest));
        return invocation.proceed();
    }

    @Override
    public void interceptBeforeAllMethod(InvocationInterceptor.Invocation<Void> invocation,
                                         ReflectiveInvocationContext<Method> invocationContext,
                                         ExtensionContext extensionContext) throws Throwable {
        invokeHooksAndProceed(invocation, invocationContext, false);
    }

    @Override
    public void interceptAfterAllMethod(Invocation<Void> invocation,
                                        ReflectiveInvocationContext<Method> invocationContext,
                                        ExtensionContext extensionContext) throws Throwable {
        invokeHooksAndProceed(invocation, invocationContext, false);
    }

    @Override
    public void interceptBeforeEachMethod(InvocationInterceptor.Invocation<Void> invocation,
                                          ReflectiveInvocationContext<Method> invocationContext,
                                          ExtensionContext extensionContext) throws Throwable {
        invokeHooksAndProceed(invocation, invocationContext, false);
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation,
                                         ReflectiveInvocationContext<Method> invocationContext,
                                         ExtensionContext extensionContext) throws Throwable {
        invokeHooksAndProceed(invocation, invocationContext, false);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        invokeHooksAndProceed(invocation, invocationContext, true);
    }

    @Override
    public <T> T interceptTestFactoryMethod(Invocation<T> invocation,
                                            ReflectiveInvocationContext<Method> invocationContext,
                                            ExtensionContext extensionContext) throws Throwable {
        return invokeHooksAndProceed(invocation, invocationContext, true);
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation,
                                            ReflectiveInvocationContext<Method> invocationContext,
                                            ExtensionContext extensionContext) throws Throwable {
        invokeHooksAndProceed(invocation, invocationContext, true);
    }
}
