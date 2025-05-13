package com.creditsimulator.unit;

import com.creditsimulator.application.message.QueuedSimulationMessage;
import com.creditsimulator.application.service.BatchSimulationProcessor;
import com.creditsimulator.application.service.EmailNotificationService;
import com.creditsimulator.domain.model.simulation.CreditSimulation;
import com.creditsimulator.domain.model.simulation.CreditSimulationCalculator;
import com.creditsimulator.domain.model.simulation.CreditSimulationJob;
import com.creditsimulator.domain.port.outgoing.CurrencyConversionClient;
import com.creditsimulator.domain.port.outgoing.SimulationJobPersistencePort;
import com.creditsimulator.domain.port.outgoing.SimulationResultPersistencePort;
import com.creditsimulator.shared.enums.SimulationJobStatus;
import com.creditsimulator.shared.enums.SimulationResultStatus;
import com.creditsimulator.shared.enums.TaxType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchSimulationProcessorTest {

    @Mock
    private CreditSimulationCalculator calculator;
    @Mock
    private CurrencyConversionClient currencyClient;
    @Mock
    private SimulationResultPersistencePort resultPersistence;
    @Mock
    private SimulationJobPersistencePort jobPersistence;
    @Mock
    private EmailNotificationService emailService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private BatchSimulationProcessor processor;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        processor = new BatchSimulationProcessor(
            calculator,
            currencyClient,
            resultPersistence,
            jobPersistence,
            emailService,
            objectMapper
        );
    }

    private final UUID jobId = UUID.randomUUID();

    private QueuedSimulationMessage validMessage() {
        return new QueuedSimulationMessage(
            jobId,
            new BigDecimal("10000.00"),
            24,
            LocalDate.of(1990, 5, 20),
            TaxType.AGE_BASED,
            null,
            "BRL"
        );
    }

    @Test
    void shouldSaveSuccessResultAndUpdateJob() throws Exception {
        CreditSimulation simulation = mock(CreditSimulation.class);
        when(calculator.calculate(any(), anyInt(), any(), any(), any())).thenReturn(simulation);

        var job = new CreditSimulationJob(
            jobId,
            SimulationJobStatus.PROCESSING, // ✅ importante!
            LocalDateTime.now(),
            null,
            1, 0, 0,
            "Fabiana", "fabiana@email.com"
        );

        when(jobPersistence.findById(jobId)).thenReturn(Optional.of(job));

        when(resultPersistence.findByJobId(jobId)).thenReturn(List.of());

        processor.process(validMessage());

        verify(resultPersistence).save(argThat(result ->
            result.status() == SimulationResultStatus.OK &&
                result.jobId().equals(jobId)
        ));

        verify(jobPersistence, atLeastOnce()).save(any());
        verify(emailService).sendJobResults(argThat(j -> j.status() == SimulationJobStatus.DONE), any());
    }

    @Test
    void shouldSaveErrorResultWhenSimulationFails() throws Exception {
        when(calculator.calculate(any(), anyInt(), any(), any(), any())).thenThrow(new RuntimeException("fail"));

        var job = new CreditSimulationJob(jobId, SimulationJobStatus.PROCESSING, LocalDateTime.now(), null, 1, 0, 0, "Fabiana", "fabiana@email.com");
        when(jobPersistence.findById(jobId)).thenReturn(Optional.of(job));

        processor.process(validMessage());

        verify(resultPersistence).save(argThat(result ->
            result.status() == SimulationResultStatus.ERROR &&
                result.jobId().equals(jobId)
        ));
    }

    @Test
    void shouldSendEmailWhenJobIsFinalized() throws Exception {
        CreditSimulation simulation = mock(CreditSimulation.class);
        when(calculator.calculate(any(), anyInt(), any(), any(), any())).thenReturn(simulation);

        var job = new CreditSimulationJob(
            jobId,
            SimulationJobStatus.PROCESSING,
            LocalDateTime.now(),
            null,
            1, // totalSimulations
            0, // successCount
            0, // errorCount
            "Fabiana",
            "fabiana@email.com"
        );

        when(jobPersistence.findById(jobId)).thenReturn(Optional.of(job));
        when(resultPersistence.findByJobId(jobId)).thenReturn(List.of());

        processor.process(validMessage());

        verify(emailService).sendJobResults(argThat(j -> j.status() == SimulationJobStatus.DONE), any());
    }

    @Test
    void shouldConvertCurrencyWhenCurrencyIsNotBRL() throws Exception {
        QueuedSimulationMessage message = new QueuedSimulationMessage(
            jobId, new BigDecimal("10000.00"), 24, LocalDate.of(1990, 5, 20),
            TaxType.AGE_BASED, null, "USD"
        );

        CreditSimulation original = mock(CreditSimulation.class);
        CreditSimulation converted = mock(CreditSimulation.class);

        when(calculator.calculate(any(), anyInt(), any(), any(), any())).thenReturn(original);
        when(original.convertTo(eq("USD"), any())).thenReturn(converted);

        var job = new CreditSimulationJob(jobId, SimulationJobStatus.PROCESSING, LocalDateTime.now(), null, 1, 0, 0, "Fabiana", "fabiana@email.com");
        when(jobPersistence.findById(jobId)).thenReturn(Optional.of(job));

        processor.process(message);

        verify(original).convertTo(eq("USD"), eq(currencyClient));
        verify(resultPersistence).save(any());
    }
}

