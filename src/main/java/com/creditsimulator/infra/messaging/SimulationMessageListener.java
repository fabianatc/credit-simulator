package com.creditsimulator.infra.messaging;

import com.creditsimulator.application.message.QueuedSimulationMessage;
import com.creditsimulator.application.service.BatchSimulationProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimulationMessageListener {

    private final BatchSimulationProcessor processor;

    @RabbitListener(queues = RabbitSimulationQueueAdapter.QUEUE_NAME)
    public void receive(QueuedSimulationMessage message) {
        log.info("[RABBIT] Received simulation message for job {}", message.jobId());
        processor.process(message);
    }
}