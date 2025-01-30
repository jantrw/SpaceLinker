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

public class DiscordBot extends ListenerAdapter {

    public static void main(String[] args) throws InterruptedException, IOException {

        Properties prop = new Properties();
        String path = "/home/jantrw/Development/bot_full-main/java-discord-bot/Discord-Bot/src/main/resources/config.properties";
        prop.load(new FileInputStream(path));
        // Bot-Token einfügen
        String token = prop.getProperty("botToken");

        // JDA-Instanz erstellen
        JDA bot = JDABuilder.createDefault(token,
                        GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing("mit der ISS")) // Status setzen
                .addEventListeners(new BotListener()) // Listener registrieren// NasaPicture-Kommando
                .addEventListeners(new NasaPictureOfTheDay()) // NasaPictureOfTheDay-Kommando
                .addEventListeners(new ISSData()) // ISSData-Kommando
                .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS) // Cache optimieren
                .build().awaitReady();

        // Globale Slash-Commands registrieren
        bot.updateCommands().addCommands(
                Commands.slash("picture", "Zeigt das aktuelle 'Picture of the Day' von der NASA"),
                Commands.slash("pictureinfo", "Gibt Infos über das 'Picture of the Day' von der NASA"),
                Commands.slash("iss", "Gibt die aktuelle Position der ISS aus"),
                Commands.slash("help", "Zeigt eine Hilfeliste an")
        ).queue();

        System.out.println("Bot ist bereit und läuft auf mehreren Servern!");
    }
}
