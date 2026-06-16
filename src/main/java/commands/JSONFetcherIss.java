package commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import data.Config;
import data.Http;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
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
    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    private String longitude, latitude, timezone_id, country, city, state, mapUrl, ocean;
    private double velocity, altitude;
    private final String username;

    /**
     * Initialisiert den ISS-Fetcher mit dem konfigurierten GeoNames-Benutzernamen.
     */
    public JSONFetcherIss() {
        this.username = Config.get("username", "");
    }

    /**
     * Lädt alle ISS-Daten in der benötigten Reihenfolge.
     *
     * @return {@code true}, wenn mindestens die ISS-Koordinaten erfolgreich geladen wurden
     */
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

    /**
     * Lädt die aktuelle ISS-Position.
     */
    public void fetchLocation() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://api.open-notify.org/iss-now.json"))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = Http.client().send(request, HttpResponse.BodyHandlers.ofString());
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

    /**
     * Lädt Geschwindigkeit und Höhe der ISS.
     */
    public void fetchSpeedHeight() {
        if (latitude == null || longitude == null) return;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.wheretheiss.at/v1/satellites/" + NORAD_ID))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = Http.client().send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

            this.velocity = json.get("velocity").getAsDouble();
            this.altitude = json.get("altitude").getAsDouble();

        } catch (Exception e) {
            log.error("Fehler beim Abrufen der ISS-Geschwindigkeit/Höhe", e);
            this.velocity = 0;
            this.altitude = 0;
        }
    }

    /**
     * Lädt Zeitzone und Kartenlink für die aktuelle ISS-Position.
     */
    public void fetchMapUrlTimeZone() {
        if (latitude == null || longitude == null) return;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.wheretheiss.at/v1/coordinates/" + this.latitude + "," + this.longitude))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = Http.client().send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

            this.timezone_id = optString(json, "timezone_id", DEFAULT_VALUE);
            this.mapUrl = optString(json, "map_url", "");

        } catch (Exception e) {
            log.error("Fehler beim Abrufen der Zeitzone/Karte", e);
            this.timezone_id = DEFAULT_VALUE;
            this.mapUrl = "";
        }
    }

    /**
     * Ermittelt den Ozean unter der ISS, falls verfügbar.
     */
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

            HttpResponse<String> response = Http.client().send(request, HttpResponse.BodyHandlers.ofString());
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

    /**
     * Ermittelt Land, Bundesland und Stadt für die aktuelle ISS-Position.
     */
    public void fetchCountry() {
        if (latitude == null || longitude == null) return;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://nominatim.openstreetmap.org/reverse?lat=" + this.latitude + "&lon=" + this.longitude + "&format=json"))
                    .header("User-Agent", "SpaceLinker-Discord-Bot/1.0")
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = Http.client().send(request, HttpResponse.BodyHandlers.ofString());
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

    /**
     * Gibt den zuletzt geladenen Längengrad der ISS zurück.
     *
     * @return Längengrad
     */
    public String getLongitude() { return longitude; }

    /**
     * Gibt den zuletzt geladenen Breitengrad der ISS zurück.
     *
     * @return Breitengrad
     */
    public String getLatitude() { return latitude; }

    /**
     * Gibt die zuletzt geladene Zeitzone der ISS-Position zurück.
     *
     * @return Zeitzonen-ID
     */
    public String getTimezone_id() { return timezone_id; }

    /**
     * Gibt das zuletzt geladene Land unter der ISS zurück.
     *
     * @return Ländername oder Fallback
     */
    public String getCountry() { return country; }

    /**
     * Gibt den Kartenlink zur zuletzt geladenen ISS-Position zurück.
     *
     * @return Karten-URL
     */
    public String getMapUrl() { return mapUrl; }

    /**
     * Gibt die zuletzt geladene ISS-Geschwindigkeit zurück.
     *
     * @return Geschwindigkeit in km/h
     */
    public double getVelocity() { return velocity; }

    /**
     * Gibt die zuletzt geladene ISS-Höhe zurück.
     *
     * @return Höhe in km
     */
    public double getAltitude() { return altitude; }

    /**
     * Gibt den zuletzt geladenen Ozean unter der ISS zurück.
     *
     * @return Ozeanname oder Fallback
     */
    public String getOcean() { return ocean; }

    /**
     * Gibt das zuletzt geladene Bundesland oder den Staat unter der ISS zurück.
     *
     * @return Staat oder Fallback
     */
    public String getState() { return state; }

    /**
     * Gibt die zuletzt geladene Stadt unter der ISS zurück.
     *
     * @return Stadtname oder Fallback
     */
    public String getCity() { return city; }
}
