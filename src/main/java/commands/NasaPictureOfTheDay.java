package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Diese Klasse verarbeitet Slash-Befehle für das NASA-Bild des Tages.
 * Sie lädt die Daten von der NASA API und sendet sie als Embed-Nachricht in Discord.
 */
public class NasaPictureOfTheDay extends ListenerAdapter {

    private final JSONFetcherNasa jsonFetcherNasa;

    public NasaPictureOfTheDay() {
        this.jsonFetcherNasa = new JSONFetcherNasa(loadApiKey());
    }

    private String loadApiKey() {
        Properties prop = new Properties();
        String configPath = "A:\\Development\\StarLinker\\src\\main\\resources\\config.properties";
        try (FileInputStream input = new FileInputStream(configPath)) {
            prop.load(input);
            return prop.getProperty("apiKeyNasa", "DEMO_KEY");
        } catch (IOException e) {
            System.err.println("Fehler beim Laden der NASA-API-Konfiguration: " + e.getMessage());
            return "DEMO_KEY";
        }
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

    // 🌟 Methode für Slash-Befehl (Bleibt unverändert, aber jetzt öffentlich)
    public void handleCommand(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(jsonFetcherNasa.getTitle())
                .setDescription(jsonFetcherNasa.getExplanation())
                .setImage(jsonFetcherNasa.getUrl());

        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

    // 🌟 Methode für Slash-Befehl (Bleibt unverändert, aber jetzt öffentlich)
    public void handleCommandPicture(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(jsonFetcherNasa.getTitle())
                .setImage(jsonFetcherNasa.getUrl());

        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

    // ✨ NEUE Methode für normale Textnachrichten
    public void handleCommand(MessageReceivedEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(jsonFetcherNasa.getTitle())
                .setDescription(jsonFetcherNasa.getExplanation())
                .setImage(jsonFetcherNasa.getUrl());

        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }

    // ✨ NEUE Methode für normale Textnachrichten
    public void handleCommandPicture(MessageReceivedEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(jsonFetcherNasa.getTitle())
                .setImage(jsonFetcherNasa.getUrl());

        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
}
