package me.eeshe.retailerpricecollector.service;

import com.google.genai.Client;
import com.google.genai.errors.ClientException;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.ThinkingConfig;

import me.eeshe.retailerpricecollector.util.AppConfig;

public class GeminiWrapper {

  public String prompt(String prompt) {
    Client geminiClient = Client.builder().apiKey(AppConfig.getGeminiAPIKey()).build();

    try {
      GenerateContentResponse response = geminiClient.models.generateContent(
          "gemini-2.5-flash",
          prompt,
          GenerateContentConfig.builder().thinkingConfig(ThinkingConfig.builder().thinkingBudget(0).build()).build());
      return response.text();

    } catch (ClientException e) {
      System.out.println(e.getMessage());
      System.out.println(e.message());
      try {
        Thread.sleep(10000);
      } catch (InterruptedException e1) {
        e1.printStackTrace();
      }
      return prompt(prompt);
    }
  }
}
