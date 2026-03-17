package data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Zentrale Konfigurationsklasse.
 * Lädt die Konfigurationsdatei einmalig beim Start und stellt die Werte über statische Methoden bereit.
 */
public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                log.warn("config.properties nicht im Classpath gefunden!");
            } else {
                properties.load(input);
            }
        } catch (IOException e) {
            log.error("Fehler beim Laden der Konfiguration", e);
        }
    }

    /**
     * Gibt einen Konfigurationswert zurück.
     *
     * @param key Der Schlüssel in der Konfigurationsdatei.
     * @return Der Wert oder null, wenn nicht gefunden.
     */
    public static String get(String key) {
        return properties.getProperty(key);
    }

    /**
     * Gibt einen Konfigurationswert mit Fallback zurück.
     *
     * @param key          Der Schlüssel in der Konfigurationsdatei.
     * @param defaultValue Der Standardwert, falls der Schlüssel nicht existiert.
     * @return Der Wert oder der Standardwert.
     */
    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Prüft, ob ein Konfigurationsschlüssel vorhanden ist.
     *
     * @param key Der zu prüfende Schlüssel.
     * @return true, wenn der Schlüssel existiert.
     */
    public static boolean has(String key) {
        return properties.containsKey(key);
    }
}
