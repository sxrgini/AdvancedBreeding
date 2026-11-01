package com.iiampanda.icbreed.platform;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class RenderStateDataKey<T> {
    private final Map<Object, T> values = Collections.synchronizedMap(new WeakHashMap<>());

    private RenderStateDataKey() {
    }

    public static <T> RenderStateDataKey<T> create() {
        return new RenderStateDataKey<>();
    }

    public void put(Object state, T value) {
        values.put(state, value);
    }

    public T get(Object state) {
        return values.get(state);
    }
}
