package com.example.AIStories.controller;

import com.example.AIStories.dto.AIRequest;
import com.example.AIStories.dto.AIResponse;
import com.example.AIStories.dto.GenerateStoryDTO;
import com.example.AIStories.service.AIService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AIController {

	private final AIService aiService;

	@PreAuthorize("hasAnyAuthority('ADMIN','USER')")
	@PostMapping("/generate")
    public AIResponse generateStory(@RequestBody GenerateStoryDTO dto) throws JsonProcessingException {
        return aiService.generateStory(dto.getPrompt(),300);
    }
	@ExceptionHandler(JsonProcessingException.class)
	public ResponseEntity<Object> JsonProcessingError(Exception e) {
		Map<String, String> response = new HashMap<>();
		response.put("message", "JsonProcessingError!");
		response.put("error", e.getMessage());
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleGeneralError(Exception e) {
		Map<String, String> response = new HashMap<>();
		response.put("message", "Щось пішло не так!");
		response.put("error", e.getMessage());
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
