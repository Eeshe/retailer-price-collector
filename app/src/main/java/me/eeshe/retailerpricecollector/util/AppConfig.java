package me.eeshe.retailerpricecollector.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
  private static Properties properties = new Properties();

  static {
    try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
      if (input == null) {
        System.out.println("Unable to find config.properties");
      } else {
        properties.load(input);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String getGeminiAPIKey() {
    return properties.getProperty("GEMINI_API_KEY");
  }
}
