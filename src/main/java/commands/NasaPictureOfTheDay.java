package commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Verarbeitet die Slash-Commands /picture und /pictureinfo.
 * Delegiert an den zentralen NasaCommandHandler.
 */
public class NasaPictureOfTheDay extends ListenerAdapter {

    private final NasaCommandHandler handler = new NasaCommandHandler();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String name = event.getName();
        if (!name.equals("picture") && !name.equals("pictureinfo")) return;

        event.deferReply().queue();

        switch (name) {
            case "picture":
                event.getHook().sendMessageEmbeds(handler.buildPictureEmbed().build()).queue();
                break;
            case "pictureinfo":
                event.getHook().sendMessageEmbeds(handler.buildPictureInfoEmbed().build()).queue();
                break;
        }
    }
}
