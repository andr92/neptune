package ru.tinkoff.qa.neptune.spring.data.delete;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.RxJava2CrudRepository;
import org.springframework.data.repository.reactive.RxJava3CrudRepository;
import ru.tinkoff.qa.neptune.spring.data.SpringDataFunction;

import static java.util.Objects.nonNull;

@SuppressWarnings("unchecked")
public final class DeleteAll<R, ID, T extends Repository<R, ID>> extends SpringDataFunction<T, Void> {

    DeleteAll() {
        super(CrudRepository.class,
                ReactiveCrudRepository.class,
                RxJava2CrudRepository.class,
                RxJava3CrudRepository.class);
    }

    @Override
    public Void apply(T t) {
        if (t instanceof CrudRepository) {
            ((CrudRepository<R, ID>) t).deleteAll();
            return null;
        }

        if (t instanceof ReactiveCrudRepository) {
            return ((ReactiveCrudRepository<R, ID>) t).deleteAll().block();
        }

        if (t instanceof RxJava2CrudRepository) {
            var thrown = ((RxJava2CrudRepository<R, ID>) t).deleteAll().blockingGet();
            if (nonNull(thrown)) {
                throw new RuntimeException(thrown);
            }
            return null;
        }

        if (t instanceof RxJava3CrudRepository) {
            ((RxJava3CrudRepository<R, ID>) t).deleteAll().blockingAwait();
            return null;
        }

        throw unsupportedRepository(t);
    }
}
