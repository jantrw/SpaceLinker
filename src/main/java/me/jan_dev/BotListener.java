package me.jan_dev;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Verarbeitet den /help Slash-Command.
 */
public class BotListener extends ListenerAdapter {

    /**
     * Beantwortet den Slash-Command {@code /help} mit einer kompakten Befehlsübersicht.
     *
     * @param event Slash-Command-Interaktion des Nutzers
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("help")) return;

        event.reply(
                "**Verfügbare Befehle:**\n\n" +
                        "`/picture` - Zeigt das NASA-Bild des Tages\n" +
                        "`/pictureinfo` - Zeigt Infos zum NASA-Bild\n" +
                        "`/iss` - Zeigt die aktuelle ISS-Position\n" +
                        "`/help` - Zeigt diese Hilfe-Nachricht"
        ).setEphemeral(true).queue();
    }
}
