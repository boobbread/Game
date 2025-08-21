package com.mjolkster.artifice.registration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Registry<T> {

    private final Map<String, T> entries = new HashMap<>();
    private final String registryName;

    public Registry(String registryName) {
        this.registryName = registryName;
    }

    /** Lazy registration using Supplier, returns a RegistryObject<T> */
    public RegistryObject<T> register(String key, Supplier<? extends T> supplier) {
        if (entries.containsKey(key)) {
            throw new IllegalArgumentException(
                "Duplicate key '" + key + "' in registry '" + registryName + "'");
        }
        RegistryObject<T> ro = new RegistryObject<>(supplier);
        entries.put(key, ro.get()); // optionally, could store lazy object only
        return ro;
    }

    public T get(String key) {
        return entries.get(key);
    }

    public Map<String, T> getAll() {
        return Collections.unmodifiableMap(entries);
    }

    public String getName() {
        return registryName;
    }
}
