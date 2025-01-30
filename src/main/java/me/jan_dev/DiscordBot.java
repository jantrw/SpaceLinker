package me.jan_dev;

import commands.ISSData;
import commands.NasaPictureOfTheDay;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Hauptklasse für den Discord-Bot.
 * Diese Klasse initialisiert und startet den Bot mit den notwendigen Listenern und Slash-Commands.
 */
public class DiscordBot extends ListenerAdapter {

    /**
     * Einstiegspunkt des Programms.
     * Initialisiert den Bot, lädt die Konfiguration und registriert die Befehle.
     *
     * @param args Kommandozeilenargumente  (werden nicht genutzt)
     * @throws InterruptedException Falls die Bot-Initialisierung unterbrochen wird
     * @throws IOException Falls ein Fehler beim Laden der Konfigurationsdatei auftritt
     */
    public static void main(String[] args) throws InterruptedException, IOException {

        // Eigenschaften-Objekt für die Konfigurationsdatei
        Properties prop = new Properties();

        // Pfad zur Konfigurationsdatei
        String path = "A:\\Development\\StarLinker\\src\\main\\resources\\config.properties";

        // Konfigurationsdatei laden
        prop.load(new FileInputStream(path));

        // Bot-Token aus der Konfigurationsdatei abrufen
        String token = prop.getProperty("botToken");

        // Erstellen und Starten der JDA-Instanz mit den benötigten Berechtigungen
        JDA bot = JDABuilder.createDefault(token,
                        GatewayIntent.GUILD_MESSAGES, // Erlaubt das Empfangen von Nachrichten in Servern
                        GatewayIntent.MESSAGE_CONTENT) // Erlaubt das Lesen des Nachrichteninhalts
                .setActivity(Activity.playing("mit der ISS")) // Status des Bots setzen
                .addEventListeners(new BotListener()) // Registrieren des Bot-Listeners für Befehlsverarbeitung
                .addEventListeners(new NasaPictureOfTheDay()) // Registrieren des NASA-Befehls
                .addEventListeners(new ISSData()) // Registrieren des ISS-Daten-Befehls
                .disableCache(
                        CacheFlag.VOICE_STATE, // Deaktiviert den Voice-Status-Cache (nicht benötigt)
                        CacheFlag.EMOJI, // Deaktiviert den Emoji-Cache
                        CacheFlag.STICKER, // Deaktiviert den Sticker-Cache
                        CacheFlag.SCHEDULED_EVENTS // Deaktiviert geplante Events (nicht benötigt)
                )
                .build()
                .awaitReady(); // Warten, bis der Bot vollständig verbunden ist

        // Globale Slash-Commands registrieren
        bot.updateCommands().addCommands(
                Commands.slash("picture", "Zeigt das aktuelle 'Picture of the Day' von der NASA"),
                Commands.slash("pictureinfo", "Gibt Infos über das 'Picture of the Day' von der NASA"),
                Commands.slash("iss", "Gibt die aktuelle Position der ISS aus"),
                Commands.slash("help", "Zeigt eine Hilfeliste an")
        ).queue();

        // Erfolgreiche Initialisierung im Konsolen-Output anzeigen
        System.out.println("Bot ist bereit und läuft auf mehreren Servern!");
    }
}
