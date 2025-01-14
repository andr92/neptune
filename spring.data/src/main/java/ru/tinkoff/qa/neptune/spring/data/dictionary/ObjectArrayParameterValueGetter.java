package ru.tinkoff.qa.neptune.spring.data.dictionary;

import ru.tinkoff.qa.neptune.core.api.steps.parameters.ParameterValueGetter;

import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Arrays.stream;
import static ru.tinkoff.qa.neptune.database.abstractions.data.serializer.DataSerializer.serializeObject;

public final class ObjectArrayParameterValueGetter implements ParameterValueGetter<Object[]> {

    @Override
    public String getParameterValue(Object[] fieldValue) {
        return stream(fieldValue)
                .map(o -> serializeObject(NON_NULL, o))
                .collect(Collectors.joining(", "));
    }
}
