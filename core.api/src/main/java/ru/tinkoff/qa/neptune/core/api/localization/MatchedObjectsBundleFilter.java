package ru.tinkoff.qa.neptune.core.api.localization;

import io.github.classgraph.ClassGraph;
import ru.tinkoff.qa.neptune.core.api.hamcrest.MatchObjectName;
import ru.tinkoff.qa.neptune.core.api.steps.annotations.Description;

import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

final class MatchedObjectsBundleFilter extends DefaultAbstractBundleFiller {

    private static List<Class<?>> matchedObjects;

    protected MatchedObjectsBundleFilter(LocalizationBundlePartition p) {
        super(p, getMatchedObjects(), "MATCHED OBJECTS");
    }

    public static synchronized List<Class<?>> getMatchedObjects() {
        if (nonNull(matchedObjects)) {
            return matchedObjects;
        }

        matchedObjects = new ClassGraph()
                .enableClassInfo()
                .ignoreClassVisibility()
                .scan()
                .getSubclasses(MatchObjectName.class.getName())
                .loadClasses(MatchObjectName.class)
                .stream()
                .filter(c -> c.getAnnotation(Description.class) != null)
                .map(cls -> (Class<?>) cls)
                .sorted(comparing(Class::getName))
                .collect(toList());

        return matchedObjects;
    }
}
