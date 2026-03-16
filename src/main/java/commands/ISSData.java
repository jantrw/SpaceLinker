package commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Die Klasse {@code ISSData} verarbeitet den Discord-Slash-Befehl "/iss" und liefert aktuelle Informationen
 * zur Position, Geschwindigkeit, Höhe und weiteren Details der Internationalen Raumstation (ISS).
 * <p>
 * Sie nutzt die Klasse {@link JSONFetcherIss}, um die benötigten Daten von APIs abzurufen.
 * Beim Empfang eines Befehls ruft sie frische Daten ab und sendet sie an den Benutzer.
 * </p>
 */
public class ISSData extends ListenerAdapter {

    private final JSONFetcherIss jsonFetcher;

    /**
     * Konstruktor für {@code ISSData}.
     * Initialisiert ein neues {@link JSONFetcherIss}-Objekt, das für das Abrufen der ISS-Daten verantwortlich ist.
     */
    public ISSData() {
        this.jsonFetcher = new JSONFetcherIss();
    }

    /**
     * Event-Handler für den Slash-Befehl "/iss".
     * <p>
     * Diese Methode wird automatisch aufgerufen, wenn ein Nutzer den Befehl "/iss" in Discord ausführt.
     * Sie ruft aktuelle ISS-Daten von verschiedenen APIs ab, formatiert die Informationen und sendet
     * sie als  Antwort an den Nutzer.
     * </p>
     *
     * @param event Das {@link SlashCommandInteractionEvent}, das die Slash-Command-Interaktion beschreibt.
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("iss")) return;

        // Deferred Reply, da die API-Aufrufe Zeit benötigen
        event.deferReply().queue(hook -> {
            try {
                jsonFetcher.fetchAllData();

                // Daten von der API abrufen
                String latitude = jsonFetcher.getLatitude();
                String longitude = jsonFetcher.getLongitude();
                double velocity = jsonFetcher.getVelocity();
                double altitude = jsonFetcher.getAltitude();
                String country = jsonFetcher.getCountry();
                String timezone = jsonFetcher.getTimezone_id();
                String mapUrl = jsonFetcher.getMapUrl();
                String ocean = jsonFetcher.getOcean();
                String city = jsonFetcher.getCity();
                String state = jsonFetcher.getState();

                // Prüfen, ob die ISS über einem Land oder einem Ozean ist
                String locationText = (country.equals("??")) ? ocean : country;

                // Formatierte Stadt- und Bundesstaat-Daten generieren
                String cityStateText = getCityStateText(state, city);

                // Antwort zusammenbauen
                String response = buildResponse(latitude, longitude, velocity, altitude, locationText, cityStateText, timezone, mapUrl);

                hook.sendMessage(response).queue();

            } catch (Exception e) {
                hook.sendMessage("Fehler beim Abrufen der ISS-Daten.").queue();
                e.printStackTrace();
            }
        });
    }

    /**
     * Erstellt einen formatierten Text für Stadt und Staat basierend auf den abgerufenen Daten.
     * <p>
     * Falls sowohl Stadt als auch Staat verfügbar sind, werden beide mit passenden Emojis ausgegeben.
     * Falls nur der Staat verfügbar ist, wird nur dieser angezeigt.
     * Falls beide nicht verfügbar sind (markiert mit "??"), wird ein leerer String zurückgegeben.
     * </p>
     *
     * @param state Der Bundesstaat bzw. das Bundesland, über dem sich die ISS befindet (oder "??", falls nicht verfügbar).
     * @param city  Die Stadt, über der sich die ISS befindet (oder "??", falls nicht verfügbar).
     * @return Ein formatierter String mit Staat- und Stadtinformationen oder ein leerer String, falls keine Daten vorliegen.
     */
    private String getCityStateText(String state, String city) {
        if (!state.equals("??") && !city.equals("??")) {
            return "**🏛 Staat:** " + state + "\n**🏙 Stadt:** " + city + "\n";
        } else if (!state.equals("??")) {
            return "**🏛 Staat:** " + state + "\n"; // Nur Staat anzeigen, wenn Stadt nicht verfügbar ist
        }
        return ""; // Falls keine relevanten Daten vorhanden sind, wird ein leerer String zurückgegeben
    }

    /**
     * Erstellt eine formatierte Nachricht mit den aktuellen ISS-Daten.
     * <p>
     * Diese Methode generiert eine strukturierte Antwort, die die aktuelle Position, Geschwindigkeit,
     * Höhe, das Land oder den Ozean, die Zeitzone sowie einen Google-Maps-Link enthält.
     * </p>
     *
     * @param latitude      Der Breitengrad der ISS als String.
     * @param longitude     Der Längengrad der ISS als String.
     * @param velocity      Die Geschwindigkeit der ISS in Kilometern pro Stunde.
     * @param altitude      Die Höhe der ISS in Kilometern.
     * @param locationText  Der Name des Landes oder des Ozeans, über dem sich die ISS befindet.
     * @param cityStateText Formatierter Text mit Stadt- und Staatsinformationen (falls verfügbar).
     * @param timezone      Die aktuelle Zeitzone der ISS.
     * @param mapUrl        Eine URL zu einer Karte mit der aktuellen Position der ISS.
     * @return Eine formatierte Nachricht mit den aktuellen ISS-Daten.
     */
    private String buildResponse(String latitude, String longitude, double velocity, double altitude,
                                 String locationText, String cityStateText, String timezone, String mapUrl) {

        StringBuilder response = new StringBuilder();
        response.append("📡 **Aktuelle ISS-Position** 🌍\n")
                .append("**🛰 Breitengrad:** ").append(latitude).append("\n")
                .append("**🛰 Längengrad:** ").append(longitude).append("\n")
                .append("**🚀 Geschwindigkeit:** ").append(String.format("%.3f", velocity)).append(" km/h\n")
                .append("**📏 Höhe:** ").append(String.format("%.3f", altitude)).append(" km\n")
                .append("**📌 Land/Ozean:** ").append(locationText).append("\n")
                .append(cityStateText) // Falls Stadt/Staat existieren, werden sie hier hinzugefügt
                .append("**⏰ Zeitzone:** ").append(timezone).append("\n")
                .append("🌍 [Live-Karte ansehen](").append(mapUrl).append(")");

        return response.toString();
    }
}
