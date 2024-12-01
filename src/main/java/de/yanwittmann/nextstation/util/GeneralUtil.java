package de.yanwittmann.nextstation.util;

import java.util.function.Consumer;

public abstract class GeneralUtil {
    @SafeVarargs
    public static <T> T with(T instance, Consumer<T>... consumer) {
        for (Consumer<T> c : consumer) {
            c.accept(instance);
        }
        return instance;
    }
}
