package com.example.AIStories.service;

import com.example.AIStories.dto.AIRequest;
import com.example.AIStories.dto.AIResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AIService {
	private final String path = "https://api.openai.com/v1/chat/completions";

	@Value("${gpt.token}")
	private String token;

	private final RestTemplate restTemplate = new RestTemplate();

	public AIResponse generateStory(String prompt, Integer maxTokens) throws JsonProcessingException {
		AIRequest request = new AIRequest(prompt,maxTokens);

		// Створення HTTP-запиту
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(token);  // Додаємо Bearer Token

		HttpEntity<AIRequest> entity = new HttpEntity<>(request, headers);
		System.out.println("Request Body: " + new ObjectMapper().writeValueAsString(request));
		// Виконання POST-запиту
		ResponseEntity<AIResponse> response = restTemplate.exchange(
				path, HttpMethod.POST, entity, AIResponse.class
		);
		System.out.println("Response Status: " + response.getStatusCode());
		System.out.println("Response Body: " + response.getBody());
		// Повертаємо всю відповідь API
		if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
			return response.getBody();
		} else {
			throw new RuntimeException("Не вдалося отримати відповідь від OpenAI API");
		}
	}
}
