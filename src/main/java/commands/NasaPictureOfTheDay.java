package commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verarbeitet die Slash-Commands /picture und /pictureinfo.
 * Delegiert an den zentralen NasaCommandHandler.
 */
public class NasaPictureOfTheDay extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(NasaPictureOfTheDay.class);

    private final NasaCommandHandler handler = new NasaCommandHandler();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String name = event.getName();
        if (!name.equals("picture") && !name.equals("pictureinfo")) return;

        event.deferReply().queue(hook -> {
            try {
                switch (name) {
                    case "picture":
                        hook.sendMessageEmbeds(handler.buildPictureEmbed().build()).queue();
                        break;
                    case "pictureinfo":
                        hook.sendMessageEmbeds(handler.buildPictureInfoEmbed().build()).queue();
                        break;
                }
            } catch (Exception e) {
                hook.sendMessage("❌ Fehler beim Abrufen der NASA-Daten. Bitte später erneut versuchen.").queue();
                log.error("Fehler bei /{} command", name, e);
            }
        });
    }
}
