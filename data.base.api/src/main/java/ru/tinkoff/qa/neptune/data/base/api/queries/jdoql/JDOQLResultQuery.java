package ru.tinkoff.qa.neptune.data.base.api.queries.jdoql;

import ru.tinkoff.qa.neptune.data.base.api.IdSetter;
import ru.tinkoff.qa.neptune.data.base.api.PersistableObject;
import ru.tinkoff.qa.neptune.data.base.api.result.TableResultList;

import javax.jdo.JDOQLTypedQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * This class is designed to perform a query to select list of list. Each list item contains values of fields taken
 * from stored objects.
 * @param <T> is a type of {@link PersistableObject} objects to take field values from
 */
public class JDOQLResultQuery<T extends PersistableObject> implements Function<ReadableJDOQuery<T>, TableResultList>, IdSetter {

    private JDOQLResultQuery() {
        super();
    }

    /**
     * Creates an instance that performs a query to select list of field values taken from stored objects by {@link JDOQLTypedQuery}
     *
     * @param <T> is a type of {@link PersistableObject} objects to take field values from
     * @return new {@link JDOQLResultQuery}
     */
    public static <T extends PersistableObject> JDOQLResultQuery<T> byJDOQLResultQuery() {
        return new JDOQLResultQuery<>();
    }

    @Override
    public TableResultList apply(ReadableJDOQuery<T> jdoqlTypedQuery) {
        var result = jdoqlTypedQuery.executeInternalQuery(jdoqlTypedQuery.getInternalQuery());
        List<?> list = ofNullable(result)
                .map(o -> {
                    var clazz = o.getClass();
                    if (Collection.class.isAssignableFrom(clazz)) {
                        return new ArrayList<>((Collection<?>) o);
                    }

                    if (clazz.isArray()) {
                        return new ArrayList<>(Arrays.asList((Object[]) o));
                    }

                    var resultList =  new ArrayList<>();
                    resultList.add(o);
                    return resultList;
                })
                .orElseGet(ArrayList::new);

        var manager = jdoqlTypedQuery.getPersistenceManager();
        List<List<Object>> toBeReturned = new ArrayList<>();

        list.forEach(o -> {
            var row = new ArrayList<>();

            if (o != null && o.getClass().isArray()) {
                row.addAll(stream(((Object[]) o))
                        .map(o1 -> ofNullable(o1).map(o2 -> {
                            if (!PersistableObject.class.isAssignableFrom(o2.getClass())) {
                                return o2;
                            }
                            else {
                                var toReturn = manager.detachCopy(o2);
                                setRealId(o2, toReturn);
                                return toReturn;
                            }
                        }).orElse(null))
                        .collect(toList()));

                toBeReturned.add(row);
            } else {
                var resulted = ofNullable(o).map(o1 -> {
                    if (!PersistableObject.class.isAssignableFrom(o1.getClass())) {
                        return o1;
                    }
                    else {
                        var toReturn = manager.detachCopy(o1);
                        setRealId(o1, toReturn);
                        return toReturn;
                    }
                }).orElse(null);
                row.add(resulted);

                toBeReturned.add(row);
            }
        });

        return new TableResultList(toBeReturned) {
        };
    }
}
