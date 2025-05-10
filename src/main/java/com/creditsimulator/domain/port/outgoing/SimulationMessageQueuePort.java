package com.creditsimulator.domain.port.outgoing;

import com.creditsimulator.application.message.QueuedSimulationMessage;

public interface SimulationMessageQueuePort {
    void enqueue(QueuedSimulationMessage message);
}
