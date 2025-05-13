package com.creditsimulator.integration;

import com.creditsimulator.application.service.BatchSimulationExportService;
import com.creditsimulator.domain.model.simulation.CreditSimulationJob;
import com.creditsimulator.domain.model.simulation.CreditSimulationResult;
import com.creditsimulator.domain.port.outgoing.SimulationJobPersistencePort;
import com.creditsimulator.domain.port.outgoing.SimulationResultPersistencePort;
import com.creditsimulator.shared.enums.SimulationJobStatus;
import com.creditsimulator.shared.enums.SimulationResultStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
public class BatchSimulationQueryControllerTest extends BaseIntegrationTest {
    @MockitoBean
    private BatchSimulationExportService exportService;

    @MockitoBean
    private SimulationJobPersistencePort jobPersistence;

    @MockitoBean
    private SimulationResultPersistencePort resultPersistence;

    @Test
    void shouldReturnCsvWhenJobIsDone() throws Exception {
        UUID jobId = UUID.randomUUID();

        var job = new CreditSimulationJob(jobId,
            SimulationJobStatus.DONE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            3,
            3,
            0,
            "Fabiana Costa",
            "fabiana.costa@outlook.com");
        var result = new CreditSimulationResult(
            UUID.randomUUID(),
            jobId,
            "{\"creditAmount\":10000}",
            "{\"monthlyPayment\":500}",
            SimulationResultStatus.OK,
            null,
            LocalDateTime.now()
        );

        when(exportService.getIfCompleted(jobId)).thenReturn(Optional.of(job));
        when(exportService.writeCsvResponse(jobId)).thenCallRealMethod();
        when(exportService.findByJobId(jobId)).thenReturn(List.of(result));

        given()
            .when()
            .get("/simulations/batch/{jobId}/csv", jobId)
            .then()
            .statusCode(200)
            .header("Content-Disposition", containsString("attachment"))
            .contentType("application/octet-stream")
            .body(containsString("creditAmount"))
            .body(containsString("monthlyPayment"));
    }

    @Test
    void shouldReturn400IfJobExistsButIsNotCompleted() {
        UUID jobId = UUID.randomUUID();

        var job = new CreditSimulationJob(
            jobId,
            SimulationJobStatus.PROCESSING,
            LocalDateTime.now(),
            LocalDateTime.now(),
            3,
            1,
            0,
            "Fabiana Costa",
            "fabiana.costa@outlook.com");

        when(exportService.getIfCompleted(jobId)).thenReturn(Optional.empty());
        when(exportService.jobExists(jobId)).thenReturn(true);

        given()
            .when()
            .get("/simulations/batch/{jobId}/csv", jobId)
            .then()
            .statusCode(400);
    }

    @Test
    void shouldReturn404IfJobDoesNotExist() {
        UUID jobId = UUID.randomUUID();

        when(exportService.getIfCompleted(jobId)).thenReturn(Optional.empty());
        when(exportService.jobExists(jobId)).thenReturn(false);

        given()
            .when()
            .get("/simulations/batch/{jobId}/csv", jobId)
            .then()
            .statusCode(404);
    }
}


