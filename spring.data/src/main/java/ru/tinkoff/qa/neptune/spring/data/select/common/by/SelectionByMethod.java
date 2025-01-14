package ru.tinkoff.qa.neptune.spring.data.select.common.by;

import org.springframework.data.repository.Repository;
import ru.tinkoff.qa.neptune.core.api.steps.annotations.Description;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.reflect.Proxy.newProxyInstance;
import static org.springframework.util.ClassUtils.getAllInterfaces;
import static ru.tinkoff.qa.neptune.core.api.localization.StepLocalization.translate;

@SuppressWarnings("unchecked")
@Description("Select by method invocation")
public final class SelectionByMethod<R, ID, T extends Repository<R, ID>, RESULT> implements Function<T, RESULT> {

    private final Function<T, RESULT> delegate;
    private Method invoked;
    private Object[] parameters;

    public SelectionByMethod(Function<T, RESULT> delegate) {
        checkNotNull(delegate);
        this.delegate = delegate;
    }

    @Override
    public RESULT apply(T t) {
        var proxyRepo = (T) newProxyInstance(getSystemClassLoader(),
                getAllInterfaces(t),
                new RepositoryMethodInvocationHandler(this, t));
        return delegate.apply(proxyRepo);
    }

    public Method getInvoked() {
        return invoked;
    }

    public Object[] getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return translate(this);
    }

    public static class RepositoryMethodInvocationHandler implements InvocationHandler {

        private final SelectionByMethod<?, ?, ?, ?> selection;
        private final Repository<?, ?> repository;

        public RepositoryMethodInvocationHandler(SelectionByMethod<?, ?, ?, ?> selection, Repository<?, ?> repository) {
            this.selection = selection;
            this.repository = repository;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            selection.invoked = method;
            selection.parameters = args;
            return method.invoke(repository, args);
        }
    }
}
