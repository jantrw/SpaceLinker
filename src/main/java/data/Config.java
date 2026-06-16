package data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Zentrale Konfigurationsklasse.
 * Lädt Konfiguration aus Umgebungsvariablen oder einer lokalen Properties-Datei außerhalb des Classpath.
 */
public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);
    private static final Properties properties = new Properties();

    static {
        properties.putAll(loadFileProperties());
    }

    /**
     * Lädt eine lokale {@code config.properties} aus den unterstützten Kandidatenpfaden.
     *
     * @return geladene Properties, ggf. leer
     */
    static Properties loadFileProperties() {
        Properties loaded = new Properties();

        for (Path candidate : configCandidates()) {
            if (!Files.isRegularFile(candidate)) {
                continue;
            }

            try (InputStream input = Files.newInputStream(candidate)) {
                loaded.load(input);
                log.info("Konfiguration geladen aus {}", candidate.toAbsolutePath());
                return loaded;
            } catch (IOException e) {
                throw new IllegalStateException("Konfiguration konnte nicht gelesen werden: " + candidate, e);
            }
        }

        log.warn("Keine lokale config.properties gefunden. Verwende nur Umgebungsvariablen.");
        return loaded;
    }

    /**
     * Liefert die möglichen Pfade für die lokale Konfigurationsdatei.
     *
     * @return Liste unterstützter Dateipfade
     */
    private static List<Path> configCandidates() {
        String configuredPath = System.getProperty("spacelinker.config.path", "").trim();
        if (!configuredPath.isEmpty()) {
            return List.of(Path.of(configuredPath));
        }

        return List.of(Path.of("config.properties"));
    }

    /**
     * Gibt einen Konfigurationswert zurück.
     *
     * @param key Der Schlüssel in der Konfigurationsdatei.
     * @return Der Wert oder null, wenn nicht gefunden.
     */
    public static String get(String key) {
        return resolveValue(properties, System.getenv(), key).orElse(null);
    }

    /**
     * Gibt einen Konfigurationswert mit Fallback zurück.
     *
     * @param key          Der Schlüssel in der Konfigurationsdatei.
     * @param defaultValue Der Standardwert, falls der Schlüssel nicht existiert.
     * @return Der Wert oder der Standardwert.
     */
    public static String get(String key, String defaultValue) {
        return resolveValue(properties, System.getenv(), key).orElse(defaultValue);
    }

    /**
     * Prüft, ob ein Konfigurationsschlüssel vorhanden ist.
     *
     * @param key Der zu prüfende Schlüssel.
     * @return true, wenn der Schlüssel existiert.
     */
    public static boolean has(String key) {
        return resolveValue(properties, System.getenv(), key).isPresent();
    }

    /**
     * Liest einen Wert bevorzugt aus Umgebungsvariablen und sonst aus Properties.
     *
     * @param properties geladene Properties
     * @param environment aktuelle Umgebungsvariablen
     * @param key Konfigurationsschlüssel
     * @return aufgelöster Wert, falls vorhanden
     */
    static Optional<String> resolveValue(Properties properties, Map<String, String> environment, String key) {
        String envKey = toEnvironmentKey(key);
        Optional<String> envValue = normalize(environment.get(envKey));
        if (envValue.isPresent()) {
            return envValue;
        }

        return normalize(properties.getProperty(key));
    }

    /**
     * Mappt einen internen Konfigurationsschlüssel auf die zugehörige Umgebungsvariable.
     *
     * @param key Konfigurationsschlüssel
     * @return Name der Umgebungsvariable
     */
    private static String toEnvironmentKey(String key) {
        return switch (key) {
            case "botToken" -> "DISCORD_TOKEN";
            case "apiKeyNasa" -> "NASA_API_KEY";
            case "username" -> "GEONAMES_USERNAME";
            default -> key.replace('.', '_').replace('-', '_').toUpperCase();
        };
    }

    /**
     * Entfernt Leerzeichen und behandelt leere Werte als nicht vorhanden.
     *
     * @param value Eingabewert
     * @return normalisierter Wert oder leer
     */
    private static Optional<String> normalize(String value) {
        if (value == null) {
            return Optional.empty();
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? Optional.empty() : Optional.of(trimmed);
    }
}
