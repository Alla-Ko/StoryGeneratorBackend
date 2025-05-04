package com.example.AIStories.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;

@Service
public class StabilityAiService {
	private static final String API_URL = "https://api.stability.ai/v2beta/stable-image/generate/sd3";

	@Value("${stability.token}")
	private String BEARER_TOKEN;

	private final RestTemplate restTemplate = new RestTemplate();
	private final String TEMP_IMAGE_PATH = System.getProperty("java.io.tmpdir") + "/generated_image.jpg";
	private final ImageUploadService imageUploadService;

	public StabilityAiService(ImageUploadService imageUploadService) {
		this.imageUploadService = imageUploadService;
	}

	public String generateImage(String prompt) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(BEARER_TOKEN);
		headers.setAccept(Collections.singletonList(MediaType.parseMediaType("image/*")));
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("prompt", prompt);
		body.add("aspect_ratio", "1:1");
		body.add("output_format", "jpeg");

		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

		ResponseEntity<byte[]> response = restTemplate.exchange(
				API_URL,
				HttpMethod.POST,
				requestEntity,
				byte[].class
		);

		if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
			ByteArrayResource imageResource = new ByteArrayResource(response.getBody()) {
				@Override
				public String getFilename() {
					return "generated_image.jpg";
				}
			};

			// Завантажуємо отримане зображення на ImgBB
			return imageUploadService.uploadImageByteArrayResource(imageResource);
		} else {
			throw new RuntimeException("Помилка отримання зображення: " + response.getStatusCode());
		}
	}
}
