package data;

import java.util.HashMap;

/**
 * Diese Klasse verwaltet Guild-spezifische Daten, insbesondere benutzerdefinierte Präfixe.
 *
 * Aktuell werden die Präfixe in einer HashMap gespeichert, was bedeutet, dass die Daten
 * nur zur Laufzeit verfügbar sind und nach einem Neustart des Bots verloren gehen.
 *
 * Hinweis: Eine persistente Speicherung (z. B. durch eine Datenbank wie SQLite, MySQL)
 * sollte in Zukunft implementiert werden, um Präfixe und andere Guild-spezifische Einstellungen
 * auch nach einem Neustart des Bots beizubehalten.
 */
public class GuildDataManager {
    // HashMap, die die Präfixe für jede Guild speichert.
    // Der Schlüssel ist die Guild-ID, und der Wert ist das dazugehörige Präfix.
    private static final HashMap<String, String> guildPrefixes = new HashMap<>();

    // Standardpräfix für alle Guilds
    static {
        // Füge einen Eintrag für das Standardpräfix hinzu, der verwendet wird,
        // wenn für eine Guild kein spezifisches Präfix gesetzt wurde.
        guildPrefixes.put("default", "!");
    }

    /**
     * Setzt ein Präfix für eine spezifische Guild.
     *
     * @param guildId Die ID der Discord-Guild, für die das Präfix gesetzt wird.
     * @param prefix  Das neue Präfix, das in dieser Guild verwendet werden soll.
     */
    public static void setPrefix(String guildId, String prefix) {
        // Fügt das Präfix für die Guild-ID in die HashMap ein oder aktualisiert es, falls es bereits existiert.
        guildPrefixes.put(guildId, prefix);
    }

    /**
     * Gibt das Präfix für eine spezifische Guild zurück.
     *
     * @param guildId Die ID der Discord-Guild, deren Präfix abgerufen werden soll.
     * @return Das Präfix der Guild, oder das Standardpräfix, wenn keins gesetzt ist.
     */
    public static String getPrefix(String guildId) {
        // Prüft, ob ein Präfix für die Guild-ID existiert. Wenn nicht, wird das Standardpräfix zurückgegeben.
        return guildPrefixes.getOrDefault(guildId, guildPrefixes.get("default"));
    }

    /*
     * Zukünftige Verbesserungen:
     *
     * 1. **Persistente Speicherung:**
     *    Aktuell gehen die Präfixe nach einem Neustart des Bots verloren, da sie nur
     *    in einer HashMap gespeichert werden. Um dieses Problem zu lösen, könnte eine
     *    Datenbank (z. B. SQLite oder MySQL) verwendet werden, um die Präfixe dauerhaft
     *    zu speichern.
     *
     * 2. **Dynamisches Standardpräfix:**
     *    Eine Möglichkeit, das Standardpräfix dynamisch für alle Guilds zu ändern,
     *    könnte in Betracht gezogen werden.
     *
     * 3. **Weitere Guild-spezifische Einstellungen:**
     *    Neben Präfixen könnten weitere Einstellungen wie Sprache, aktive Module
     *    oder Befehlsberechtigungen hinzugefügt werden.
     */
}
