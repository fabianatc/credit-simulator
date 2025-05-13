package com.creditsimulator.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
class CreditSimulationIntegrationPerformanceTest extends BaseIntegrationTest {

    @Test
    void shouldRespondWithinExpectedTimeUnderLoad() {
        String json = """
                {
                  "creditAmount": 10000,
                  "termInMonths": 24,
                  "birthDate": "1990-05-20",
                  "taxType": "AGE_BASED"
                }
            """;

        int runs = 100;

        long total = 0;
        for (int i = 0; i < runs; i++) {
            long start = System.currentTimeMillis();

            given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/simulations")
                .then()
                .statusCode(200);

            total += (System.currentTimeMillis() - start);
        }

        double average = total / (double) runs;
        System.out.printf("✅ Média de tempo por requisição: %.2f ms%n", average);

        // opcional: assertiva de performance
        assertTrue(average < 200, "Requisição média demorou mais de 200ms");
    }
}