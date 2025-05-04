package com.example.AIStories.controller;

import com.example.AIStories.service.ImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/image")
@RequiredArgsConstructor
public class ImageUploadController {

	private final ImageUploadService imageUploadService;

	@Operation(
			summary = "Завантаження зображення",
			responses = {
					@ApiResponse(responseCode = "200", description = "Успішне завантаження", content = @Content(schema = @Schema(implementation = Map.class))),
					@ApiResponse(responseCode = "400", description = "Помилка завантаження зображення")
			}
	)
	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> uploadImage(
			@Parameter(description = "Файл зображення", required = true)
			@RequestPart("file") MultipartFile file) {
		Map<String, String> response = new HashMap<>();
		try {
			// Validate file size or type if necessary`
			String imageUrl = imageUploadService.uploadImage(file);
			response.put("imageUrl", imageUrl);
			return ResponseEntity.ok(response);
		} catch (IOException e) {
			response.put("message", "Помилка завантаження зображення: Невірний формат або проблема з файлом.");
			response.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		} catch (Exception e) {
			response.put("message", "Помилка завантаження зображення.");
			response.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
}
