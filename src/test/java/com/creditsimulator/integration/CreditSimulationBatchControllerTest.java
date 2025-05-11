package com.creditsimulator.integration;

import com.creditsimulator.domain.port.incoming.BatchSimulationUseCase;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.org.hamcrest.Matchers;

import java.io.IOException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testintegration")
public class CreditSimulationBatchControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private BatchSimulationUseCase batchSimulationUseCase;

    @Test
    void shouldUploadValidCsvSuccessfully() throws Exception {
        // Arrange
        UUID fakeJobId = UUID.randomUUID();
        Mockito.when(batchSimulationUseCase.process(
                any(), anyString(), anyString()))
            .thenReturn(fakeJobId);

        String validCsv = """
                creditAmount,termInMonths,birthDate,taxType,fixedTax,currency
                10000,24,1990-05-20,AGE_BASED,,USD
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "valid.csv", "text/csv", validCsv.getBytes()
        );

        mockMvc.perform(multipart("/simulations/batch/upload")
                .file(file)
                .param("requesterName", "Fabiana Costa")
                .param("requesterEmail", "fabiana@email.com")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.jobId").value(fakeJobId.toString()))
            .andExpect(jsonPath("$.emailNotification").value(true))
            .andExpect(jsonPath("$.message").value(Matchers.containsString("email")))
            .andExpect(jsonPath("$.resultUrl").value(Matchers.containsString("/simulations/batch/")));
    }

    @Test
    void shouldReturn400WhenCsvHeadersAreMissing() throws Exception {
        // Arrange: use case lança IllegalArgumentException com "headers"
        Mockito.when(batchSimulationUseCase.process(
                any(), anyString(), anyString()))
            .thenThrow(new IllegalArgumentException("Missing required headers"));

        String invalidCsv = """
                amount,term,date
                10000,24,1990-05-20
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "invalid.csv", "text/csv", invalidCsv.getBytes()
        );

        mockMvc.perform(multipart("/simulations/batch/upload")
                .file(file)
                .param("requesterName", "Fabiana Costa")
                .param("requesterEmail", "fabiana@email.com")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isBadRequest())
            .andExpect(content().string((Matcher<? super String>) Matchers.containsString("headers")));
    }

    @Test
    void shouldAcceptCsvWithInvalidLinesButProcessOthers() throws Exception {
        String csvWithInvalidLine = """
            creditAmount,termInMonths,birthDate,taxType,fixedTax,currency
            10000,24,1990-05-20,AGE_BASED,,USD
            15000,36,1985-10-10,INVALID_TYPE,0.03,EUR
            """;

        UUID jobId = UUID.randomUUID();
        Mockito.when(batchSimulationUseCase.process(any(), anyString(), anyString()))
            .thenReturn(jobId);

        MockMultipartFile file = new MockMultipartFile(
            "file", "mix.csv", "text/csv", csvWithInvalidLine.getBytes()
        );

        mockMvc.perform(multipart("/simulations/batch/upload")
                .file(file)
                .param("requesterName", "Fabiana")
                .param("requesterEmail", "fabiana@email.com"))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.jobId").value(jobId.toString()));
    }

    @Test
    void shouldReturn400WhenCsvExceedsMaxLines() throws Exception {
        StringBuilder csvBuilder = new StringBuilder("creditAmount,termInMonths,birthDate,taxType,fixedTax,currency\n");
        for (int i = 0; i <= 10001; i++) {
            csvBuilder.append("10000,24,1990-05-20,AGE_BASED,,USD\n");
        }

        MockMultipartFile file = new MockMultipartFile(
            "file", "too-big.csv", "text/csv", csvBuilder.toString().getBytes()
        );

        Mockito.when(batchSimulationUseCase.process(any(), anyString(), anyString()))
            .thenThrow(new IllegalArgumentException("Maximum allowed lines exceeded"));

        mockMvc.perform(multipart("/simulations/batch/upload")
                .file(file)
                .param("requesterName", "Fabiana")
                .param("requesterEmail", "fabiana@email.com"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string((Matcher<? super String>) Matchers.containsString("Maximum allowed lines")));
    }

    @Test
    void shouldReturn400WhenCsvCannotBeParsed() throws Exception {
        byte[] corruptedContent = new byte[]{0x13, 0x37, 0x00, 0x01}; // binário inválido

        MockMultipartFile file = new MockMultipartFile(
            "file", "corrupted.csv", "text/csv", corruptedContent
        );

        Mockito.when(batchSimulationUseCase.process(any(), anyString(), anyString()))
            .thenThrow(new IOException("Failed to parse CSV"));

        mockMvc.perform(multipart("/simulations/batch/upload")
                .file(file)
                .param("requesterName", "Fabiana")
                .param("requesterEmail", "fabiana@email.com"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string((Matcher<? super String>) Matchers.containsString("Failed to parse CSV")));
    }
}