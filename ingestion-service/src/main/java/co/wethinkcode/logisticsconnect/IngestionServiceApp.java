package co.wethinkcode.logisticsconnect;

import java.util.Locale;
import com.opencsv.CSVReader;
import io.javalin.Javalin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IngestionServiceApp {

    public static void main(String[] args) {
        List<Hub> hubs = loadAndCleanHubs();
        Javalin app = Javalin.create().start(7050);

        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/hubs", ctx -> ctx.json(hubs));

        // TODO: read and clean src/main/resources/hubs-global.csv (hubs, sorting centers, regional districts data —
        // trim whitespace, fix casing, normalize dates/booleans) and expose the
        // cleaned records here for the other services to consume.
    }

    private static List<Hub> loadAndCleanHubs() {
        List<Hub> cleanedHubs = new ArrayList<>();
        Set<String> seenLocations = new HashSet<>();

        InputStream inputStream = IngestionServiceApp.class
                .getClassLoader()
                .getResourceAsStream("hubs-global.csv");

        if (inputStream == null) {
            throw new RuntimeException("hubs-global.csv was not found");
        }

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(inputStream))) {

            String[] row;

            // Skip the header row.
            reader.readNext();

            while ((row = reader.readNext()) != null) {

                if (row.length < 4) {
                    continue;
                }

                String hubId = normalizeHubId(row[0]);
                String province = normalizeProvince(row[1]);
                String sortingCenter = normalizeText(row[2]);
                Boolean active = normalizeBoolean(row[3]);

                // A record must have enough information to represent a hub.
                if (hubId == null || province == null
                        || sortingCenter == null) {
                    continue;
                }

                /*
                 * A province + sorting center represents one real-world entity.
                 * We keep the first cleaned record and discard later duplicates.
                 */
                String duplicateKey = province.toLowerCase(Locale.ROOT)
                        + "|"
                        + sortingCenter.toLowerCase(Locale.ROOT);

                if (seenLocations.contains(duplicateKey)) {
                    continue;
                }

                seenLocations.add(duplicateKey);

                cleanedHubs.add(new Hub(
                        hubId,
                        province,
                        sortingCenter,
                        active
                ));
            }

        } catch (Exception exception) {
            throw new RuntimeException(
                    "Failed to read and clean hubs-global.csv",
                    exception
            );
        }

        return cleanedHubs;
    }

    private static String normalizeHubId(String value) {
        String cleaned = normalizeText(value);

        if (cleaned == null) {
            return null;
        }

        return cleaned.toUpperCase(Locale.ROOT);
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value
                .trim()
                .replaceAll("\\s+", " ");

        if (cleaned.isBlank()
                || cleaned.equalsIgnoreCase("N/A")
                || cleaned.equalsIgnoreCase("unknown")
                || cleaned.equalsIgnoreCase("TBD")
                || cleaned.equals("-")
                || cleaned.equalsIgnoreCase("NaN")) {
            return null;
        }

        return cleaned;
    }

    private static String normalizeProvince(String value) {
        String cleaned = normalizeText(value);

        if (cleaned == null) {
            return null;
        }

        String normalized = cleaned
                .toLowerCase(Locale.ROOT)
                .replace("-", " ")
                .replaceAll("\\s+", " ")
                .trim();

        return switch (normalized) {
            case "gauteng" -> "Gauteng";
            case "western cape" -> "Western Cape";
            case "eastern cape" -> "Eastern Cape";
            case "northern cape" -> "Northern Cape";
            case "free state" -> "Free State";
            case "north west" -> "North West";
            case "mpumalanga" -> "Mpumalanga";
            case "limpopo" -> "Limpopo";
            case "kwazulu natal", "kwa zulu natal" -> "KwaZulu-Natal";
            default -> cleaned;
        };
    }

    private static Boolean normalizeBoolean(String value) {
        String cleaned = normalizeText(value);

        if (cleaned == null) {
            return null;
        }

        return switch (cleaned.toLowerCase(Locale.ROOT)) {
            case "y", "yes", "1", "true" -> true;
            case "n", "no", "0", "false" -> false;
            default -> null;
        };
    }
}
