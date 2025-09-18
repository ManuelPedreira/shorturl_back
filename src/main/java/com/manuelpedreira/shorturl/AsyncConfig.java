package com.manuelpedreira.shorturl;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

  private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

  @Bean(name = "telemetryExecutor")
  public Executor telemetryExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(50);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("Telemetry-");
    executor.initialize();
    return executor;
  }

  @Bean(name = "metadataExecutor")
  public Executor metadataExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(3);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("Metadata-");
    executor.initialize();
    return executor;
  }

  @Override
  public Executor getAsyncExecutor() {
    // valor por defecto si no especificas un @Async("nombreExecutor")
    return telemetryExecutor();
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return new AsyncUncaughtExceptionHandler() {
      @Override
      public void handleUncaughtException(@NonNull Throwable ex, @NonNull Method method, @NonNull Object... params) {
        logger.error("Async error in method: {} - params: {}", method.getName(), params, ex);
      }
    };
  }
}
