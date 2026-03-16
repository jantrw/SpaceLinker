package commands;

import data.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Diese Klasse verarbeitet Slash-Befehle für das NASA-Bild des Tages.
 * Sie lädt die Daten von der NASA API und sendet sie als Embed-Nachricht in Discord.
 */
public class NasaPictureOfTheDay extends ListenerAdapter {

    // Discord Embed-Limits
    private static final int MAX_EMBED_TITLE = 256;
    private static final int MAX_EMBED_DESCRIPTION = 4096;

    private final String apiKey;

    public NasaPictureOfTheDay() {
        this.apiKey = Config.get("apiKeyNasa", "DEMO_KEY");
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        switch (event.getName()) {
            case "picture":
                handleCommandPicture(event);
                break;
            case "pictureinfo":
                handleCommand(event);
                break;
        }
    }

    // Slash-Command: Bild mit Infos
    public void handleCommand(SlashCommandInteractionEvent event) {
        // Daten erst jetzt abrufen (lazy)
        JSONFetcherNasa fetcher = new JSONFetcherNasa(apiKey);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(truncate(fetcher.getTitle(), MAX_EMBED_TITLE))
                .setDescription(truncate(fetcher.getExplanation(), MAX_EMBED_DESCRIPTION))
                .setImage(fetcher.getUrl());

        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

    // Slash-Command: nur Bild
    public void handleCommandPicture(SlashCommandInteractionEvent event) {
        JSONFetcherNasa fetcher = new JSONFetcherNasa(apiKey);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(truncate(fetcher.getTitle(), MAX_EMBED_TITLE))
                .setImage(fetcher.getUrl());

        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

    // Text-Befehl: Bild mit Infos
    public void handleCommand(MessageReceivedEvent event) {
        JSONFetcherNasa fetcher = new JSONFetcherNasa(apiKey);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(truncate(fetcher.getTitle(), MAX_EMBED_TITLE))
                .setDescription(truncate(fetcher.getExplanation(), MAX_EMBED_DESCRIPTION))
                .setImage(fetcher.getUrl());

        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }

    // Text-Befehl: nur Bild
    public void handleCommandPicture(MessageReceivedEvent event) {
        JSONFetcherNasa fetcher = new JSONFetcherNasa(apiKey);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(truncate(fetcher.getTitle(), MAX_EMBED_TITLE))
                .setImage(fetcher.getUrl());

        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }

    /**
     * Kürzt einen Text auf die angegebene maximale Länge.
     * Fügt "..." hinzu, wenn der Text abgeschnitten wird.
     */
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
