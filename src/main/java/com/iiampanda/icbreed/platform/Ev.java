package com.iiampanda.icbreed.platform;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Ev<T> {
    public final List<T> handlers = new CopyOnWriteArrayList<>();

    public void register(T handler) {
        handlers.add(handler);
    }
}
