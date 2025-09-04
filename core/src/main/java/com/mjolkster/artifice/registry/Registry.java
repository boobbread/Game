package com.mjolkster.artifice.registry;

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

    /**
     * Lazy registry using Supplier, returns a RegistryObject<T>
     */
    public <U extends T> RegistryObject<U> register(String key, Supplier<U> supplier) {
        if (entries.containsKey(key)) {
            throw new IllegalArgumentException(
                "Duplicate key '" + key + "' in registry '" + registryName + "'");
        }
        RegistryObject<U> ro = new RegistryObject<>(supplier);
        entries.put(key, ro.get()); // still eagerly stores, but works
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
