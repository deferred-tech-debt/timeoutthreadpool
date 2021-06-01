package com.async.executor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TimeOutThreadPoolExecutorTest {

    private TimeOutThreadPoolExecutor executor;

    @Before
    public void init() {
        executor = new TimeOutThreadPoolExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("Time Out Async -");
        executor.setAllowCoreThreadTimeOut(true);
        executor.setDaemon(true);
        executor.setTimeOut(1);
        executor.setTimeUnit(TimeUnit.MINUTES);
        executor.setQueueCapacity(5);
        executor.init();
        executor.afterPropertiesSet();
    }

    @Test
    public void testExecuteRunnable() throws ExecutionException, InterruptedException {
        AtomicInteger incrementalInteger = new AtomicInteger(1);
        FutureTask<Void> task = new FutureTask<>(() -> incrementalInteger.set(incrementalInteger.get() + 1), null);
        executor.execute(task);
        task.get();
        Assert.assertEquals(2, incrementalInteger.get());
    }

    @Test
    public void testExecuteStartTimeOutRunnable() throws ExecutionException, InterruptedException {
        AtomicInteger incrementalInteger = new AtomicInteger(1);
        FutureTask<Void> task = new FutureTask<>(() -> incrementalInteger.set(incrementalInteger.get() + 1), null);
        executor.execute(task, 10);
        task.get();
        Assert.assertEquals(2, incrementalInteger.get());
    }

    @Test
    public void testExecuteRunnableTimeOut() throws InterruptedException {
        AtomicInteger incrementalInteger = new AtomicInteger(1);
        Runnable task = getInterruptAbleRunnable(incrementalInteger, 100);
        executor.execute(task, 2, TimeUnit.MILLISECONDS);
        Thread.sleep(200);
        Assert.assertEquals(-1, incrementalInteger.get());
    }

    @Test
    public void testSubmitRunnable() throws ExecutionException, InterruptedException {
        AtomicInteger incrementalInteger = new AtomicInteger(1);
        FutureTask<Void> task = new FutureTask<>(() -> incrementalInteger.set(incrementalInteger.get() + 1), null);
        executor.submit(task);
        task.get();
        Assert.assertEquals(2, incrementalInteger.get());
    }

    @Test
    public void testSubmitRunnableTimeOut() throws InterruptedException {
        AtomicInteger incrementalInteger = new AtomicInteger(1);
        Runnable task = getInterruptAbleRunnable(incrementalInteger, 100);
        executor.submit(task, 2, TimeUnit.MILLISECONDS);
        Thread.sleep(200);
        Assert.assertEquals(-1, incrementalInteger.get());
    }

    private Runnable getInterruptAbleRunnable(AtomicInteger incrementalInteger, long sleepMilliSeconds) {
        return () -> {
            try {
                Thread.sleep(sleepMilliSeconds);
                incrementalInteger.set(incrementalInteger.get() + 1);
            } catch (InterruptedException e) {
                incrementalInteger.set(-1);
            }
        };
    }

    @Test
    public void testSubmitCallable() throws ExecutionException, InterruptedException {
        AtomicInteger incrementalInteger = new AtomicInteger(1);
        Callable<Void> task = () -> {
            incrementalInteger.set(incrementalInteger.get() + 1);
            return null;
        };
        Future<Void> future = executor.submit(task);
        future.get();
        Assert.assertEquals(2, incrementalInteger.get());
    }

    @Test
    public void testSubmitListenableRunnable() throws ExecutionException, InterruptedException {
        AtomicInteger incrementalInteger = new AtomicInteger(1);
        Runnable task = () -> incrementalInteger.set(incrementalInteger.get() + 1);
        Future<?> future = executor.submitListenable(task);
        future.get();
        Assert.assertEquals(2, incrementalInteger.get());
    }

    @Test
    public void testSubmitListenableCallable() throws ExecutionException, InterruptedException {
        AtomicInteger incrementalInteger = new AtomicInteger(1);
        Callable<Void> task = () -> {
            incrementalInteger.set(incrementalInteger.get() + 1);
            return null;
        };
        Future<Void> future = executor.submitListenable(task);
        future.get();
        Assert.assertEquals(2, incrementalInteger.get());
    }

    @Test
    public void testSubmitCallableTimeOut() throws InterruptedException {
        AtomicInteger incrementalInteger = new AtomicInteger(1);
        Callable<Void> task = () -> {
            try {
                Thread.sleep(100);
                incrementalInteger.set(incrementalInteger.get() + 1);
            } catch (InterruptedException e) {
                incrementalInteger.set(-1);
            }
            return null;
        };
        Future<Void> future = executor.submit(task, 20, TimeUnit.MILLISECONDS);
        Thread.sleep(200);
        Assert.assertTrue(future.isCancelled());
        Assert.assertEquals(-1, incrementalInteger.get());
    }
}