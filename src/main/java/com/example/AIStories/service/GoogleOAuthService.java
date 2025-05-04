package com.example.AIStories.service;

import com.example.AIStories.dto.GoogleUserDTO;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@Service
public class GoogleOAuthService {

	@Value("${spring.security.oauth2.client.registration.google.client-id}")
	private static String clientId;

	@Value("${spring.security.oauth2.client.registration.google.client-secret}")
	private static String clientSecret;

	@Value("${spring.security.oauth2.client.registration.google.scope}")
	private String scope;

	@Value("${spring.security.oauth2.client.provider.google.issuer-uri}")
	private String issuerUri;

	private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
	private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
	private static final ObjectMapper objectMapper = new ObjectMapper();

	// Отримання інформації користувача
	public static GoogleUserDTO getUserInfo(String authorizationCode) {
		String accessToken = getAccessToken(authorizationCode);

		// Виконання запиту до Google API для отримання даних користувача
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken); // Додаємо токен в заголовок запиту

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(GOOGLE_USERINFO_URL, HttpMethod.GET, entity, String.class);
		System.out.println("Google user info response: " + response.getBody());
		try {
			JsonNode responseBody = objectMapper.readTree(response.getBody());

			String email = responseBody.get("email").asText();
			String picture = responseBody.get("picture").asText();

			// Повертаємо GoogleUser з отриманими даними
			return new GoogleUserDTO(email, picture);
		} catch (Exception e) {
			throw new RuntimeException("Помилка при отриманні даних користувача з Google", e);
		}
	}

	// Отримання access token за authorization code
	public static String getAccessToken(String authorizationCode) {
		String fixedCode = fixAuthorizationCode(authorizationCode);
		System.out.println("authorizationCode: " + fixedCode);
		// Параметри для запиту на обмін коду на токен
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("code", fixedCode);
		map.add("client_id", "secret");
		map.add("client_secret", "secret");
		map.add("redirect_uri", "http://localhost:4200/auth/login/google");
		map.add("grant_type", "authorization_code");

		// Виконання запиту через RestTemplate
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

		try {
			ResponseEntity<String> response = restTemplate.exchange(GOOGLE_TOKEN_URL, HttpMethod.POST, entity, String.class);

			// Логування відповіді для дебагу
			System.out.println("Response: " + response.getBody());

			// Перевірка статусу
			if (response.getStatusCode() != HttpStatus.OK) {
				throw new RuntimeException("Помилка при отриманні access token, статус відповіді: " + response.getStatusCode());
			}

			// Обробка JSON відповіді
			JsonNode responseBody = objectMapper.readTree(response.getBody());
			if (responseBody.has("access_token")) {
				return responseBody.get("access_token").asText();
			} else {
				throw new RuntimeException("Не вдалося отримати access_token з відповіді Google");
			}
		} catch (Exception e) {
			throw new RuntimeException("Помилка при отриманні access token з відповіді Google", e);
		}
	}
	public static String fixAuthorizationCode(String code) {
		try {
			// Декодуємо код, щоб замінити %2F на '/'
			return URLDecoder.decode(code, "UTF-8").replace("%2F", "/");
		} catch (UnsupportedEncodingException e) {
			// Якщо виникає помилка при декодуванні
			throw new RuntimeException("Error decoding authorization code", e);
		}
	}


}
