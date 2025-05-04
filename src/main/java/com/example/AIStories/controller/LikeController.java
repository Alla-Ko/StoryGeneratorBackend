package com.example.AIStories.controller;

import com.example.AIStories.entity.Like;
import com.example.AIStories.exception.TokenExpiredException;
import com.example.AIStories.exception.UserNotFoundException;
import com.example.AIStories.security.JwtService;
import com.example.AIStories.service.LikeService;
import com.example.AIStories.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

	private final LikeService likeService;
	private final JwtService jwtService;
	private final UserService userService;
	@Autowired
	public LikeController(LikeService likeService, JwtService jwtService, UserService userService) {
		this.likeService = likeService;
		this.jwtService = jwtService;
		this.userService = userService;
	}

	// Тогл метод для додавання або видалення лайка
	@PostMapping("/toggle")
	public ResponseEntity<Map<String, String>> toggleLike(@RequestParam Long generatedStoryId) {
		// Викликаємо сервіс для додавання або видалення лайка
		Long userId = userService.getCurrentUser().getId();
		Map<String, String> response = likeService.toggleLike(generatedStoryId, userId);
		return ResponseEntity.ok(response);
	}
	//	----------------------------------------------------------------
//			ВИКЛЮЧЕННЯ
//	----------------------------------------------------------------
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<Object> handleUserNotFound(UserNotFoundException e) {
		Map<String, String> response = new HashMap<>();
		response.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}
	@ExceptionHandler(TokenExpiredException.class)
	public ResponseEntity<Object> handleTokenExpired(TokenExpiredException e) {
		Map<String, String> response = new HashMap<>();
		response.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleGeneralError(Exception e) {
		Map<String, String> response = new HashMap<>();
		response.put("message", "Щось з лайками пішло не так!");
		response.put("error", e.getMessage());
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}

