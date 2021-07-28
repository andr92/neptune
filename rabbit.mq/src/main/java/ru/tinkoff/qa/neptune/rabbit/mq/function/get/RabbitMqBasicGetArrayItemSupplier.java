package ru.tinkoff.qa.neptune.rabbit.mq.function.get;

import ru.tinkoff.qa.neptune.core.api.data.format.DataTransformer;
import ru.tinkoff.qa.neptune.core.api.data.format.TypeRef;
import ru.tinkoff.qa.neptune.core.api.event.firing.annotations.MaxDepthOfReporting;
import ru.tinkoff.qa.neptune.core.api.steps.Criteria;
import ru.tinkoff.qa.neptune.core.api.steps.SequentialGetStepSupplier;
import ru.tinkoff.qa.neptune.core.api.steps.annotations.Description;
import ru.tinkoff.qa.neptune.core.api.steps.annotations.DescriptionFragment;
import ru.tinkoff.qa.neptune.core.api.steps.parameters.ParameterValueGetter;
import ru.tinkoff.qa.neptune.rabbit.mq.RabbitMqStepContext;

import java.time.Duration;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@SequentialGetStepSupplier.DefineGetImperativeParameterName("Retrieve:")
@SequentialGetStepSupplier.DefineTimeOutParameterName("Time of the waiting")
@SequentialGetStepSupplier.DefineCriteriaParameterName("Object criteria")
@MaxDepthOfReporting(0)
public class RabbitMqBasicGetArrayItemSupplier<T> extends SequentialGetStepSupplier
        .GetObjectFromArrayStepSupplier<RabbitMqStepContext, T, RabbitMqBasicGetArrayItemSupplier<T>> {

    final GetFromQueue<?> getFromQueue;

    protected <M> RabbitMqBasicGetArrayItemSupplier(GetFromQueue<M> getFromQueue, Function<M, T[]> function) {
        super(function.compose(getFromQueue));
        this.getFromQueue = getFromQueue;
    }

    /**
     * Creates a step that gets some value from array which is calculated by body of message.
     *
     * @param description is description of value to get
     * @param queue       is a queue to read
     * @param autoAck     true if the server should consider messages
     *                    acknowledged once delivered; false if the server should expect
     *                    explicit acknowledgements
     * @param classT      is a class of a value to deserialize body
     * @param toGet       describes how to get desired value
     * @param <M>         is a type of deserialized body
     * @param <T>         is a type of an item of array
     * @return an instance of {@link RabbitMqBasicGetArrayItemSupplier}
     */
    @Description("{description}")
    public static <M, T> RabbitMqBasicGetArrayItemSupplier<T> rabbitArray(
            @DescriptionFragment(value = "description",
                    makeReadableBy = ParameterValueGetter.TranslatedDescriptionParameterValueGetter.class
            ) String description,
            String queue,
            boolean autoAck,
            Class<M> classT,
            Function<M, T[]> toGet) {
        checkArgument(isNotBlank(description), "Description should be defined");
        return new RabbitMqBasicGetArrayItemSupplier<>(new GetFromQueue<>(queue, autoAck, classT), toGet);
    }

    /**
     * Creates a step that gets some value from array which is calculated by body of message.
     *
     * @param description is description of value to get
     * @param queue       is a queue to read
     * @param autoAck     true if the server should consider messages
     *                    acknowledged once delivered; false if the server should expect
     *                    explicit acknowledgements
     * @param typeT       is a reference to type of a value to deserialize body
     * @param toGet       describes how to get desired value
     * @param <M>         is a type of deserialized body
     * @param <T>         is a type of an item of array
     * @return an instance of {@link RabbitMqBasicGetArrayItemSupplier}
     */
    @Description("{description}")
    public static <M, T> RabbitMqBasicGetArrayItemSupplier<T> rabbitArray(
            @DescriptionFragment(value = "description",
                    makeReadableBy = ParameterValueGetter.TranslatedDescriptionParameterValueGetter.class
            ) String description,
            String queue,
            boolean autoAck,
            TypeRef<M> typeT,
            Function<M, T[]> toGet) {
        checkArgument(isNotBlank(description), "Description should be defined");
        return new RabbitMqBasicGetArrayItemSupplier<>(new GetFromQueue<>(queue, autoAck, typeT), toGet);
    }

    /**
     * Creates a step that gets value from array body of message.
     *
     * @param description is description of value to get
     * @param queue       is a queue to read
     * @param autoAck     true if the server should consider messages
     *                    acknowledged once delivered; false if the server should expect
     *                    explicit acknowledgements
     * @param classT      is a class of a value to deserialize body
     * @param <T>         is a type of an item of array
     * @return an instance of {@link RabbitMqBasicGetArrayItemSupplier}
     */
    public static <T> RabbitMqBasicGetArrayItemSupplier<T> rabbitArray(
            String description,
            String queue,
            boolean autoAck,
            Class<T[]> classT) {
        checkArgument(isNotBlank(description), "Description should be defined");
        return rabbitArray(description, queue, autoAck, classT, ts -> ts);
    }

    /**
     * Creates a step that gets value from array body of message.
     *
     * @param description is description of value to get
     * @param queue       is a queue to read
     * @param autoAck     true if the server should consider messages
     *                    acknowledged once delivered; false if the server should expect
     *                    explicit acknowledgements
     * @param typeT       is a reference to type of a value to deserialize body
     * @param <T>         is a type of an item of array
     * @return an instance of {@link RabbitMqBasicGetArrayItemSupplier}
     */
    public static <T> RabbitMqBasicGetArrayItemSupplier<T> rabbitArray(
            String description,
            String queue,
            boolean autoAck,
            TypeRef<T[]> typeT) {
        return rabbitArray(description, queue, autoAck, typeT, ts -> ts);
    }

    @Override
    public RabbitMqBasicGetArrayItemSupplier<T> timeOut(Duration timeOut) {
        return super.timeOut(timeOut);
    }

    @Override
    public RabbitMqBasicGetArrayItemSupplier<T> criteria(String description, Predicate<? super T> predicate) {
        return super.criteria(description, predicate);
    }

    @Override
    public RabbitMqBasicGetArrayItemSupplier<T> criteria(Criteria<? super T> criteria) {
        return super.criteria(criteria);
    }

    RabbitMqBasicGetArrayItemSupplier<T> setDataTransformer(DataTransformer dataTransformer) {
        checkNotNull(dataTransformer);
        getFromQueue.setTransformer(dataTransformer);
        return this;
    }
}
