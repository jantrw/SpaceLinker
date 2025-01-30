package commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

/**
 * Diese Klasse ruft das "Astronomy Picture of the Day" (APOD) von der NASA API ab
 * und speichert Titel, Beschreibung und Bild-URL.
 */
public class JSONFetcherNasa {

    private String title;
    private String explanation;
    private String url;

    /**
     * Konstruktor, der automatisch die APOD-Daten  abruft.
     *
     * @param apiKey Der API-Schlüssel für die NASA-API.
     */
    public JSONFetcherNasa(String apiKey) {
        fetchData(apiKey);
    }

    /**
     * Holt die aktuellen APOD-Daten von der NASA API und speichert sie.
     *
     * @param apiKey Der API-Schlüssel für den Zugriff auf die API.
     */
    private void fetchData(String apiKey) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.nasa.gov/planetary/apod?api_key=" + apiKey))
                    .header("accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

            this.title = jsonObject.has("title") ? jsonObject.get("title").getAsString() : "Kein Titel verfügbar";
            this.explanation = jsonObject.has("explanation") ? jsonObject.get("explanation").getAsString() : "Keine Erklärung verfügbar";
            this.url = jsonObject.has("url") ? jsonObject.get("url").getAsString() : "Keine URL verfügbar";

        } catch (IOException | InterruptedException e) {
            System.err.println("Fehler beim Abrufen der NASA-Daten: " + e.getMessage());
            this.title = "Fehler";
            this.explanation = "Fehler beim Laden der Daten";
            this.url = "";
        }
    }

    // Getter-Methoden
    public String getTitle() {
        return title;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getUrl() {
        return url;
    }
}
