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

class DelayStageServiceTest {

    private Javalin app;

    private final HttpClient client =
            HttpClient.newHttpClient();

    @BeforeEach
    void setUp() {
        app = DelayStageServiceApp.createApp();
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
    void shouldUpdateStage() throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                "http://localhost:"
                                        + app.port()
                                        + "/stage/5"))
                        .PUT(
                                HttpRequest.BodyPublishers.noBody()
                        )
                        .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        assertEquals(200, response.statusCode());
        assertTrue(
                response.body().contains("\"stage\":5")
        );
    }

    @Test
    void shouldRejectStageAbove8() throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                "http://localhost:"
                                        + app.port()
                                        + "/stage/9"))
                        .PUT(
                                HttpRequest.BodyPublishers.noBody()
                        )
                        .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        assertEquals(400, response.statusCode());
        assertEquals(
                "Stage must be between 0 and 8",
                response.body()
        );
    }

    @Test
    void shouldRejectNegativeStage() throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                "http://localhost:"
                                        + app.port()
                                        + "/stage/-1"))
                        .PUT(
                                HttpRequest.BodyPublishers.noBody()
                        )
                        .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        assertEquals(400, response.statusCode());
        assertEquals(
                "Stage must be between 0 and 8",
                response.body()
        );
    }
}