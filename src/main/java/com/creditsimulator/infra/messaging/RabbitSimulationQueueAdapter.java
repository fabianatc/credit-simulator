package com.creditsimulator.infra.messaging;

import com.creditsimulator.application.message.QueuedSimulationMessage;
import com.creditsimulator.domain.port.outgoing.SimulationMessageQueuePort;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitSimulationQueueAdapter implements SimulationMessageQueuePort {

    private final RabbitTemplate rabbitTemplate;

    public static final String QUEUE_NAME = "simulation.queue";

    @Override
    public void enqueue(QueuedSimulationMessage message) {
        rabbitTemplate.convertAndSend(QUEUE_NAME, message);
    }
}
