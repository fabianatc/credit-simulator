package com.creditsimulator.integration;

import com.creditsimulator.domain.model.simulation.CreditSimulation;
import com.creditsimulator.domain.port.incoming.SimulateCreditUseCase;
import com.creditsimulator.shared.enums.TaxType;
import com.creditsimulator.shared.exception.BusinessValidationException;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
public class CreditSimulationControllerTest extends BaseIntegrationTest {
    @MockitoBean
    private SimulateCreditUseCase simulateCreditUseCase;

    @Test
    void shouldSimulateCreditWithAgeBasedTaxAndDefaultCurrency() {
        String json = """
            {
              "creditAmount": 10000.00,
              "termInMonths": 24,
              "birthDate": "1990-05-20",
              "taxType": "AGE_BASED"
            }
            """;

        CreditSimulation simulation = new CreditSimulation(
            new BigDecimal("10000.00"),
            24,
            LocalDate.of(1990, 5, 20),
            new BigDecimal("12254.70"),
            new BigDecimal("93.11"),
            new BigDecimal("254.70")
        );

        when(simulateCreditUseCase.simulate(
            new BigDecimal("10000.00"),
            24,
            LocalDate.of(1990, 5, 20),
            TaxType.AGE_BASED,
            null,
            null
        )).thenReturn(simulation);

        given()
            .contentType(ContentType.JSON)
            .body(json)
            .when()
            .post("/simulations")
            .then()
            .statusCode(200)
            .body("totalAmount", equalTo(12254.7f))
            .body("monthlyPayment", equalTo(93.11f));
    }

    @Test
    void shouldSimulateCreditWithFixedTaxAndCustomCurrency() {
        String json = """
            {
              "currency": "USD",
              "creditAmount": 15000.00,
              "termInMonths": 36,
              "birthDate": "1985-08-15",
              "taxType": "FIXED",
              "fixedTax": 0.045
            }
            """;

        CreditSimulation simulation = new CreditSimulation(
            new BigDecimal("15000.00"),
            36,
            LocalDate.of(1985, 8, 15),
            new BigDecimal("17000.00"),
            new BigDecimal("472.22"),
            new BigDecimal("2000.00")
        );

        when(simulateCreditUseCase.simulate(
            new BigDecimal("15000.00"),
            36,
            LocalDate.of(1985, 8, 15),
            TaxType.FIXED,
            new BigDecimal("0.045"),
            "USD"
        )).thenReturn(simulation);

        given()
            .contentType(ContentType.JSON)
            .body(json)
            .when()
            .post("/simulations")
            .then()
            .statusCode(200)
            .body("totalAmount", equalTo(17000.00f))
            .body("monthlyPayment", equalTo(472.22f))
            .body("currency", equalTo("USD"));
    }

    @Test
    void shouldReturn400WhenRequiredFieldsAreMissing() {
        String json = """
            {
              "currency": "USD"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(json)
            .when()
            .post("/simulations")
            .then()
            .statusCode(400)
            .body("message", containsString("creditAmount"))
            .body("message", containsString("termInMonths"))
            .body("message", containsString("birthDate"))
            .body("message", containsString("taxType"));
    }

    @Test
    void shouldReturn400WhenFixedTaxIsMissingForFixedType() {
        String json = """
            {
              "creditAmount": 8000.00,
              "termInMonths": 18,
              "birthDate": "1995-10-01",
              "taxType": "FIXED"
            }
            """;

        when(simulateCreditUseCase.simulate(
            new BigDecimal("8000.00"),
            18,
            LocalDate.of(1995, 10, 1),
            TaxType.FIXED,
            null,
            null
        )).thenThrow(new BusinessValidationException("Fixed tax must be provided when tax type is FIXED"));

        given()
            .contentType(ContentType.JSON)
            .body(json)
            .when()
            .post("/simulations")
            .then()
            .statusCode(400)
            .body("message", containsString("Fixed tax"));
    }
}
