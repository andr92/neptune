package ru.tinkoff.qa.neptune.data.base.api.data.operations;

import org.apache.commons.lang3.StringUtils;
import org.datanucleus.ExecutionContextImpl;
import org.datanucleus.api.jdo.JDOPersistenceManager;
import ru.tinkoff.qa.neptune.core.api.event.firing.annotation.MakeFileCapturesOnFinishing;
import ru.tinkoff.qa.neptune.core.api.event.firing.annotation.MakeStringCapturesOnFinishing;
import ru.tinkoff.qa.neptune.core.api.steps.SequentialGetStepSupplier;
import ru.tinkoff.qa.neptune.data.base.api.DataBaseStepContext;
import ru.tinkoff.qa.neptune.data.base.api.ListOfDataBaseObjects;
import ru.tinkoff.qa.neptune.data.base.api.PersistableObject;
import ru.tinkoff.qa.neptune.data.base.api.queries.SelectASingle;
import ru.tinkoff.qa.neptune.data.base.api.queries.SelectList;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static ru.tinkoff.qa.neptune.core.api.steps.StoryWriter.action;
import static ru.tinkoff.qa.neptune.data.base.api.ConnectionToUse.ConnectionDataReader.getConnection;

/**
 * This class is designed to perform available operations on stored data such as the inserting/updating/deleting
 * and return results.
 *
 * @param <T> is a type of {@link PersistableObject} to be operated (e.g. inserted, updated or deleted)
 */
@SuppressWarnings("unchecked")
@MakeFileCapturesOnFinishing
@MakeStringCapturesOnFinishing
public final class DataOperation<T extends PersistableObject>  extends SequentialGetStepSupplier
        .GetIterableChainedStepSupplier<DataBaseStepContext, List<T>, Map<JDOPersistenceManager, List<T>>, T, DataOperation<T>> {

    private DataOperation(String description, Function<Map<JDOPersistenceManager, List<T>>, List<T>> originalFunction) {
        super(description, originalFunction);
    }

    /**
     * Updating a single stored record. The record to be updated is selected by query and then updated.
     *
     * @param howToSelect is a description of query how to select the record
     * @param set are instances of {@link UpdateExpression} that describe how to update the record
     * @param <T> is a type of {@link PersistableObject} to be updated
     * @return an instance of {@link DataOperation}
     */
    public static <T extends PersistableObject> DataOperation<T> updated(SelectASingle<T, ?> howToSelect, UpdateExpression<T>... set) {
        checkArgument(nonNull(howToSelect), "Please define how to select an object to be updated");
        checkArgument(nonNull(set), "Please define update-actions");
        checkArgument(set.length > 0, "Should be defined at leas one update-actions");
        return new DataOperation<T>(format("Updated %s", howToSelect),
                jdoPersistenceManagerListMap -> update(jdoPersistenceManagerListMap, set))
                .from(context -> {
                    var result = context.select(howToSelect);
                    var list = ofNullable(result).map(List::of).orElseGet(List::of);
                    return getMap(context, list);
                });
    }

    /**
     * Updating a list of stored records. Records to be updated are selected by query and then updated.
     *
     * @param howToSelect is a description of query how to select records
     * @param set are instances of {@link UpdateExpression} that describe how to update the record
     * @param <T> is a type of {@link PersistableObject} to be updated
     * @return an instance of {@link DataOperation}
     */
    public static <T extends PersistableObject> DataOperation<T> updated(SelectList<T, ?> howToSelect, UpdateExpression<T>... set) {
        checkArgument(nonNull(howToSelect), "Please define how to select objects to be updated");
        checkArgument(nonNull(set), "Please define update-actions");
        checkArgument(set.length > 0, "Should be defined at leas one update-actions");
        return new DataOperation<T>(format("Updated %s", howToSelect),
                jdoPersistenceManagerListMap -> update(jdoPersistenceManagerListMap, set))
                .from(context -> getMap(context, context.select(howToSelect)));
    }

    /**
     * Updating a list of stored records.
     *
     * @param toBeUpdated is a list of stored records that is selected firstly
     * @param set are instances of {@link UpdateExpression} that describe how to update the record
     * @param <T> is a type of {@link PersistableObject} to be updated
     * @return an instance of {@link DataOperation}
     */
    public static <T extends PersistableObject> DataOperation<T> updated(Collection<T> toBeUpdated, UpdateExpression<T>... set) {
        checkArgument(nonNull(toBeUpdated),
                "Collection of objects to be updated should be defined as a value that differs from null");
        checkArgument(nonNull(set), "Please define update-actions");
        checkArgument(set.length > 0, "Should be defined at leas one update-actions");

        var toUpdate = toBeUpdated
                .stream()
                .filter(Objects::nonNull)
                .collect(toList());

        checkArgument(toUpdate.size() > 0,
                "Should be defined at least one object to update it");
        checkArgument(nonNull(set),
                "Please define update-actions");

        return new DataOperation<T>(format("Updated %s object/objects from table/tables %s",
                toUpdate.size(),
                toUpdate.stream()
                        .map(PersistableObject::fromTable)
                        .distinct()
                        .collect(toList())),
                jdoPersistenceManagerListMap -> update(jdoPersistenceManagerListMap, set))
                .from(context -> getMap(context, toUpdate));
    }

    /**
     * Deleting a single stored record. The record to be deleted is selected by query and then deleted.
     *
     * @param howToSelect is a description of query how to select the record
     * @param <T> is a type of {@link PersistableObject} to be deleted
     * @return an instance of {@link DataOperation}
     */
    public static <T extends PersistableObject> DataOperation<T> deleted(SelectASingle<T, ?> howToSelect) {
        checkArgument(nonNull(howToSelect), "Please define how to select an object to be deleted");
        return new DataOperation<T>(format("Deleted %s", howToSelect),
                DataOperation::delete)
                .from(context -> {
                    var result = context.select(howToSelect);
                    var list = ofNullable(result).map(List::of).orElseGet(List::of);
                    return getMap(context, list);
                });
    }

    /**
     * Deleting a list of stored records. Records to be deleted are selected by query and then deleted.
     *
     * @param howToSelect is a description of query how to select records
     * @param <T> is a type of {@link PersistableObject} to be deleted
     * @return an instance of {@link DataOperation}
     */
    public static <T extends PersistableObject> DataOperation<T> deleted(SelectList<T, ?> howToSelect) {
        checkArgument(nonNull(howToSelect), "Please define how to select objects to be deleted");
        return new DataOperation<T>(format("Deleted %s", howToSelect),
                DataOperation::delete)
                .from(context -> getMap(context, context.select(howToSelect)));
    }

    /**
     * Deleting a list of stored records.
     *
     * @param toBeDeleted is a list of stored records that is selected firstly
     * @param <T> is a type of {@link PersistableObject} to be deleted
     * @return an instance of {@link DataOperation}
     */
    public static <T extends PersistableObject> DataOperation<T> deleted(Collection<T> toBeDeleted) {
        checkArgument(nonNull(toBeDeleted),
                "Collection of objects to be deleted should be defined as a value that differs from null");

        var toDelete = toBeDeleted
                .stream()
                .filter(Objects::nonNull)
                .collect(toList());

        return new DataOperation<T>(format("Deleted %s object/objects from table/tables %s",
                toDelete.size(),
                toDelete.stream()
                        .map(PersistableObject::fromTable)
                        .distinct()
                        .collect(toList())),
                DataOperation::delete)
                .from(context -> getMap(context, toDelete));
    }

    /**
     * Inserting a list of stored records.
     *
     * @param toBeInserted is a list of records to be inserted
     * @param <T> is a type of {@link PersistableObject} to be inserted
     * @return an instance of {@link DataOperation}
     */
    public static <T extends PersistableObject> DataOperation<T> inserted(Collection<T> toBeInserted) {
        checkArgument(nonNull(toBeInserted),
                "Collection of objects to be inserted should be defined as a value that differs from null");

        var toInsert = toBeInserted
                .stream()
                .filter(Objects::nonNull)
                .collect(toList());

        checkArgument(toInsert.size() > 0,
                "Should be defined at least one object to insert it");

        return new DataOperation<T>(format("Inserted %s object/objects",
                toInsert.size()),
                DataOperation::insert)
                .from(context -> getMap(context, toInsert));
    }

    private static <T extends PersistableObject> List<T> update(Map<JDOPersistenceManager, List<T>> connectionMap, UpdateExpression<T>... set) {
        var managerSet = connectionMap.keySet();
        openTransaction(managerSet);

        try {
            var result = new ListOfDataBaseObjects<T>() {
                public String toString() {
                    var resultStr = format("%s updated object/objects", size());
                    var tableList = stream().map(PersistableObject::fromTable)
                            .filter(StringUtils::isNotBlank)
                            .distinct()
                            .collect(toList());

                    if (tableList.size() > 0) {
                        resultStr = format("%s of table/tables %s", resultStr, join(",", tableList));
                    }
                    return resultStr;
                }
            };

            var updated = new HashSet<T>();

            stream(set).forEach(setAction -> {
                updated.clear();
                var consumer = setAction.getUpdateAction();
                action(consumer.toString(), (Consumer<Map<JDOPersistenceManager, List<T>>>) map -> map.forEach((manager, ts) -> {
                    consumer.accept(ts);
                    manager.makePersistentAll(ts);
                    updated.addAll(manager.detachCopyAll(ts));
                })).accept(connectionMap);
            });

            commitTransaction(managerSet);
            result.addAll(updated);
            return result;
        }
        catch (Throwable t) {
            rollbackTransaction(managerSet);
            throw t;
        }
    }

    private static <T extends PersistableObject> List<T> insert(Map<JDOPersistenceManager, List<T>> connectionMap) {
        var managerSet = connectionMap.keySet();
        openTransaction(managerSet);

        try {
            var result = new ListOfDataBaseObjects<T>() {
                public String toString() {
                    var resultStr = format("%s inserted object/objects", size());
                    var tableList = stream().map(PersistableObject::fromTable)
                            .filter(StringUtils::isNotBlank)
                            .distinct()
                            .collect(toList());

                    if (tableList.size() > 0) {
                        resultStr = format("%s of table/tables %s", resultStr, join(",", tableList));
                    }
                    return resultStr;
                }
            };

            connectionMap.forEach((manager, toBeInserted) -> {
                var persistent = manager.makePersistentAll(toBeInserted);
                result.addAll(manager.detachCopyAll(persistent));
            });

            preCommit(managerSet);
            commitTransaction(managerSet);
            return result;
        }
        catch (Throwable t) {
            rollbackTransaction(managerSet);
            throw t;
        }
    }

    private static <T extends PersistableObject> List<T> delete(Map<JDOPersistenceManager, List<T>> connectionMap) {
        var managerSet = connectionMap.keySet();
        openTransaction(managerSet);

        try {
            var result = new ListOfDataBaseObjects<T>() {
                public String toString() {
                    return format("%s deleted object/objects", size());
                }
            };

            connectionMap.forEach((manager, ts) -> {
                manager.deletePersistentAll(ts);
                ts.forEach(o -> result.add((T) o.clone()));
            });

            preCommit(managerSet);
            commitTransaction(managerSet);
            return result;
        }
        catch (Throwable t) {
            rollbackTransaction(managerSet);
            throw t;
        }
    }

    private static <T extends PersistableObject> Map<JDOPersistenceManager, List<T>> getMap(DataBaseStepContext context,
                                                                                            Collection<T> toBeOperated) {
        var result = new LinkedHashMap<JDOPersistenceManager, List<T>>();

        toBeOperated.forEach(t -> {
            var manager = context.getManager(getConnection(t.getClass()));
            ofNullable(result.get(manager))
                    .ifPresentOrElse(ts -> ts.add(t),
                            () -> result.put(manager, new ArrayList<>(List.of(t))));
        });
        return result;
    }

    private static void openTransaction(Set<JDOPersistenceManager> jdoPersistenceManagers) {
        jdoPersistenceManagers.forEach(jdoPersistenceManager -> {
            var transaction = jdoPersistenceManager.currentTransaction();
            transaction.begin();
        });
    }

    private static void commitTransaction(Set<JDOPersistenceManager> jdoPersistenceManagers) {
        jdoPersistenceManagers.forEach(jdoPersistenceManager ->
                jdoPersistenceManager
                        .currentTransaction()
                        .commit());
    }

    private static void rollbackTransaction(Set<JDOPersistenceManager> jdoPersistenceManagers) {
        jdoPersistenceManagers.forEach(jdoPersistenceManager -> {
            var transaction = jdoPersistenceManager.currentTransaction();
            if (transaction.isActive()) {
                transaction.rollback();
            }
        });
    }

    private static void preCommit(Set<JDOPersistenceManager> jdoPersistenceManagers) {
        jdoPersistenceManagers.forEach(jdoPersistenceManager ->
                ((ExecutionContextImpl) jdoPersistenceManager
                        .getExecutionContext())
                        .preCommit());
    }
}
