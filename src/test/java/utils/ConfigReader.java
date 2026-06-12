package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private final Properties properties = new Properties();

    public ConfigReader() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (inputStream == null) {
                throw new IllegalStateException("config.properties file not found");
            }
            properties.load(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load config.properties", exception);
        }
    }

    public String get(String key) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }
        return properties.getProperty(key);
    }

    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public String getRequiredSecret(String environmentVariableName) {
        String value = System.getProperty(environmentVariableName);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentVariableName);
        }
        if (value == null || value.isBlank()) {
            value = properties.getProperty(environmentVariableName);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(environmentVariableName + " environment variable is required");
        }
        return value;
    }
}
