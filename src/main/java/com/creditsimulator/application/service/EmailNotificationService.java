package com.creditsimulator.application.service;

import com.creditsimulator.domain.model.simulation.CreditSimulationJob;
import com.creditsimulator.domain.model.simulation.CreditSimulationResult;
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
            helper.setSubject("Credit simulation results [" + job.id() + "]");
            helper.setText(buildBody(job), false);

            String csvContent = buildCsv(results);
            helper.addAttachment("results-" + job.id() + ".csv",
                new ByteArrayResource(csvContent.getBytes(StandardCharsets.UTF_8)));

            mailSender.send(message);
            log.info("[EMAIL] Results sent to {}", job.requesterEmail());

        } catch (Exception e) {
            log.error("[EMAIL] Failed to send results to {}: {}", job.requesterEmail(), e.getMessage());
        }
    }

    private String buildBody(CreditSimulationJob job) {
        String base = """
            Hello %s,
            
            Your batch of credit simulations has been processed.
            
            Total simulations: %d
            Successful: %d
            Failed: %d
            """.formatted(
            job.requesterName(),
            job.totalSimulations(),
            job.successCount(),
            job.errorCount()
        );

        if (job.errorCount() > 0) {
            base += """
                
                ⚠️ Some simulations failed during processing.
                Please review the attached file for details (see the 'errorMessage' column).
                
                If needed, you can resubmit a new CSV file containing only the corrected records.
                """;
        }

        base += """
            
            Thank you for using the credit simulator.
            """;

        return base;
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