package net.klakegg.guice.lambda;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import net.klakegg.guice.lambda.annotation.Dependencies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author erlend
 */
public class LambdaInjector<S> {

    private final Class<S> handler;

    private final Injector injector;

    public static <S> LambdaInjector<S> with(Class<S> handler) {
        return new LambdaInjector<>(handler);
    }

    private LambdaInjector(Class<S> handler) {
        this.handler = handler;

        // Create injector with dependent modules
        injector = Guice.createInjector(getDependencies(handler).stream()
                .distinct()
                .map(LambdaInjector::classNewInstance)
                .collect(Collectors.toList()));
    }

    public S getHandler() {
        return injector.getInstance(handler);
    }

    private List<Class<? extends Module>> getDependencies(Class<?> cls) {
        if (!cls.isAnnotationPresent(Dependencies.class))
            return Collections.emptyList();

        List<Class<? extends Module>> result = new ArrayList<>();
        result.addAll(Arrays.asList(cls.getAnnotation(Dependencies.class).value()));
        result.addAll(Stream.of(cls.getAnnotation(Dependencies.class).value())
                .map(this::getDependencies)
                .flatMap(List::stream)
                .collect(Collectors.toList()));

        return result;
    }

    private static <T> T classNewInstance(Class<? extends T> cls) {
        try {
            return cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
