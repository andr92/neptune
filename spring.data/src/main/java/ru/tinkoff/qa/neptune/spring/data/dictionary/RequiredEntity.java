package ru.tinkoff.qa.neptune.spring.data.dictionary;

import ru.tinkoff.qa.neptune.core.api.steps.annotations.Description;

import static ru.tinkoff.qa.neptune.core.api.localization.StepLocalization.translate;

@Description("Required entity")
public final class RequiredEntity {

    @Override
    public String toString() {
        return translate(this);
    }
}
