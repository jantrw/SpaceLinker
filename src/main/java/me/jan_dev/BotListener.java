package me.jan_dev;

import commands.NasaCommandHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verarbeitet empfangene Nachrichten (Prefix-Befehle) und den /help Slash-Command.
 */
public class BotListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(BotListener.class);

    private final NasaCommandHandler nasaHandler = new NasaCommandHandler();

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
                event.getChannel().sendMessageEmbeds(nasaHandler.buildPictureEmbed().build()).queue();
                break;

            case "nasapicinfo":
                event.getChannel().sendMessageEmbeds(nasaHandler.buildPictureInfoEmbed().build()).queue();
                break;

            case "setprefix":
                handleSetPrefix(event, guildId);
                break;

            case "help":
                sendHelpMessage(event, prefix);
                break;

            default:
                event.getChannel().sendMessage(
                        "Unbekannter Befehl. Gib `" + prefix + "help` ein, um eine Liste verfügbarer Befehle zu sehen."
                ).queue();
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
        log.info("Präfix für Guild {} auf '{}' geändert", guildId, newPrefix);
    }

    private void sendHelpMessage(MessageReceivedEvent event, String prefix) {
        event.getChannel().sendMessage(
                "**Verfügbare Befehle:**\n\n" +
                        "**Slash-Commands (/):**\n" +
                        "`/picture` - Zeigt das NASA-Bild des Tages\n" +
                        "`/pictureinfo` - Zeigt Infos zum NASA-Bild\n" +
                        "`/iss` - Zeigt die aktuelle ISS-Position\n" +
                        "`/help` - Zeigt diese Hilfe-Nachricht\n\n" +
                        "**Text-Befehle (" + prefix + "):**\n" +
                        "`" + prefix + "nasapic` - Zeigt das NASA-Bild des Tages\n" +
                        "`" + prefix + "nasapicinfo` - Zeigt Infos zum NASA-Bild\n" +
                        "`" + prefix + "setprefix [neues Präfix]` - Ändert das Präfix\n" +
                        "`" + prefix + "help` - Zeigt diese Hilfe-Nachricht an"
        ).queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("help")) return;

        event.reply(
                "**Verfügbare Befehle:**\n\n" +
                        "**Slash-Commands (/):**\n" +
                        "`/picture` - Zeigt das NASA-Bild des Tages\n" +
                        "`/pictureinfo` - Zeigt Infos zum NASA-Bild\n" +
                        "`/iss` - Zeigt die aktuelle ISS-Position\n" +
                        "`/help` - Zeigt diese Hilfe-Nachricht\n\n" +
                        "**Text-Befehle (!):**\n" +
                        "`!nasapic` - Zeigt das NASA-Bild des Tages\n" +
                        "`!nasapicinfo` - Zeigt Infos zum NASA-Bild\n" +
                        "`!setprefix [neues Präfix]` - Ändert das Präfix\n" +
                        "`!help` - Zeigt diese Hilfe-Nachricht an"
        ).setEphemeral(true).queue();
    }
}
