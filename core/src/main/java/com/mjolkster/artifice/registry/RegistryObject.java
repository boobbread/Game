package com.mjolkster.artifice.registry;

import java.util.function.Supplier;

public class RegistryObject<T> {
    private final Supplier<? extends T> supplier;
    private T instance;

    public RegistryObject(Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (instance == null) {
            instance = supplier.get();
        }
        return instance;
    }
}
