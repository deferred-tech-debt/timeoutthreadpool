package com.async.executor;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeOutThreadPoolExecutor extends ThreadPoolTaskExecutor {

    private long timeOut;
    private TimeUnit timeUnit;
    private ScheduledExecutorService scheduledExecutorService;

    public void init() {
        this.scheduledExecutorService = Executors.newScheduledThreadPool(this.getCorePoolSize());
    }

    @Override
    public void execute(Runnable runnable) {
        this.execute(runnable, timeOut, timeUnit);
    }

    @Override
    public void execute(Runnable task, long startTimeout) {
        this.execute(task);
    }

    public void execute(Runnable runnable, long timeout, TimeUnit timeUnit) {
        this.submit(runnable, timeout, timeUnit);
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        return this.submit(runnable, timeOut, timeUnit);
    }

    public Future<?> submit(Runnable runnable, long timeout, TimeUnit timeUnit) {
        final Future<?> runnableFuture = super.submit(runnable);
        this.scheduledExecutorService.schedule(() -> runnableFuture.cancel(true), timeout, timeUnit);
        return runnableFuture;
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        return this.submit(callable, timeOut, timeUnit);
    }

    @Override
    public ListenableFuture<?> submitListenable(Runnable task) {
        final ListenableFuture<?> listenableFuture = super.submitListenable(task);
        this.scheduledExecutorService.schedule(() -> listenableFuture.cancel(true), timeOut, timeUnit);
        return listenableFuture;
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
        final ListenableFuture<T> listenableFuture = super.submitListenable(task);
        this.scheduledExecutorService.schedule(() -> listenableFuture.cancel(true), timeOut, timeUnit);
        return listenableFuture;
    }

    public <T> Future<T> submit(Callable<T> callable, long timeout, TimeUnit timeUnit) {
        final Future<T> future = super.submit(callable);
        this.scheduledExecutorService.schedule(() -> future.cancel(true), timeout, timeUnit);
        return future;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
}