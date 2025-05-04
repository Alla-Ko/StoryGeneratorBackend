package com.example.AIStories.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Service
public class ImageUploadService {

	@Value("${imgbb.api.key}")
	private String imgbbApiKey;
	private final RestTemplate restTemplate = new RestTemplate();
	private final String TEMP_FOLDER = System.getProperty("java.io.tmpdir") + "/TempImages";

	public ImageUploadService() {
		File tempDir = new File(TEMP_FOLDER);
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}
	}

	public String uploadImage(MultipartFile file) throws IOException {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("Файл не завантажений або порожній.");
		}

		// Збереження у тимчасову папку
		Path tempFilePath = saveTempFile(file);

		try {
			String imageUrl = uploadToImgBB(tempFilePath.toFile());
			if (imageUrl == null || imageUrl.isEmpty()) {
				throw new RuntimeException("Помилка завантаження зображення на ImgBB.");
			}
			return imageUrl;
		} finally {
			Files.deleteIfExists(tempFilePath);
		}
	}

	private Path saveTempFile(MultipartFile file) throws IOException {
		String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
		Path tempFilePath = Path.of(TEMP_FOLDER, fileName);
		Files.copy(file.getInputStream(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);
		return tempFilePath;
	}

	private String uploadToImgBB(File imageFile) throws IOException {
		RestTemplate restTemplate = new RestTemplate();
		String uploadUrl = "https://api.imgbb.com/1/upload?key=" + imgbbApiKey;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		// Підготовка тіла запиту для multipart
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

		// Завантаження файлу за допомогою FileSystemResource для правильного форматування
		FileSystemResource resource = new FileSystemResource(imageFile);
		body.add("image", resource);

		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

		// Відправка POST запиту
		ResponseEntity<Map> response = restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, Map.class);

		// Перевірка відповіді
		if (response.getBody() != null && response.getBody().get("data") != null) {
			Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
			return (String) data.get("url");
		}
		return null;
	}

	public String uploadImageByteArrayResource(ByteArrayResource imageResource) {
		String uploadUrl = "https://api.imgbb.com/1/upload?key=" + imgbbApiKey;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		// Формуємо мультиформ-тіло
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("image", imageResource);

		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

		// Відправляємо запит на ImgBB
		ResponseEntity<Map> response = restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, Map.class);

		// Перевіряємо відповідь
		if (response.getBody() != null && response.getBody().get("data") != null) {
			Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
			return (String) data.get("url");
		}
		throw new RuntimeException("Помилка завантаження зображення на ImgBB.");
	}
}
