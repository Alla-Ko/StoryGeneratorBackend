package com.example.AIStories.controller;

import com.example.AIStories.dto.GenerateStoryDTO;
import com.example.AIStories.service.ImageUploadService;
import com.example.AIStories.service.StabilityAiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ai/images")
public class StabilityAiController {

	private final StabilityAiService stabilityAiService;


	public StabilityAiController(StabilityAiService stabilityAiService) {
		this.stabilityAiService = stabilityAiService;
	}
	@PostMapping("/generate")
	public ResponseEntity<Map<String,String>> generateImage(@RequestParam GenerateStoryDTO prompt) {
		String imageUrl = stabilityAiService.generateImage(prompt.getPrompt());
		Map<String, String> response = new HashMap<>();
		response.put("imageUrl", imageUrl);
		return ResponseEntity.ok(response);
	}
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleGeneralError(Exception e) {
		Map<String, String> response = new HashMap<>();
		response.put("message", "Не вдалося згенерувати зображення!");
		response.put("error", e.getMessage());
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
