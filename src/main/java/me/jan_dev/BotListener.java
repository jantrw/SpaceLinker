package me.jan_dev;

import commands.NasaPictureOfTheDay;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Diese Klasse verarbeitet empfangene Nachrichten und führt die entsprechenden Befehle aus.
 * Sie überprüft das Präfix, extrahiert den Befehl und ruft die entsprechende Funktion auf.
 */
public class BotListener extends ListenerAdapter {

    // Instanz der Klasse für das Abrufen des NASA-Bildes des Tages
    private final NasaPictureOfTheDay nasaPictureOfTheDay;

    /**
     * Konstruktor, der die Instanz der NasaPictureOfTheDay-Klasse initialisiert.
     */
    public BotListener() {
        this.nasaPictureOfTheDay = new NasaPictureOfTheDay();
    }

    /**
     * Diese Methode wird aufgerufen, wenn eine neue Nachricht in einem Discord-Textkanal empfangen wird.
     * @param event Das empfangene MessageReceivedEvent, das die Nachrichtendaten enthält.
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignoriere Nachrichten von anderen Bots, um Endlosschleifen zu vermeiden.
        if (event.getAuthor().isBot()) return;

        // Nachrichtentext bereinigen (Leerzeichen entfernen)
        String message = event.getMessage().getContentRaw().trim();
        if (message.isEmpty()) return; // Ignoriere leere Nachrichten

        // Guild-ID abrufen und das entsprechende Präfix ermitteln
        String guildId = event.getGuild().getId();
        String prefix = data.GuildDataManager.getPrefix(guildId);

        // Überprüfen, ob die Nachricht mit dem gesetzten Präfix beginnt
        if (!message.startsWith(prefix)) return;

        // Befehl aus der Nachricht extrahieren
        String command = message.substring(prefix.length()).trim().toLowerCase();
        handleCommand(event, command, guildId, prefix);
    }

    /**
     * Verarbeitet den extrahierten Befehl und ruft die entsprechende Methode auf.
     * @param event Das MessageReceivedEvent-Objekt.
     * @param command Der erkannte Befehl als String.
     * @param guildId Die ID der Discord-Gilde (Server).
     * @param prefix Das aktuelle Befehlspräfix.
     */
    private void handleCommand(MessageReceivedEvent event, String command, String guildId, String prefix) {
        switch (command) {
            case "nasapic":
                // Ruft die Methode auf, um das NASA-Bild des Tages zu senden
                nasaPictureOfTheDay.handleCommandPicture(event);
                break;

            case "nasapicinfo":
                // Ruft die Methode auf, um das NASA-Bild mit Informationen zu senden
                nasaPictureOfTheDay.handleCommand(event);
                break;

            case "setprefix":
                // Ändert das Befehlspräfix für die jeweilige Gilde
                handleSetPrefix(event, guildId);
                break;

            case "help":
                // Sendet eine Hilfe-Nachricht mit allen verfügbaren Befehlen
                sendHelpMessage(event, prefix);
                break;

            default:
                // Antwortet mit einer Fehlermeldung bei unbekannten Befehlen
                event.getChannel().sendMessage("Unbekannter Befehl. Gib `" + prefix + "help` ein, um eine Liste verfügbarer Befehle zu sehen.").queue();
                break;
        }
    }

    /**
     * Ändert das Befehlspräfix für den Server.
     * @param event Das empfangene MessageReceivedEvent.
     * @param guildId Die ID der Gilde, deren Präfix geändert werden soll.
     */
    private void handleSetPrefix(MessageReceivedEvent event, String guildId) {
        // Nachricht in Wörter aufsplitten, um das neue Präfix zu extrahieren
        String[] parts = event.getMessage().getContentRaw().split(" ");
        if (parts.length < 2) {
            // Falls kein neues Präfix angegeben wurde, eine Fehlermeldung senden
            event.getChannel().sendMessage("Bitte gib ein neues Präfix an! Beispiel: `setprefix !`").queue();
            return;
        }

        // Neues Präfix setzen
        String newPrefix = parts[1];
        data.GuildDataManager.setPrefix(guildId, newPrefix);
        event.getChannel().sendMessage("Das Präfix wurde auf `" + newPrefix + "` geändert!").queue();
    }

    /**
     * Sendet eine Liste der verfügbaren Befehle an den Nutzer.
     * @param event Das MessageReceivedEvent-Objekt.
     * @param prefix Das aktuelle Befehlspräfix.
     */
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
