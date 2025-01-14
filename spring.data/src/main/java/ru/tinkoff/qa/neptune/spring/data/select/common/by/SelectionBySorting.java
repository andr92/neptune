package ru.tinkoff.qa.neptune.spring.data.select.common.by;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.data.repository.reactive.RxJava2SortingRepository;
import org.springframework.data.repository.reactive.RxJava3SortingRepository;
import ru.tinkoff.qa.neptune.core.api.steps.annotations.Description;
import ru.tinkoff.qa.neptune.core.api.steps.annotations.DescriptionFragment;
import ru.tinkoff.qa.neptune.spring.data.SpringDataFunction;

import static com.google.common.base.Preconditions.checkNotNull;
import static ru.tinkoff.qa.neptune.core.api.localization.StepLocalization.translate;

@SuppressWarnings("unchecked")
@Description("all by sorting '{sort}'")
public final class SelectionBySorting<R, ID, T extends Repository<R, ID>> extends SpringDataFunction<T, Iterable<R>> {

    @DescriptionFragment("sort")
    final Sort sort;

    public SelectionBySorting(Sort sort) {
        super(PagingAndSortingRepository.class,
                ReactiveSortingRepository.class,
                RxJava2SortingRepository.class,
                RxJava3SortingRepository.class);
        checkNotNull(sort);
        this.sort = sort;
    }

    @Override
    public String toString() {
        return translate(this);
    }

    @Override
    public Iterable<R> apply(T t) {
        if (t instanceof PagingAndSortingRepository) {
            return ((PagingAndSortingRepository<R, ID>) t).findAll(sort);
        }

        if (t instanceof ReactiveSortingRepository) {
            return ((ReactiveSortingRepository<R, ID>) t).findAll(sort).collectList().block();
        }

        if (t instanceof RxJava2SortingRepository) {
            return ((RxJava2SortingRepository<R, ID>) t).findAll(sort).toList().blockingGet();
        }

        if (t instanceof RxJava3SortingRepository) {
            return ((RxJava3SortingRepository<R, ID>) t).findAll(sort).toList().blockingGet();
        }

        throw unsupportedRepository(t);
    }
}
