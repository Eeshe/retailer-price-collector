package me.eeshe.retailerpricecollector.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class GeminiWrapper {

  public String prompt(String prompt) {
    Client geminiClient = Client.builder().apiKey("API_KEY").build();

    GenerateContentResponse response = geminiClient.models.generateContent(
        "gemini-2.5-flash",
        prompt,
        null);

    return response.text();
  }
}
