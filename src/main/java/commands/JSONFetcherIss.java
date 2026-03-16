package commands;

import data.Config;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Die Klasse {@code JSONFetcherIss} ruft aktuelle Daten zur Internationalen Raumstation (ISS) von verschiedenen APIs ab.
 * Sie liefert Informationen zur Position, Geschwindigkeit, Höhe, dem aktuellen Land oder Ozean sowie der zugehörigen Zeitzone.
 * <p>
 * Die Daten müssen über die Methode {@link #fetchAllData()} abgerufen werden, bevor sie durch Getter-Methoden verfügbar sind.
 * </p>
 */
public class JSONFetcherIss {

    private static final String NORAD_ID = "25544"; // ISS NORAD-Katalog-ID
    private static final String DEFAULT_VALUE = "??";
    private static final Duration TIMEOUT = Duration.ofSeconds(8);

    // Shared HttpClient für alle Anfragen
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(16))
            .build();

    // ISS-Datenvariablen
    private String longitude, latitude, timezone_id, country, city, state, mapUrl, ocean;
    private double velocity, altitude;
    private final String username;

    public JSONFetcherIss() {
        this.username = Config.get("username", "");
    }

    /**
     * Ruft alle relevanten ISS-Daten von externen APIs ab.
     * Die einzelnen Fetch-Schritte prüfen auf erfolgreiche Vorergebnisse.
     *
     * @return true, wenn alle Daten erfolgreich abgerufen wurden.
     */
    public boolean fetchAllData() {
        fetchLocation();

        if (latitude == null || longitude == null) {
            System.err.println("ISS-Position konnte nicht abgerufen werden.");
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
            JSONObject json = new JSONObject(response.body());

            JSONObject data = json.getJSONObject("iss_position");
            this.latitude = data.getString("latitude");
            this.longitude = data.getString("longitude");

        } catch (Exception e) {
            System.err.println("Fehler beim Abrufen der ISS-Position: " + e.getMessage());
            this.latitude = null;
            this.longitude = null;
        }
    }

    public void fetchSpeedHeight() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.wheretheiss.at/v1/satellites/" + NORAD_ID))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());

            this.velocity = json.getDouble("velocity");
            this.altitude = json.getDouble("altitude");

        } catch (Exception e) {
            System.err.println("Fehler beim Abrufen der ISS-Geschwindigkeit/Höhe: " + e.getMessage());
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
            JSONObject json = new JSONObject(response.body());

            this.timezone_id = json.optString("timezone_id", DEFAULT_VALUE);
            this.mapUrl = json.optString("map_url", "");

        } catch (Exception e) {
            System.err.println("Fehler beim Abrufen der Zeitzone/Karte: " + e.getMessage());
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
            JSONObject json = new JSONObject(response.body());

            if (json.has("ocean")) {
                JSONObject oceanObj = json.getJSONObject("ocean");
                this.ocean = oceanObj.optString("name", DEFAULT_VALUE);
            } else {
                this.ocean = "Die ISS ist über einem Land";
            }

        } catch (Exception e) {
            System.err.println("Fehler beim Abrufen der Ozean-Daten: " + e.getMessage());
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
            JSONObject json = new JSONObject(response.body());

            if (json.has("address")) {
                JSONObject address = json.getJSONObject("address");
                this.country = address.optString("country", DEFAULT_VALUE);
                this.state = address.optString("state", DEFAULT_VALUE);
                this.city = address.optString("city", address.optString("town", address.optString("village", DEFAULT_VALUE)));
            } else {
                this.country = DEFAULT_VALUE;
                this.state = DEFAULT_VALUE;
                this.city = DEFAULT_VALUE;
            }

        } catch (Exception e) {
            System.err.println("Fehler beim Abrufen der Land-Daten: " + e.getMessage());
            this.country = DEFAULT_VALUE;
            this.state = DEFAULT_VALUE;
            this.city = DEFAULT_VALUE;
        }
    }

    // Getter-Methoden

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
