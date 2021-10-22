package ru.tinkoff.qa.neptune.spring.data.select;

import org.springframework.data.repository.Repository;
import ru.tinkoff.qa.neptune.core.api.steps.SequentialGetStepSupplier;
import ru.tinkoff.qa.neptune.core.api.steps.annotations.IncludeParamsOfInnerGetterStep;
import ru.tinkoff.qa.neptune.database.abstractions.SelectQuery;
import ru.tinkoff.qa.neptune.spring.data.SpringDataContext;

import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Gets some {@link Iterable} from selected entity.
 *
 * @param <T> is a type of object to get
 * @param <M> is a type of entity
 */
@SuppressWarnings("unchecked")
@IncludeParamsOfInnerGetterStep
@SequentialGetStepSupplier.DefineCriteriaParameterName("Result criteria")
public abstract class GetItemOfArrayFromEntity<T, M, S extends GetItemOfArrayFromEntity<T, M, S>>
        extends SequentialGetStepSupplier.GetObjectFromArrayChainedStepSupplier<SpringDataContext, T, M, S>
        implements SelectQuery<T> {

    private GetItemOfArrayFromEntity(Function<M, T[]> originalFunction) {
        super(originalFunction);
    }

    static <T, M, ID, R extends Repository<M, ID>> GetItemOfArrayFromEntity<T, M, ?> getArrayItemFromEntity(
            SelectOneStepSupplier<M, ID, R> from,
            Function<M, T[]> f) {
        return new GetItemOfArrayFromEntityImpl<>(f).from(from);
    }

    @Override
    protected S from(SequentialGetStepSupplier<SpringDataContext, ? extends M, ?, ?, ?> from) {
        setDescription(from.toString());
        return super.from(from);
    }

    @Override
    protected String getDescription() {
        return ofNullable(getFrom())
                .map(Object::toString)
                .orElse(EMPTY);
    }

    public static final class GetItemOfArrayFromEntityImpl<T, M>
            extends GetItemOfArrayFromEntity<T, M, GetItemOfArrayFromEntityImpl<T, M>> {

        private GetItemOfArrayFromEntityImpl(Function<M, T[]> originalFunction) {
            super(originalFunction);
        }

        public <ID, R extends Repository<M, ID>> GetItemOfArrayFromEntityImpl<T, M> setRepository(R repository) {
            ofNullable(getFrom()).ifPresent(o -> ((SelectOneStepSupplier.SelectOneStepSupplierImpl<M, ID, R>) o).from(repository));
            return this;
        }

        @Override
        public GetItemOfArrayFromEntityImpl<T, M> setDescription(String description) {
            ofNullable(getFrom()).ifPresent(o -> ((SelectOneStepSupplier.SelectOneStepSupplierImpl<?, ?, ?>) o).setDescription(description));
            return this;
        }
    }
}
