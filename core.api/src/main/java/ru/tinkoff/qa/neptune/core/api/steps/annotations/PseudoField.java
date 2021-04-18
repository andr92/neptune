package ru.tinkoff.qa.neptune.core.api.steps.annotations;

import com.google.common.base.Supplier;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Creates an object which plays role of a field that doesn't exists.
 * This is for meta-information, that helps to create parameters and description of steps
 */
public class PseudoField<T extends Annotation> implements AnnotatedElement, Member {
    private final Class<?> clazz;
    private final String name;
    private final Class<T> annotationClass;
    private final Supplier<T> annotationSupplier;

    public PseudoField(Class<?> clazz, String name, Class<T> annotationClass, Supplier<T> annotationSupplier) {
        this.clazz = clazz;
        this.name = name;
        checkNotNull(annotationClass);
        checkArgument(annotationClass.getAnnotation(AsFieldAnnotation.class) != null,
                format("Class of annotation '%s' should be annotated by '%s'",
                        annotationClass.getName(),
                        AsFieldAnnotation.class.getName()));
        this.annotationClass = annotationClass;
        checkNotNull(annotationSupplier);
        this.annotationSupplier = annotationSupplier;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return clazz;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        if (annotationClass.isAssignableFrom(this.annotationClass)) {
            return (A) annotationSupplier.get();
        }
        return null;
    }

    @Override
    public Annotation[] getAnnotations() {
        return getDeclaredAnnotations();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return new Annotation[]{annotationSupplier.get()};
    }

    @Override
    public int getModifiers() {
        return 0;
    }

    @Override
    public boolean isSynthetic() {
        return false;
    }
}
