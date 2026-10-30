package lab3;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Config {
    private static final Properties FILE = load();

    private Config() {}

    public static String graphHopperKey() { return require("GRAPHHOPPER_KEY"); }
    public static String openWeatherKey() { return require("OPENWEATHER_KEY"); }

    private static String require(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) value = FILE.getProperty(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Не задан " + name + ": укажите переменную окружения или config.properties");
        }
        return value.trim();
    }

    private static Properties load() {
        Properties p = new Properties();
        Path path = Path.of("config.properties");
        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                p.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Не удалось прочитать config.properties", e);
            }
        }
        return p;
    }
}