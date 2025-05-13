package com.creditsimulator.infra.messaging;

import com.creditsimulator.application.message.QueuedSimulationMessage;
import com.creditsimulator.domain.port.outgoing.SimulationMessageQueuePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitSimulationQueueAdapter implements SimulationMessageQueuePort {
    private final RabbitTemplate rabbitTemplate;

    public static final String QUEUE_NAME = "simulation.queue";

    @Override
    public void enqueue(QueuedSimulationMessage message) {
        rabbitTemplate.convertAndSend(QUEUE_NAME, message);
        log.info("[QUEUE] Enqueued message for job {}", message.jobId());
    }
}
