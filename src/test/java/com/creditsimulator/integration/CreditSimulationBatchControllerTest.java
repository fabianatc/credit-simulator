package com.creditsimulator.integration;

import com.creditsimulator.application.response.BatchSimulationResponse;
import com.creditsimulator.domain.port.incoming.BatchSimulationUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
public class CreditSimulationBatchControllerTest extends BaseIntegrationTest {
    @MockitoBean
    BatchSimulationUseCase batchSimulationUseCase;

    @Test
    void shouldAcceptCsvUploadAndReturnJobId() throws Exception {
        // simula um CSV válido
        String csv = "creditAmount,termInMonths,birthDate,taxType,fixedTax,currency\n"
            + "10000,24,1990-05-20,AGE_BASED,,BRL\n";

        MockMultipartFile file = new MockMultipartFile(
            "file", "simulations.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        String requesterName = "Fabiana Costa";
        String requesterEmail = "fabiana.costa@outlook.com";
        UUID jobId = UUID.randomUUID();

        BatchSimulationResponse expectedResponse = new BatchSimulationResponse(
            jobId,
            "Your batch is being processed. Results will be sent to your email when ready.",
            true,
            "/simulations/batch/" + jobId + "/csv"
        );

        when(batchSimulationUseCase.process(any(), eq(requesterName), eq(requesterEmail)))
            .thenReturn(jobId);

        given()
            .multiPart("file", file.getOriginalFilename(), file.getBytes(), "text/csv")
            .formParam("requesterName", requesterName)
            .formParam("requesterEmail", requesterEmail)
            .when()
            .post("/simulations/batch/upload")
            .then()
            .statusCode(202)
            .body("jobId", equalTo(jobId.toString()))
            .body("message", equalTo(expectedResponse.message()))
            .body("emailNotification", equalTo(true))
            .body("resultUrl", equalTo(expectedResponse.resultUrl()));
    }

    @Test
    void shouldReturn400WhenCsvIsInvalid() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "invalid.txt", "text/plain", "invalid content".getBytes(StandardCharsets.UTF_8)
        );

        when(batchSimulationUseCase.process(any(), any(), any()))
            .thenThrow(new IllegalArgumentException("Invalid file: must be a non-empty CSV file."));

        given()
            .multiPart("file", file.getOriginalFilename(), file.getBytes(), "text/csv")
            .formParam("requesterName", "Fabiana")
            .formParam("requesterEmail", "fabiana@email.com")
            .when()
            .post("/simulations/batch/upload")
            .then()
            .statusCode(400)
            .body("message", containsString("Invalid"));
    }
}