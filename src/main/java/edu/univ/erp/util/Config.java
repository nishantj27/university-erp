package edu.univ.erp.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads app.properties from the classpath once and hands out settings. Keeping this in one place
 * means the DB urls / credentials aren't scattered around the code.
 */
public final class Config {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (in == null) {
                throw new IllegalStateException(
                        "app.properties not found on the classpath. Copy app.properties.example to "
                                + "src/main/resources/app.properties and set your MySQL credentials.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("could not read app.properties", e);
        }
    }

    private Config() {
    }

    public static String get(String key) {
        String value = props.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("missing config key: " + key);
        }
        return value;
    }

    public static int getInt(String key, int fallback) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Integer.parseInt(value.trim());
    }
}
