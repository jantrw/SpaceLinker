package commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

/**
 * Diese Klasse ruft das "Astronomy Picture of the Day" (APOD) von der NASA API ab
 * und speichert Titel, Beschreibung und Bild-URL.
 * <p>
 * Die Daten werden NICHT im Konstruktor geladen — rufe {@link #fetchData()} explizit auf.
 * </p>
 */
public class JSONFetcherNasa {

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private final String apiKey;
    private String title;
    private String explanation;
    private String url;
    private boolean fetched = false;

    public JSONFetcherNasa(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Holt die aktuellen APOD-Daten von der NASA API und speichert sie.
     * Kann mehrfach aufgerufen werden, um Daten zu aktualisieren.
     */
    public void fetchData() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.nasa.gov/planetary/apod?api_key=" + apiKey))
                    .header("accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

            this.title = jsonObject.has("title") ? jsonObject.get("title").getAsString() : "Kein Titel verfügbar";
            this.explanation = jsonObject.has("explanation") ? jsonObject.get("explanation").getAsString() : "Keine Erklärung verfügbar";
            this.url = jsonObject.has("url") ? jsonObject.get("url").getAsString() : "";
            this.fetched = true;

        } catch (Exception e) {
            System.err.println("Fehler beim Abrufen der NASA-Daten: " + e.getMessage());
            this.title = "Fehler";
            this.explanation = "Fehler beim Laden der Daten";
            this.url = "";
            this.fetched = false;
        }
    }

    /**
     * Stellt sicher, dass Daten geladen sind, bevor Getter aufgerufen werden.
     */
    private void ensureFetched() {
        if (!fetched) {
            fetchData();
        }
    }

    public String getTitle() {
        ensureFetched();
        return title;
    }

    public String getExplanation() {
        ensureFetched();
        return explanation;
    }

    public String getUrl() {
        ensureFetched();
        return url;
    }
}
