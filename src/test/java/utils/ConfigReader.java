package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private final Properties properties = new Properties();

    public ConfigReader() {
        loadRequiredProperties("config.properties");
        loadOptionalProperties("secrets.local.properties");
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
        String value = getOptionalSecret(environmentVariableName);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(environmentVariableName + " environment variable is required");
        }
        return value;
    }

    public String getOptionalSecret(String environmentVariableName) {
        String value = System.getProperty(environmentVariableName);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentVariableName);
        }
        if (value == null || value.isBlank()) {
            value = properties.getProperty(environmentVariableName);
        }
        return value;
    }

    private void loadRequiredProperties(String fileName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IllegalStateException(fileName + " file not found");
            }
            properties.load(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load " + fileName, exception);
        }
    }

    private void loadOptionalProperties(String fileName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load " + fileName, exception);
        }
    }
}
