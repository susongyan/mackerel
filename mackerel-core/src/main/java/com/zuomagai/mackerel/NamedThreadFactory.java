package com.zuomagai.mackerel;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private final AtomicInteger id = new AtomicInteger(1);
    private final String threadNamePrefix;

    public NamedThreadFactory(String prefix) {
        this.threadNamePrefix = prefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(threadNamePrefix + "#" + id.getAndIncrement());
        return thread;
    }
}
