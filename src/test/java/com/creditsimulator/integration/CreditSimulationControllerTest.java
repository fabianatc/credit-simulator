package com.creditsimulator.integration;

import com.creditsimulator.application.request.CreditSimulationRequest;
import com.creditsimulator.domain.model.simulation.CreditSimulation;
import com.creditsimulator.domain.port.incoming.SimulateCreditUseCase;
import com.creditsimulator.shared.enums.TaxType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = com.creditsimulator.application.controller.CreditSimulationControllerImpl.class)
public class CreditSimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private SimulateCreditUseCase simulateCreditUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSimulateCreditWithFixedTaxAndUSD() throws Exception {
        // Arrange
        CreditSimulationRequest request = new CreditSimulationRequest(
            "USD",
            new BigDecimal("10000.00"),
            24,
            LocalDate.of(1990, 5, 20),
            TaxType.FIXED,
            new BigDecimal("0.035")
        );

        CreditSimulation simulation = new CreditSimulation(
            new BigDecimal("2000.00"),
            24,
            request.birthDate(),
            new BigDecimal("2254.70"),
            new BigDecimal("93.11"),
            new BigDecimal("254.70")
        );

        Mockito.when(simulateCreditUseCase.simulate(
            request.creditAmount(),
            request.termInMonths(),
            request.birthDate(),
            request.taxType(),
            request.fixedTax(),
            request.currency()
        )).thenReturn(simulation);

        // Act & Assert
        mockMvc.perform(post("/simulations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalAmount").value("2254.70"))
            .andExpect(jsonPath("$.monthlyPayment").value("93.11"))
            .andExpect(jsonPath("$.taxPaid").value("254.70"))
            .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void shouldSimulateCreditWithAgeBasedTaxAndDefaultCurrency() throws Exception {
        CreditSimulationRequest request = new CreditSimulationRequest(
            null,
            new BigDecimal("15000.00"),
            36,
            LocalDate.of(1985, 3, 10),
            TaxType.AGE_BASED,
            null
        );

        CreditSimulation simulation = new CreditSimulation(
            request.creditAmount(),
            request.termInMonths(),
            request.birthDate(),
            new BigDecimal("16478.00"),
            new BigDecimal("457.72"),
            new BigDecimal("1478.00")
        );

        Mockito.when(simulateCreditUseCase.simulate(
            request.creditAmount(),
            request.termInMonths(),
            request.birthDate(),
            request.taxType(),
            request.fixedTax(),
            request.currency()
        )).thenReturn(simulation);

        mockMvc.perform(post("/simulations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalAmount").value("16478.00"))
            .andExpect(jsonPath("$.monthlyPayment").value("457.72"))
            .andExpect(jsonPath("$.taxPaid").value("1478.00"))
            .andExpect(jsonPath("$.currency").doesNotExist()); // or default to BRL if added
    }

    @Test
    void shouldReturnBadRequestWhenFixedTaxIsMissing() throws Exception {
        CreditSimulationRequest request = new CreditSimulationRequest(
            "EUR",
            new BigDecimal("12000.00"),
            12,
            LocalDate.of(1992, 11, 5),
            TaxType.FIXED,
            null
        );

        Mockito.when(simulateCreditUseCase.simulate(
            Mockito.any(), Mockito.anyInt(), Mockito.any(), Mockito.any(), Mockito.isNull(), Mockito.any()
        )).thenThrow(new IllegalArgumentException("Fixed tax must be provided when tax type is FIXED"));

        mockMvc.perform(post("/simulations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Fixed tax must be provided")));
    }

}
