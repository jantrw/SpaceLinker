package data;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Shared HTTP client for all API requests.
 */
public class Http {

    private Http() {}

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public static HttpClient client() {
        return CLIENT;
    }
}
