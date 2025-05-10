package com.creditsimulator.application.service;

import com.creditsimulator.domain.model.simulation.CreditSimulationJob;
import com.creditsimulator.domain.model.simulation.CreditSimulationResult;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {
    private final JavaMailSender mailSender;

    public void sendJobResults(CreditSimulationJob job, List<CreditSimulationResult> results) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(job.requesterEmail());
            helper.setSubject("Resultado da simulação em lote [" + job.id() + "]");
            helper.setText(buildBody(job), false);

            String csvContent = buildCsv(results);
            helper.addAttachment("resultados-" + job.id() + ".csv",
                new ByteArrayResource(csvContent.getBytes(StandardCharsets.UTF_8)));

            mailSender.send(message);
            log.info("[EMAIL] Resultados enviados para {}", job.requesterEmail());

        } catch (MessagingException e) {
            log.error("[EMAIL] Falha ao enviar e-mail para {}: {}", job.requesterEmail(), e.getMessage());
        }
    }

    private String buildBody(CreditSimulationJob job) {
        return """
            Olá %s,
            
            O processamento do lote de simulações foi concluído.
            
            Total de simulações: %d
            Sucesso: %d
            Erros: %d
            
            Em anexo, você encontra o arquivo com os resultados completos.
            
            Obrigado.
            """.formatted(
            job.requesterName(),
            job.totalSimulations(),
            job.successCount(),
            job.errorCount()
        );
    }

    private String buildCsv(List<CreditSimulationResult> results) {
        String header = "id,input,output,status,errorMessage,createdAt";
        return header + "\n" + results.stream()
            .map(r -> String.join(",",
                r.id().toString(),
                escapeCsv(r.input()),
                escapeCsv(r.output()),
                r.status().name(),
                escapeCsv(r.errorMessage()),
                r.createdAt().toString()))
            .collect(Collectors.joining("\n"));
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}