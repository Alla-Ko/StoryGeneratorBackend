package com.example.AIStories.service;
import com.example.AIStories.exception.TranslationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Service
public class TranslationService {
	private final RestTemplate restTemplate = new RestTemplate();
	@Value("${transl.api.key}")
	private String translApiKey;
	public String translateUkToEn(String text) {
		String url = UriComponentsBuilder.fromHttpUrl("https://api.mymemory.translated.net/get")
				.queryParam("q", text) // Передаємо без кодування!
				.queryParam("langpair", "uk|en") // Без змін
				.queryParam("key", translApiKey)
				.build()
				.toUriString(); // НЕ викликаємо encode()

		System.out.println("Generated URL: " + url); // Лог для перевірки

		try {
			TranslationResponse response = restTemplate.getForObject(url, TranslationResponse.class);
			if (response == null || response.getResponseData() == null || response.getResponseData().getTranslatedText() == null) {
				throw new TranslationException("Помилка: відповідь API порожня або некоректна.");
			}

			return response.getResponseData().getTranslatedText();
		} catch (RestClientException e) {
			throw new TranslationException("Помилка під час запиту до API перекладу: " + e.getMessage());
		}
	}


	// DTO клас для JSON-відповіді
	private static class TranslationResponse {
		private ResponseData responseData;

		public ResponseData getResponseData() {
			return responseData;
		}

		public void setResponseData(ResponseData responseData) {
			this.responseData = responseData;
		}
	}

	private static class ResponseData {
		private String translatedText;

		public String getTranslatedText() {
			return translatedText;
		}

		public void setTranslatedText(String translatedText) {
			this.translatedText = translatedText;
		}
	}
}
