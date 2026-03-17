package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Die Klasse {@code ISSData} verarbeitet den Discord-Slash-Befehl "/iss" und liefert aktuelle Informationen
 * zur Position, Geschwindigkeit, Höhe und weiteren Details der Internationalen Raumstation (ISS).
 */
public class ISSData extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ISSData.class);
    private static final int MAX_FIELD_VALUE = 1024;
    private final JSONFetcherIss jsonFetcher;

    public ISSData() {
        this.jsonFetcher = new JSONFetcherIss();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("iss")) return;

        event.deferReply().queue(hook -> {
            try {
                boolean success = jsonFetcher.fetchAllData();

                if (!success) {
                    hook.sendMessage("❌ ISS-Position konnte nicht abgerufen werden. Bitte später erneut versuchen.").queue();
                    return;
                }

                String country = jsonFetcher.getCountry();
                String ocean = jsonFetcher.getOcean();
                String locationText = (country == null || country.equals("??")) ? ocean : country;

                String state = jsonFetcher.getState();
                String city = jsonFetcher.getCity();
                String cityStateText = getCityStateText(state, city);

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("📡 Aktuelle ISS-Position 🌍")
                        .addField("🛰 Breitengrad", safeValue(jsonFetcher.getLatitude()), true)
                        .addField("🛰 Längengrad", safeValue(jsonFetcher.getLongitude()), true)
                        .addField("🚀 Geschwindigkeit", String.format("%.3f km/h", jsonFetcher.getVelocity()), true)
                        .addField("📏 Höhe", String.format("%.3f km", jsonFetcher.getAltitude()), true)
                        .addField("📌 Land/Ozean", safeValue(locationText), true)
                        .addField("⏰ Zeitzone", safeValue(jsonFetcher.getTimezone_id()), true);

                if (!cityStateText.isEmpty()) {
                    embed.addField("📍 Details", truncate(cityStateText, MAX_FIELD_VALUE), false);
                }

                String mapUrl = jsonFetcher.getMapUrl();
                if (mapUrl != null && !mapUrl.isEmpty()) {
                    embed.addField("🌍 Live-Karte", "[Ansehen](" + mapUrl + ")", false);
                }

                hook.sendMessageEmbeds(embed.build()).queue();

            } catch (Exception e) {
                hook.sendMessage("❌ Fehler beim Abrufen der ISS-Daten.").queue();
                log.error("Fehler beim Abrufen der ISS-Daten", e);
            }
        });
    }

    private String getCityStateText(String state, String city) {
        StringBuilder sb = new StringBuilder();
        if (state != null && !state.equals("??")) {
            sb.append("**🏛 Staat:** ").append(state);
        }
        if (city != null && !city.equals("??")) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("**🏙 Stadt:** ").append(city);
        }
        return sb.toString();
    }

    private String safeValue(String value) {
        return (value == null || value.isEmpty()) ? "??" : value;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
