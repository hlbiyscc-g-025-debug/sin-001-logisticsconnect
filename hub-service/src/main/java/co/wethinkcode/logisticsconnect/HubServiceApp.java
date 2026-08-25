package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class HubServiceApp {

    private static final String INGESTION_SERVICE_URL = "http://localhost:7050/hubs";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7051);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO (Serves provinces and sorting centers (place-name source of truth).)
        // Add domain endpoints for hub-service here.
        app.get("/hubs", ctx -> {
            try {
                List<Hub> hubs = getHubsFromIngestionService();
                ctx.json(hubs);
            } catch (Exception e) {
                ctx.status(500);
                ctx.json(new ErrorResponse("Could not retrieve hubs from ingestion service"));
            }
        });
        app.get("/hubs/{hubId}", ctx -> {

            String requestedHubId = ctx.pathParam("hubId");

            try {
                List<Hub> hubs = getHubsFromIngestionService();

                Hub hub = hubs.stream()
                        .filter(item ->
                                item.getHubId().equalsIgnoreCase(requestedHubId))
                        .findFirst()
                        .orElse(null);

                if (hub == null) {
                    ctx.status(404);
                    ctx.json(new ErrorResponse("Hub not found"));
                    return;
                }

                ctx.json(hub);

            } catch (Exception e) {
                ctx.status(500);
                ctx.json(new ErrorResponse(
                        "Could not retrieve hub information"
                ));
            }
        });

    }
    private static List<Hub> getHubsFromIngestionService() throws Exception {

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(INGESTION_SERVICE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "Ingestion service returned status: "
                            + response.statusCode()
            );
        }

        return OBJECT_MAPPER.readValue(
                response.body(),
                new TypeReference<List<Hub>>() {}
        );
    }
    private static class ErrorResponse {

        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
