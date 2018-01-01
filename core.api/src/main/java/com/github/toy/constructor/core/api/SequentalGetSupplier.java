package com.github.toy.constructor.core.api;

import java.util.function.Function;

import static com.github.toy.constructor.core.api.StoryWriter.toGet;
import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

/**
 * It is designed to typify functions which get required value and to restrict chains of
 * the applying of functions.
 *
 * @param <T> is a type of an input value.
 * @param <R> is a type of a returned value.
 * @param <Q> is a type of a mediator value which is used to get the required result.
 * @param <THIS> is self-type. It is necessary for the {@link #set(String, Function)} method.
 */
public abstract class SequentalGetSupplier<T, R, Q, THIS extends SequentalGetSupplier<T, R, Q, THIS>>
        extends GetSupplier<T, R, THIS> {

    /**
     * This method is designed to represent a chain of result calculation and restrict it.
     * It is supposed to be overridden or overloaded/used by custom method.
     *
     * @param description of a value which is expected to be returned.
     * @param mediatorFunction a function which returns a mediate value to get expected result.
     * @return self-reference.
     */
    protected THIS from(String description, Function<T, Q> mediatorFunction) {
        checkArgument(mediatorFunction != null, "Function to get value from was not " +
                "defined");
        checkArgument(DescribedFunction.class.isAssignableFrom(mediatorFunction.getClass()),
                "Function to get value from is not described. " +
                        "Use method StoryWriter.toGet to describe it.");
        return set(format("%s from %s", description, mediatorFunction.toString()),
                mediatorFunction.andThen(getEndFunction()));
    }

    /**
     * This method is designed to represent a chain of result calculation and restrict it.
     * It is supposed to be overridden or overloaded/used by custom method.
     *
     * @param description of a value which is expected to be returned.
     * @param supplier of a function which returns a mediate value to get expected result.
     * @return self-reference.
     */
    protected THIS from(String description, GetSupplier<T, Q, ?> supplier) {
        checkArgument(supplier != null, "The supplier of the function is not defined");
        return from(description, supplier.get());
    }

    /**
     * This method is designed to represent a chain of result calculation and restrict it.
     * It is supposed to be overridden or overloaded/used by custom method.
     *
     * @param description of a value which is expected to be returned.
     * @param value is a mediate value to get expected result.
     * @return self-reference.
     */
    protected THIS from(String description, Q value) {
        return from(description, toGet(value.toString(), t -> value));
    }

    /**
     * @return a functions which returns required result on the applying.
     */
    abstract Function<Q, R> getEndFunction();
}
