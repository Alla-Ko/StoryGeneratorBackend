package com.example.AIStories.controller;

import com.example.AIStories.dto.TranslationRequest;
import com.example.AIStories.dto.TranslationResponse;
import com.example.AIStories.exception.TranslationException;
import com.example.AIStories.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/translate")
public class TranslationController {

	private final TranslationService translationService;

	@Autowired
	public TranslationController(TranslationService translationService) {
		this.translationService = translationService;
	}

	// Метод для перекладу тексту з української на англійську
	@PostMapping
	public ResponseEntity<TranslationResponse> translateText(@RequestBody TranslationRequest request) {
		String translatedText = translationService.translateUkToEn(request.getText());
		return ResponseEntity.ok(new TranslationResponse(translatedText));
	}

	// Обробка помилок перекладу
	@ExceptionHandler(TranslationException.class)
	public ResponseEntity<Object> handleTranslationError(TranslationException e) {
		Map<String, String> response = new HashMap<>();
		response.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	// Загальна обробка помилок
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleGeneralError(Exception e) {
		Map<String, String> response = new HashMap<>();
		response.put("message", "Щось пішло не так з перекладом!");
		response.put("error", e.getMessage());
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
