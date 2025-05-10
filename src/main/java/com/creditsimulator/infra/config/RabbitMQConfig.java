package com.creditsimulator.infra.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE = "simulation.queue";

    @Bean
    public Queue simulationQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }
}