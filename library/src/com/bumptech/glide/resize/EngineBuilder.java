package com.bumptech.glide.resize;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import com.bumptech.glide.resize.cache.DiskCache;
import com.bumptech.glide.resize.cache.MemoryCache;

import java.util.concurrent.ExecutorService;

public class EngineBuilder {
    final MemoryCache memoryCache;
    final DiskCache diskCache;

    private ExecutorService service;
    private Handler bgHandler;

    ResourceRunnerFactory factory;
    ResourceReferenceCounter resourceReferenceCounter;
    KeyFactory keyFactory;

    public EngineBuilder(MemoryCache memoryCache, DiskCache diskCache) {
        this.memoryCache = memoryCache;
        this.diskCache = diskCache;
    }

    public EngineBuilder setExecutorService(ExecutorService service) {
        this.service = service;
        return this;
    }

    public EngineBuilder setBackgroundHandler(Handler bgHandler) {
        this.bgHandler = bgHandler;
        return this;
    }

    public Engine build() {
        if (service == null) {
            final int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
            service = new FifoPriorityThreadPoolExecutor(cores);
        }

        if (bgHandler == null) {
            HandlerThread handlerThread = new HandlerThread("EngineThread");
            handlerThread.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            handlerThread.start();
            bgHandler = new Handler(handlerThread.getLooper());
        }

        this.resourceReferenceCounter = new DefaultResourceReferenceCounter();

        keyFactory = new EngineKeyFactory();

        factory = new DefaultResourceRunnerFactory(memoryCache, diskCache, new Handler(Looper.getMainLooper()), service,
                bgHandler, resourceReferenceCounter);

        return new Engine(this);
    }
}
