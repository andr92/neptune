package ru.tinkoff.qa.neptune.spring.data.save;

import com.google.common.collect.Iterables;
import org.springframework.data.repository.Repository;
import ru.tinkoff.qa.neptune.core.api.event.firing.annotations.CaptureOnSuccess;
import ru.tinkoff.qa.neptune.core.api.steps.SequentialGetStepSupplier;
import ru.tinkoff.qa.neptune.core.api.steps.annotations.DescriptionFragment;
import ru.tinkoff.qa.neptune.core.api.steps.annotations.IncludeParamsOfInnerGetterStep;
import ru.tinkoff.qa.neptune.core.api.steps.annotations.StepParameter;
import ru.tinkoff.qa.neptune.core.api.steps.parameters.ParameterValueGetter;
import ru.tinkoff.qa.neptune.database.abstractions.InsertQuery;
import ru.tinkoff.qa.neptune.database.abstractions.SelectQuery;
import ru.tinkoff.qa.neptune.database.abstractions.UpdateAction;
import ru.tinkoff.qa.neptune.spring.data.SpringDataContext;
import ru.tinkoff.qa.neptune.spring.data.captors.EntitiesCaptor;
import ru.tinkoff.qa.neptune.spring.data.save.dictionary.Update;
import ru.tinkoff.qa.neptune.spring.data.select.SelectManyStepSupplier;
import ru.tinkoff.qa.neptune.spring.data.select.SelectOneStepSupplier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.List.of;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static ru.tinkoff.qa.neptune.core.api.localization.StepLocalization.translate;

@IncludeParamsOfInnerGetterStep
@SequentialGetStepSupplier.DefineGetImperativeParameterName("Save:")
@CaptureOnSuccess(by = EntitiesCaptor.class)
public abstract class SaveStepSupplier<INPUT, RESULT, R, ID, T extends Repository<R, ID>>
        extends SequentialGetStepSupplier.GetObjectChainedStepSupplier<SpringDataContext, RESULT, INPUT, SaveStepSupplier<INPUT, RESULT, R, ID, T>>
        implements InsertQuery<RESULT>,
        SelectQuery<RESULT> {

    @StepParameter("Repository")
    final T repo;
    List<UpdateAction<R>> updates = of();

    protected SaveStepSupplier(T repo, SaveFunction<R, ID, T, INPUT, RESULT> originalFunction) {
        super(originalFunction);
        this.repo = repo;
    }

    public static <R, ID, T extends Repository<R, ID>> SaveStepSupplier<R, R, R, ID, T> save(
            String description,
            T repository,
            SelectOneStepSupplier<R, ID, T> select) {
        checkArgument(isNotBlank(description), "Description should be defined");
        var translated = translate(description);
        ((SelectOneStepSupplier.SelectOneStepSupplierImpl<R, ID, T>) select).setDescription(translated);
        ((SelectOneStepSupplier.SelectOneStepSupplierImpl<R, ID, T>) select).from(repository);
        return new SaveOneStepSupplier<>(repository)
                .setDescription(translated)
                .from(select);
    }

    public static <R, ID, T extends Repository<R, ID>> SaveStepSupplier<R, R, R, ID, T> save(
            @DescriptionFragment(value = "description",
                    makeReadableBy = ParameterValueGetter.TranslatedDescriptionParameterValueGetter.class)
                    String description,
            T repository,
            R toSave) {
        checkArgument(isNotBlank(description), "Description should be defined");
        checkNotNull(toSave);
        return new SaveOneStepSupplier<>(repository).from(toSave);
    }

    public static <R, ID, T extends Repository<R, ID>> SaveStepSupplier<Iterable<R>, Iterable<R>, R, ID, T> save(
            String description,
            T repository,
            SelectManyStepSupplier<R, ID, T> select) {
        checkArgument(isNotBlank(description), "Description should be defined");
        var translated = translate(description);
        ((SelectManyStepSupplier.SelectManyStepSupplierImpl<R, ID, T>) select).setDescription(translated);
        ((SelectManyStepSupplier.SelectManyStepSupplierImpl<R, ID, T>) select).from(repository);
        return new SaveManyStepSupplier<>(repository)
                .setDescription(translated)
                .from(select);
    }

    public static <R, ID, T extends Repository<R, ID>> SaveStepSupplier<Iterable<R>, Iterable<R>, R, ID, T> save(
            @DescriptionFragment(value = "description",
                    makeReadableBy = ParameterValueGetter.TranslatedDescriptionParameterValueGetter.class)
                    String description,
            T repository,
            Iterable<R> toSave) {
        checkArgument(isNotBlank(description), "Description should be defined");
        checkNotNull(toSave);
        checkArgument(Iterables.size(toSave) > 0, "At leas one item to save should be defined");
        return new SaveManyStepSupplier<>(repository).from(toSave);
    }

    @Override
    public Map<String, String> getParameters() {
        var i = 1;
        var result = new LinkedHashMap<String, String>();
        for (var u : updates) {
            result.put(new Update() + " " + i, u.toString());
            i++;
        }
        return result;
    }

    @SafeVarargs
    public final SaveStepSupplier<INPUT, RESULT, R, ID, T> setUpdates(UpdateAction<R>... updates) {
        checkNotNull(updates);
        checkArgument(updates.length > 0, "At least one update should be defined");
        this.updates = asList(updates);
        return this;
    }

    @Override
    protected SaveStepSupplier<INPUT, RESULT, R, ID, T> setDescription(String description) {
        return super.setDescription(description);
    }

    private static class SaveOneStepSupplier<R, ID, T extends Repository<R, ID>> extends SaveStepSupplier<R, R, R, ID, T> {

        protected SaveOneStepSupplier(T repository) {
            super(repository, new SaveFunction.SaveOne<>(repository));
        }

        @Override
        protected void onStart(R r) {
            updates.forEach(u -> u.performUpdate(of(r)));
            super.onStart(r);
        }
    }

    private static class SaveManyStepSupplier<R, ID, T extends Repository<R, ID>> extends SaveStepSupplier<Iterable<R>, Iterable<R>, R, ID, T> {

        protected SaveManyStepSupplier(T repository) {
            super(repository, new SaveFunction.SaveMany<>(repository));
        }

        @Override
        protected void onStart(Iterable<R> rs) {
            updates.forEach(u -> u.performUpdate(newArrayList(rs)));
            super.onStart(rs);
        }
    }
}
