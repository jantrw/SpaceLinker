package me.jan_dev;

import commands.ISSData;
import commands.NasaPictureOfTheDay;
import data.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hauptklasse für den Discord-Bot.
 * Initialisiert und startet den Bot mit den notwendigen Listenern und Slash-Commands.
 */
public class DiscordBot {

    private static final Logger log = LoggerFactory.getLogger(DiscordBot.class);

    public static void main(String[] args) throws InterruptedException {

        String token = Config.get("botToken");

        if (token == null || token.isBlank()) {
            throw new RuntimeException("Bot-Token nicht in config.properties gefunden!");
        }

        JDA bot = JDABuilder.createDefault(token,
                        GatewayIntent.GUILD_MESSAGES)
                .setActivity(Activity.playing("mit der ISS"))
                .addEventListeners(new BotListener())
                .addEventListeners(new NasaPictureOfTheDay())
                .addEventListeners(new ISSData())
                .disableCache(
                        CacheFlag.VOICE_STATE,
                        CacheFlag.EMOJI,
                        CacheFlag.STICKER,
                        CacheFlag.SCHEDULED_EVENTS
                )
                .build()
                .awaitReady();

        // Slash-Commands nur bei Bedarf registrieren:
        // Entferne den Kommentar und starte den Bot einmal, um Commands zu registrieren.
        // Danach wieder auskommentieren, um Rate-Limits zu vermeiden.
        //
        // bot.updateCommands().addCommands(
        //         Commands.slash("picture", "Zeigt das aktuelle 'Picture of the Day' von der NASA"),
        //         Commands.slash("pictureinfo", "Gibt Infos über das 'Picture of the Day' von der NASA"),
        //         Commands.slash("iss", "Gibt die aktuelle Position der ISS aus"),
        //         Commands.slash("help", "Zeigt eine Hilfeliste an")
        // ).queue();

        log.info("Bot ist bereit und läuft auf {} Server(n)!", bot.getGuilds().size());
    }
}
