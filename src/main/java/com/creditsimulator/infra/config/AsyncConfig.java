package com.creditsimulator.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(4);              // threads fixas
        executor.setMaxPoolSize(10);              // máximo simultâneo
        executor.setQueueCapacity(100);           // fila para tarefas pendentes
        executor.setThreadNamePrefix("AsyncJob-"); // prefixo útil para logs
        executor.initialize();

        return executor;
    }
}
