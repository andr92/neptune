package ru.tinkoff.qa.neptune.data.base.api.queries;

@Deprecated(forRemoval = true)
public interface ResultPersistentManager {

    default void keepResultPersistent(SelectASingle<?> select) {
        select.getResultPersistent().setToKeepOnPersistent(true);
    }

    default void keepResultPersistent(SelectList<?, ?> select) {
        select.getResultPersistent().setToKeepOnPersistent(true);
    }
}
