package me.jan_dev;

import commands.ISSData;
import commands.NasaPictureOfTheDay;
import data.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Hauptklasse für den Discord-Bot.
 * Initialisiert und startet den Bot mit den notwendigen Listenern und Slash-Commands.
 */
public class DiscordBot {

    private static final Logger log = LoggerFactory.getLogger(DiscordBot.class);

    private static final List<CommandData> EXPECTED_COMMANDS = List.of(
            Commands.slash("picture", "Zeigt das aktuelle 'Picture of the Day' von der NASA"),
            Commands.slash("pictureinfo", "Gibt Infos über das 'Picture of the Day' von der NASA"),
            Commands.slash("iss", "Gibt die aktuelle Position der ISS aus"),
            Commands.slash("help", "Zeigt eine Hilfeliste an")
    );

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

        registerCommandsIfNeeded(bot);

        log.info("Bot ist bereit und läuft auf {} Server(n)!", bot.getGuilds().size());
    }

    private static void registerCommandsIfNeeded(JDA bot) {
        Set<String> expectedNames = EXPECTED_COMMANDS.stream()
                .map(CommandData::getName)
                .collect(Collectors.toSet());

        Set<String> existingNames = bot.retrieveCommands().complete().stream()
                .map(Command::getName)
                .collect(Collectors.toSet());

        if (existingNames.equals(expectedNames)) {
            log.info("Slash-Commands bereits registriert, überspringe Update.");
            return;
        }

        log.info("Slash-Commands werden aktualisiert...");
        bot.updateCommands().addCommands(EXPECTED_COMMANDS).queue(
                success -> log.info("Slash-Commands erfolgreich registriert."),
                failure -> log.error("Fehler beim Registrieren der Slash-Commands", failure)
        );
    }
}
