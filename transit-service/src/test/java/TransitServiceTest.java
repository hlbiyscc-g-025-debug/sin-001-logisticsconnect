package co.wethinkcode.logisticsconnect;

import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransitServiceTest {

    private Javalin app;

    private final HttpClient client =
            HttpClient.newHttpClient();

    @BeforeEach
    void setUp() {

        TransitServiceApp.resetStage();

        app = TransitServiceApp.createApp();
        app.start(0);
    }

    @AfterEach
    void tearDown() {
        app.stop();
    }

    @Test
    void shouldReturnInitialStage() throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                "http://localhost:"
                                        + app.port()
                                        + "/stage"))
                        .GET()
                        .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        assertEquals(200, response.statusCode());

        assertTrue(
                response.body().contains("\"stage\":0")
        );
    }

    @Test
    void shouldUpdateStageFromMessage() throws Exception {

        TransitServiceApp.updateStageFromMessage(
                "{\"stage\":6}"
        );

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                "http://localhost:"
                                        + app.port()
                                        + "/stage"))
                        .GET()
                        .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        assertEquals(200, response.statusCode());

        assertTrue(
                response.body().contains("\"stage\":6")
        );
    }

    @Test
    void shouldCalculateEtaForStageZero() throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                "http://localhost:"
                                        + app.port()
                                        + "/eta/H-500"))
                        .GET()
                        .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        assertEquals(200, response.statusCode());

        assertTrue(
                response.body().contains("\"hubId\":\"H-500\"")
        );

        assertTrue(
                response.body().contains("\"stage\":0")
        );

        assertTrue(
                response.body().contains("\"etaMinutes\":30")
        );
    }

    @Test
    void shouldCalculateEtaForStageSix() throws Exception {

        TransitServiceApp.updateStageFromMessage(
                "{\"stage\":6}"
        );

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                "http://localhost:"
                                        + app.port()
                                        + "/eta/H-500"))
                        .GET()
                        .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        assertEquals(200, response.statusCode());

        assertTrue(
                response.body().contains("\"hubId\":\"H-500\"")
        );

        assertTrue(
                response.body().contains("\"stage\":6")
        );

        assertTrue(
                response.body().contains("\"etaMinutes\":90")
        );
    }
}
