package com.creditsimulator.integration;

import com.creditsimulator.application.service.BatchSimulationExportService;
import com.creditsimulator.domain.model.simulation.CreditSimulationJob;
import com.creditsimulator.domain.port.outgoing.SimulationJobPersistencePort;
import com.creditsimulator.domain.port.outgoing.SimulationResultPersistencePort;
import com.creditsimulator.shared.enums.SimulationJobStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testintegration")
public class BatchSimulationQueryControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BatchSimulationExportService exportService;

    @MockitoBean
    private SimulationJobPersistencePort jobPersistence;

    @MockitoBean
    private SimulationResultPersistencePort resultPersistence;

    @Test
    void shouldReturn404WhenJobDoesNotExist() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(exportService.getIfCompleted(jobId)).thenReturn(Optional.empty());
        when(exportService.jobExists(jobId)).thenReturn(false);

        mockMvc.perform(get("/simulations/batch/" + jobId + "/csv"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenJobNotCompleted() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(exportService.getIfCompleted(jobId)).thenReturn(Optional.empty());
        when(exportService.jobExists(jobId)).thenReturn(true);

        mockMvc.perform(get("/simulations/batch/" + jobId + "/csv"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnCsvWhenJobIsDone() throws Exception {
        UUID jobId = UUID.randomUUID();

        CreditSimulationJob doneJob = new CreditSimulationJob(
            jobId, SimulationJobStatus.DONE, LocalDateTime.now(),
            LocalDateTime.now(), 1, 1, 0, "Fabiana", "fabiana@email.com"
        );

        when(exportService.getIfCompleted(jobId)).thenReturn(Optional.of(doneJob));

        mockMvc.perform(get("/simulations/batch/" + jobId + "/csv"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv"))
            .andExpect(header().string("Content-Disposition",
                org.hamcrest.Matchers.containsString("attachment")));
    }
}


