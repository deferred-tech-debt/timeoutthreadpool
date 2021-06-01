package com.async.config;

import com.async.executor.TimeOutThreadPoolExecutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableAsync
public class AppAsyncConfig implements AsyncConfigurer {
    private final Log log = LogFactory.getLog(AppAsyncConfig.class);

    @Value("${thread.pool.blocking.coefficient.ratio:10}")
    private Integer blockingCoEfficientRatio;

    @Value("${thread.pool.queue.capacity:-1}")
    private Integer threadPoolQueueCapacity;

    @Value("${thread.pool.timeout:2}")
    private long timeOut;

    @Value("${thread.pool.timeout.unit:MINUTES}")
    private TimeUnit timeUnit;

    @Bean("timeOutAsyncExecutor")
    @Override
    public TaskExecutor getAsyncExecutor() {
        int threadPoolSize = Runtime.getRuntime().availableProcessors() * (1 + blockingCoEfficientRatio);
        final TimeOutThreadPoolExecutor executor = new TimeOutThreadPoolExecutor();
        executor.setCorePoolSize(threadPoolSize);
        executor.setMaxPoolSize(threadPoolSize);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("Time Out Async -");
        executor.setAllowCoreThreadTimeOut(true);
        executor.setDaemon(true);
        executor.setTimeOut(timeOut);
        executor.setTimeUnit(timeUnit);
        executor.setQueueCapacity(threadPoolQueueCapacity < 1 ? Integer.MAX_VALUE : threadPoolQueueCapacity);
        executor.init();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> log.error(String.format("Async uncaught exception, method: %s params: %s", method, Arrays.asList(params)), throwable);
    }
}
