package me.jan_dev;

import commands.NasaPictureOfTheDay;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Diese Klasse verarbeitet empfangene Nachrichten und führt die entsprechenden Befehle aus.
 */
public class BotListener extends ListenerAdapter {

    private final NasaPictureOfTheDay nasaPictureOfTheDay;

    public BotListener() {
        this.nasaPictureOfTheDay = new NasaPictureOfTheDay();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String message = event.getMessage().getContentRaw().trim();
        if (message.isEmpty()) return;

        String guildId = event.getGuild().getId();
        String prefix = data.GuildDataManager.getPrefix(guildId);

        if (!message.startsWith(prefix)) return;

        String command = message.substring(prefix.length()).trim().toLowerCase();
        handleCommand(event, command, guildId, prefix);
    }

    private void handleCommand(MessageReceivedEvent event, String command, String guildId, String prefix) {
        switch (command) {
            case "nasapic":
                nasaPictureOfTheDay.handleCommandPicture(event); // ✅ Jetzt korrekt
                break;

            case "nasapicinfo":
                nasaPictureOfTheDay.handleCommand(event); // ✅ Jetzt korrekt
                break;

            case "setprefix":
                handleSetPrefix(event, guildId);
                break;

            case "help":
                sendHelpMessage(event, prefix);
                break;

            default:
                event.getChannel().sendMessage("Unbekannter Befehl. Gib `" + prefix + "help` ein, um eine Liste verfügbarer Befehle zu sehen.").queue();
                break;
        }
    }

    private void handleSetPrefix(MessageReceivedEvent event, String guildId) {
        String[] parts = event.getMessage().getContentRaw().split(" ");
        if (parts.length < 2) {
            event.getChannel().sendMessage("Bitte gib ein neues Präfix an! Beispiel: `setprefix !`").queue();
            return;
        }

        String newPrefix = parts[1];
        data.GuildDataManager.setPrefix(guildId, newPrefix);
        event.getChannel().sendMessage("Das Präfix wurde auf `" + newPrefix + "` geändert!").queue();
    }

    private void sendHelpMessage(MessageReceivedEvent event, String prefix) {
        event.getChannel().sendMessage(
                "**Verfügbare Befehle:**\n" +
                        "`" + prefix + "nasapic` - Zeigt das NASA-Bild des Tages\n" +
                        "`" + prefix + "nasapicinfo` - Zeigt Infos zum NASA-Bild\n" +
                        "`" + prefix + "setprefix [neues Präfix]` - Ändert das Präfix\n" +
                        "`" + prefix + "help` - Zeigt diese Hilfe-Nachricht an"
        ).queue();
    }
}
