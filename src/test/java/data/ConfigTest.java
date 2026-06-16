package data;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigTest {

    @Test
    void prefersEnvironmentValuesOverProperties() {
        Properties properties = new Properties();
        properties.setProperty("botToken", "file-token");

        String value = Config.resolveValue(properties, Map.of("DISCORD_TOKEN", "env-token"), "botToken").orElseThrow();

        assertEquals("env-token", value);
    }

    @Test
    void fallsBackToPropertiesWhenEnvironmentIsMissing() {
        Properties properties = new Properties();
        properties.setProperty("apiKeyNasa", "file-key");

        String value = Config.resolveValue(properties, Map.of(), "apiKeyNasa").orElseThrow();

        assertEquals("file-key", value);
    }

    @Test
    void treatsBlankValuesAsMissing() {
        Properties properties = new Properties();
        properties.setProperty("username", " ");

        assertTrue(Config.resolveValue(properties, Map.of("GEONAMES_USERNAME", "astro-user"), "username").isPresent());
        assertFalse(Config.resolveValue(properties, Map.of(), "username").isPresent());
    }
}
