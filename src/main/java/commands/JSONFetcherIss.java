package commands;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

/**
 * Die Klasse {@code JSONFetcherIss} ruft aktuelle Daten zur Internationalen Raumstation (ISS) von verschiedenen APIs ab.
 * Sie liefert Informationen zur Position, Geschwindigkeit, Höhe, dem aktuellen Land oder Ozean sowie der zugehörigen Zeitzone.
 * <p>
 * Die Daten müssen über die Methode {@link #fetchAllData()} abgerufen werden, bevor sie durch Getter-Methoden verfügbar sind.
 * </p>
 */
public class JSONFetcherIss {

    // ISS-Datenvariablen
    private String longitude, latitude, timezone_id, country, city, state, mapUrl, ocean;
    private double velocity, altitude;
    private String username; // API-Nutzername für die GeoNames-API

    /**
     * Konstruktor für {@code JSONFetcherIss}.
     * <p>
     * Lädt den API-Nutzernamen aus der Konfigurationsdatei, ruft aber noch keine Daten ab.
     * Die Daten müssen explizit durch einen Aufruf von {@link #fetchAllData()} geladen werden.
     * </p>
     */
    public JSONFetcherIss() {
        setUsername();
    }

    /**
     * Ruft alle relevanten ISS-Daten von externen APIs ab.
     * <p>
     * Diese Methode führt alle API-Abfragen nacheinander aus, um die aktuellen ISS-Daten abzurufen.
     * </p>
     */
    public void fetchAllData() {
        fetchLocation();
        fetchSpeedHeight();
        fetchMapUrlTimeZone();
        fetchCountry();
        fetchOcean();
    }

    /**
     * Holt die aktuelle geografische Position (Breiten- und Längengrad) der ISS.
     * Die Daten werden von der API "Open Notify" abgerufen.
     */
    public void fetchLocation() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://api.open-notify.org/iss-now.json"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());

            // Koordinaten extrahieren
            JSONObject data = json.getJSONObject("iss_position");
            this.latitude = data.getString("latitude");
            this.longitude = data.getString("longitude");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Holt die aktuelle Geschwindigkeit (in km/h) und Höhe (in km) der ISS.
     * Die Daten werden von der API "Where the ISS at" abgerufen.
     */
    public void fetchSpeedHeight() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String noradCatalogID = "25544"; // NORAD-Katalog-ID der ISS

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.wheretheiss.at/v1/satellites/" + noradCatalogID))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());

            // Geschwindigkeit und Höhe extrahieren
            this.velocity = json.getDouble("velocity");
            this.altitude = json.getDouble("altitude");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Holt die aktuelle Zeitzone und eine Google-Maps-URL basierend auf der ISS-Position.
     */
    public void fetchMapUrlTimeZone() {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.wheretheiss.at/v1/coordinates/" + this.latitude + "," + this.longitude))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());

            // Zeitzone und Karten-URL extrahieren
            this.timezone_id = json.getString("timezone_id");
            this.mapUrl = json.getString("map_url");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Holt die Ozeanbezeichnung, falls sich die ISS über einem Gewässer befindet.
     * Die Daten werden von der GeoNames-API abgerufen.
     */
    public void fetchOcean() {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://api.geonames.org/extendedFindNearbyJSON?lat=" + this.latitude + "&lng=" + this.longitude + "&username=" + this.username))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());

            if (json.has("ocean")) {
                JSONObject oceanObj = json.getJSONObject("ocean");
                this.ocean = oceanObj.getString("name");
            } else {
                this.ocean = "Die ISS ist über einem Land";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Holt das Land, den Bundesstaat und die Stadt basierend auf den aktuellen ISS-Koordinaten.
     * Falls die API keine Daten liefert, werden Standardwerte ("??") gesetzt.
     */
    public void fetchCountry() {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://nominatim.openstreetmap.org/reverse?lat=" + this.latitude + "&lon=" + this.longitude + "&format=json"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());

            // Prüfen, ob ein Adress-Objekt vorhanden ist
            if (json.has("address")) {
                JSONObject address = json.getJSONObject("address");

                // Land, Bundesstaat und Stadt extrahieren (mit Fallbacks)
                this.country = address.optString("country", "??");
                this.state = address.optString("state", "??");
                this.city = address.optString("city", address.optString("town", address.optString("village", "??")));
            } else {
                this.country = "??";
                this.state = "??";
                this.city = "??";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Lädt den API-Nutzernamen für GeoNames aus einer Konfigurationsdatei.
     * Dieser Nutzername ist erforderlich, um die API nutzen zu können.
     */
    public void setUsername() {
        Properties prop = new Properties();
        String path = "/home/jantrw/Development/bot_full-main/java-discord-bot/Discord-Bot/src/main/resources/config.properties";
        try {
            prop.load(new FileInputStream(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.username = prop.getProperty("username");
    }

    // Getter-Methoden für die abgerufenen ISS-Daten

    /** @return Die aktuelle Länge der ISS. */
    public String getLongitude() {
        return longitude;
    }

    /** @return Die aktuelle Breite der ISS. */
    public String getLatitude() {
        return latitude;
    }

    /** @return Die Zeitzone der aktuellen ISS-Position. */
    public String getTimezone_id() {
        return timezone_id;
    }

    /** @return Das aktuelle Land unter der ISS oder "??" falls unbekannt. */
    public String getCountry() {
        return country;
    }

    /** @return Die URL zur Karte mit der ISS-Position. */
    public String getMapUrl() {
        return mapUrl;
    }

    /** @return Die Geschwindigkeit der ISS in km/h. */
    public double getVelocity() {
        return velocity;
    }

    /** @return Die Höhe der ISS in km. */
    public double getAltitude() {
        return altitude;
    }

    /** @return Der Ozean unter der ISS oder eine Meldung, falls sie über Land ist. */
    public String getOcean() {
        return ocean;
    }

    /** @return Der Bundesstaat unter der ISS oder "??" falls unbekannt. */
    public String getState() {
        return state;
    }

    /** @return Die Stadt unter der ISS oder "??" falls unbekannt. */
    public String getCity() {
        return city;
    }
}
