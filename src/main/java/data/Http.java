package data;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Shared HTTP client for all API requests.
 */
public class Http {

    /**
     * Verhindert Instanziierung der Utility-Klasse.
     */
    private Http() {}

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    /**
     * Liefert die gemeinsame {@link HttpClient}-Instanz für alle API-Aufrufe.
     *
     * @return wiederverwendbarer HTTP-Client
     */
    public static HttpClient client() {
        return CLIENT;
    }
}
