package commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import data.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Ruft aktuelle Daten zur ISS von verschiedenen APIs ab.
 */
public class JSONFetcherIss {

    private static final Logger log = LoggerFactory.getLogger(JSONFetcherIss.class);

    private static final String NORAD_ID = "25544";
    private static final String DEFAULT_VALUE = "??";
    private static final Duration TIMEOUT = Duration.ofSeconds(8);

    private static final HttpClient httpClient = HttpClient.newBuilder()
            // Iss braucht lange zum Antworten, falls es keine Antwort schickt ggf. länger warten
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private String longitude, latitude, timezone_id, country, city, state, mapUrl, ocean;
    private double velocity, altitude;
    private final String username;

    public JSONFetcherIss() {
        this.username = Config.get("username", "");
    }

    public boolean fetchAllData() {
        fetchLocation();

        if (latitude == null || longitude == null) {
            log.error("ISS-Position konnte nicht abgerufen werden.");
            return false;
        }

        fetchSpeedHeight();
        fetchMapUrlTimeZone();
        fetchCountry();
        fetchOcean();
        return true;
    }

    public void fetchLocation() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://api.open-notify.org/iss-now.json"))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject data = json.getAsJsonObject("iss_position");

            this.latitude = data.get("latitude").getAsString();
            this.longitude = data.get("longitude").getAsString();

        } catch (Exception e) {
            log.error("Fehler beim Abrufen der ISS-Position", e);
            this.latitude = null;
            this.longitude = null;
        }
    }

    public void fetchSpeedHeight() {
        if (latitude == null || longitude == null) return;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.wheretheiss.at/v1/satellites/" + NORAD_ID))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

            this.velocity = json.get("velocity").getAsDouble();
            this.altitude = json.get("altitude").getAsDouble();

        } catch (Exception e) {
            log.error("Fehler beim Abrufen der ISS-Geschwindigkeit/Höhe", e);
            this.velocity = 0;
            this.altitude = 0;
        }
    }

    public void fetchMapUrlTimeZone() {
        if (latitude == null || longitude == null) return;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.wheretheiss.at/v1/coordinates/" + this.latitude + "," + this.longitude))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

            this.timezone_id = optString(json, "timezone_id", DEFAULT_VALUE);
            this.mapUrl = optString(json, "map_url", "");

        } catch (Exception e) {
            log.error("Fehler beim Abrufen der Zeitzone/Karte", e);
            this.timezone_id = DEFAULT_VALUE;
            this.mapUrl = "";
        }
    }

    public void fetchOcean() {
        if (latitude == null || longitude == null) return;
        if (username == null || username.isBlank()) {
            this.ocean = "GeoNames-Benutzername nicht konfiguriert";
            return;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://api.geonames.org/extendedFindNearbyJSON?lat=" + this.latitude + "&lng=" + this.longitude + "&username=" + this.username))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

            if (json.has("ocean")) {
                this.ocean = optString(json.getAsJsonObject("ocean"), "name", DEFAULT_VALUE);
            } else {
                this.ocean = "Die ISS ist über einem Land";
            }

        } catch (Exception e) {
            log.error("Fehler beim Abrufen der Ozean-Daten", e);
            this.ocean = DEFAULT_VALUE;
        }
    }

    public void fetchCountry() {
        if (latitude == null || longitude == null) return;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://nominatim.openstreetmap.org/reverse?lat=" + this.latitude + "&lon=" + this.longitude + "&format=json"))
                    .header("User-Agent", "SpaceLinker-Discord-Bot/1.0")
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

            if (json.has("address")) {
                JsonObject address = json.getAsJsonObject("address");
                this.country = optString(address, "country", DEFAULT_VALUE);
                this.state = optString(address, "state", DEFAULT_VALUE);
                this.city = optString(address, "city",
                        optString(address, "town",
                                optString(address, "village", DEFAULT_VALUE)));
            } else {
                this.country = DEFAULT_VALUE;
                this.state = DEFAULT_VALUE;
                this.city = DEFAULT_VALUE;
            }

        } catch (Exception e) {
            log.error("Fehler beim Abrufen der Land-Daten", e);
            this.country = DEFAULT_VALUE;
            this.state = DEFAULT_VALUE;
            this.city = DEFAULT_VALUE;
        }
    }

    /**
     * Sichere String-Extraktion aus einem JsonObject (Gson hat kein optString).
     */
    private String optString(JsonObject obj, String key, String defaultValue) {
        return (obj.has(key) && !obj.get(key).isJsonNull()) ? obj.get(key).getAsString() : defaultValue;
    }

    // Getter

    public String getLongitude() { return longitude; }
    public String getLatitude() { return latitude; }
    public String getTimezone_id() { return timezone_id; }
    public String getCountry() { return country; }
    public String getMapUrl() { return mapUrl; }
    public double getVelocity() { return velocity; }
    public double getAltitude() { return altitude; }
    public String getOcean() { return ocean; }
    public String getState() { return state; }
    public String getCity() { return city; }
}
