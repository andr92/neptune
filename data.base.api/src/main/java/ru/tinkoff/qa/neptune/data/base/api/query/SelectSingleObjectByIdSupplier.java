package ru.tinkoff.qa.neptune.data.base.api.query;

import ru.tinkoff.qa.neptune.data.base.api.DataBaseSteps;
import ru.tinkoff.qa.neptune.data.base.api.PersistableObject;

import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static ru.tinkoff.qa.neptune.core.api.conditions.ToGetSingleCheckedObject.getSingle;

public final class SelectSingleObjectByIdSupplier<T extends PersistableObject>
        extends ByIdsSequentialGetStepSupplier<T, T, SelectSingleObjectByIdSupplier<T>> {

    private static final String DESCRIPTION = "Result as a single item of type %s by id %s";

    private SelectSingleObjectByIdSupplier(Class<T> ofType, Object id) {
        super(ofType, id);
    }

    /**
     * Creates a supplier of a function that performs selection from a data base by id and returns a single element.
     *
     * @param ofType is a class of object to be found.
     * @param id of an object to be found.
     * @param <T> is a type of result element.
     * @return created supplier of a function.
     */
    public static <T extends PersistableObject>  SelectSingleObjectByIdSupplier<T> aSingleOfTypeById(Class<T> ofType, Object id) {
        return new SelectSingleObjectByIdSupplier<>(ofType, id);
    }

    @Override
    protected Function<DataBaseSteps, T> getEndFunction() {
        Function<DataBaseSteps, T> singleFunction = dataBaseSteps -> {
            try {
                return dataBaseSteps.getCurrentPersistenceManager().getObjectById(ofType, ids[0]);
            }
            catch (RuntimeException e) {
                return null;
            }
        };

        String description = format(DESCRIPTION, ofType.getName(), ids[0]);

        return ofNullable(condition).map(tPredicate ->
                ofNullable(nothingIsSelectedExceptionSupplier).map(nothingIsSelectedExceptionSupplier1 ->
                        getSingle(description, singleFunction, tPredicate,
                                timeToGetResult, true, nothingIsSelectedExceptionSupplier1))
                        .orElseGet(() -> getSingle(description, singleFunction, tPredicate,
                                timeToGetResult, true)))

                .orElseGet(() -> ofNullable(nothingIsSelectedExceptionSupplier)
                        .map(nothingIsSelectedExceptionSupplier1 -> getSingle(description,
                                singleFunction, timeToGetResult,  nothingIsSelectedExceptionSupplier1)
                        ).orElseGet(() ->
                                getSingle(description, singleFunction, timeToGetResult)
                        ));
    }
}