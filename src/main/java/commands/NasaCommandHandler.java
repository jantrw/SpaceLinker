package commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Zentrale Verarbeitung für NASA Picture of the Day.
 * Wird von sowohl Slash-Commands als auch Text-Befehlen verwendet.
 */
public class NasaCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(NasaCommandHandler.class);

    private static final int MAX_EMBED_TITLE = 256;
    private static final int MAX_EMBED_DESCRIPTION = 4096;
    private static final Duration TIMEOUT = Duration.ofSeconds(8);

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final String apiKey;

    public NasaCommandHandler() {
        this.apiKey = data.Config.get("apiKeyNasa", "DEMO_KEY");
    }

    /**
     * Erstellt einen Embed für das NASA-Bild (nur Bild + Titel).
     */
    public EmbedBuilder buildPictureEmbed() {
        JsonObject data = fetchApodData();
        if (data == null) {
            return new EmbedBuilder()
                    .setTitle("Fehler")
                    .setDescription("NASA-Daten konnten nicht geladen werden.");
        }

        return new EmbedBuilder()
                .setTitle(truncate(getString(data, "title", "Kein Titel verfügbar"), MAX_EMBED_TITLE))
                .setImage(getString(data, "url", ""));
    }

    /**
     * Erstellt einen Embed für das NASA-Bild mit Beschreibung.
     */
    public EmbedBuilder buildPictureInfoEmbed() {
        JsonObject data = fetchApodData();
        if (data == null) {
            return new EmbedBuilder()
                    .setTitle("Fehler")
                    .setDescription("NASA-Daten konnten nicht geladen werden.");
        }

        return new EmbedBuilder()
                .setTitle(truncate(getString(data, "title", "Kein Titel verfügbar"), MAX_EMBED_TITLE))
                .setDescription(truncate(getString(data, "explanation", "Keine Erklärung verfügbar"), MAX_EMBED_DESCRIPTION))
                .setImage(getString(data, "url", ""));
    }

    private JsonObject fetchApodData() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.nasa.gov/planetary/apod?api_key=" + apiKey))
                    .header("accept", "application/json")
                    .timeout(TIMEOUT)
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return JsonParser.parseString(response.body()).getAsJsonObject();

        } catch (Exception e) {
            log.error("Fehler beim Abrufen der NASA-Daten", e);
            return null;
        }
    }

    private String getString(JsonObject obj, String key, String defaultValue) {
        return obj.has(key) ? obj.get(key).getAsString() : defaultValue;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
