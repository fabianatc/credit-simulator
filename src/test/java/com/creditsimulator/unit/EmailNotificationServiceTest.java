package com.creditsimulator.unit;

import com.creditsimulator.application.service.EmailNotificationService;
import com.creditsimulator.domain.model.simulation.CreditSimulationJob;
import com.creditsimulator.domain.model.simulation.CreditSimulationResult;
import com.creditsimulator.shared.enums.SimulationJobStatus;
import com.creditsimulator.shared.enums.SimulationResultStatus;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificationService emailService;

    @Captor
    private ArgumentCaptor<MimeMessage> mimeMessageCaptor;

    private final CreditSimulationJob job = new CreditSimulationJob(
        UUID.randomUUID(),
        SimulationJobStatus.DONE,
        LocalDateTime.now(),
        LocalDateTime.now(),
        2, 1, 1,
        "Fabiana",
        "fabiana.costa@outlook.com"
    );

    private final CreditSimulationResult resultOk = new CreditSimulationResult(
        UUID.randomUUID(),
        job.id(),
        "{\"creditAmount\":10000}",
        "{\"totalAmount\":12345}",
        SimulationResultStatus.OK,
        null,
        LocalDateTime.now()
    );

    private final CreditSimulationResult resultError = new CreditSimulationResult(
        UUID.randomUUID(),
        job.id(),
        "{\"creditAmount\":invalid}",
        null,
        SimulationResultStatus.ERROR,
        "Invalid amount",
        LocalDateTime.now()
    );

    @Test
    void shouldSendEmailWithAttachmentSuccessfully() throws Exception {
        MimeMessage mockMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mockMessage);

        emailService.sendJobResults(job, List.of(resultOk, resultError));

        verify(mailSender).send(mockMessage);
    }

    @Test
    void shouldHandleMessagingExceptionGracefully() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class); // 👍 melhor opção

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("simulated failure"))
            .when(mailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> emailService.sendJobResults(job, List.of(resultOk)));
    }
}
