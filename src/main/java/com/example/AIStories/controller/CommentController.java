package com.example.AIStories.controller;

import com.example.AIStories.dto.CommentDTO;
import com.example.AIStories.dto.CommentUpdateDTO;
import com.example.AIStories.entity.Comment;
import com.example.AIStories.entity.GeneratedStory;
import com.example.AIStories.entity.User;
import com.example.AIStories.exception.*;
import com.example.AIStories.security.JwtService;
import com.example.AIStories.service.CommentService;
import com.example.AIStories.service.GeneratedStoryService;
import com.example.AIStories.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

	private final CommentService commentService;
	private final JwtService jwtService;
	private final UserService userService;
	private final GeneratedStoryService generatedStoryService;

	@Autowired
	public CommentController(CommentService commentService, JwtService jwtService, UserService userService, GeneratedStoryService generatedStoryService) {
		this.commentService = commentService;
		this.jwtService = jwtService;
		this.userService = userService;
		this.generatedStoryService = generatedStoryService;
	}

	// Створення або оновлення коментаря


	@PostMapping
	public ResponseEntity<Comment> createComment(@RequestBody CommentDTO commentDTO) {

		if(commentDTO.getContent()==null || commentDTO.getContent().isEmpty())throw new IllegalStateException("Коментар не має контенту");
		
		User user = userService.getCurrentUser();
		Long userId = user.getId();

		
		GeneratedStory story=generatedStoryService.getGeneratedStoryById(commentDTO.getGeneratedStoryId()).orElseThrow(() -> new RuntimeException("Історія не знайдена"));
		Comment newComment = new Comment();
		newComment.setUser(user);
		newComment.setGeneratedStory(story);
		newComment.setContent(commentDTO.getContent());

		if(commentDTO.getParentCommentId()!=null){
			Comment parentComment=commentService.getCommentById(commentDTO.getParentCommentId()).orElseThrow(() -> new RuntimeException("Батьківський коментар не знайдений"));
			if(parentComment.getContent()==null) throw new RuntimeException("Батьківський коментар видалений");
			newComment.setParentComment(parentComment);
		}
		return ResponseEntity.ok(commentService.createOrUpdateComment(newComment)); 

		
	}
	@PutMapping("/{id}")
	public ResponseEntity<Comment> updateComment(@PathVariable Long id, @RequestBody CommentUpdateDTO commentDTO) {
		Comment updatedComment=commentService.getCommentById(id).orElseThrow(() -> new RuntimeException("Коментар не знайдений"));
		if(commentDTO.getContent()==null || commentDTO.getContent().isEmpty())throw new IllegalStateException("Коментар не має контенту");

		if(!Objects.equals(userService.getCurrentUser().getId(), updatedComment.getUser().getId()))
			throw new IllegalStateException("Ви не маєте прав на редагування цього коментаря");
		updatedComment.setContent(commentDTO.getContent());
		return ResponseEntity.ok(commentService.createOrUpdateComment(updatedComment));
	}


	// Пошук коментаря за ID
	@GetMapping("/{id}")
	public ResponseEntity<Comment> getCommentById(@PathVariable Long id) {
		Optional<Comment> comment = commentService.getCommentById(id);
		return comment.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	// Пошук коментарів по історії
	@GetMapping("/story/{generatedStoryId}")
	public ResponseEntity<Optional<List<Comment>>> getCommentsByGeneratedStoryId(@PathVariable Long generatedStoryId) {
		return ResponseEntity.ok(commentService.getCommentsByGeneratedStoryId(generatedStoryId));
	}

	// Видалити коментар

	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, String>> deleteComment(@PathVariable Long id) {
		Long userId = userService.getCurrentUser().getId();
		boolean isAdmin = userService.isCurrentUserAdmin();
		Optional<Comment> comment=commentService.getCommentById(id);
		if(!comment.isPresent()){
			Map<String, String> response = new HashMap<>();
			response.put("message", "Такого коментаря не існує");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if(!isAdmin && comment.get().getUser().getId() != userId){
			Map<String, String> response = new HashMap<>();
			response.put("message", "Немає прав доступу на видалення коментаря");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}
		commentService.deleteComment(id);
		Map<String, String> response = new HashMap<>();
		response.put("message", "Коментар видалено");

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
	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Object> handleIllegalState(IllegalStateException e) {
		Map<String, String> response = new HashMap<>();
		response.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(IncorrectPasswordException.class)
	public ResponseEntity<Object> handleIncorrectPassword(IncorrectPasswordException e) {
		Map<String, String> response = new HashMap<>();
		response.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(RoleNotFoundException.class)
	public ResponseEntity<Object> handleRoleNotFound(RoleNotFoundException e) {
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
	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<Object> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
		Map<String, String> response = new HashMap<>();
		response.put("message", "Такий email вже існує");
		return ResponseEntity.badRequest().body(response);
	}
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleGeneralError(Exception e) {
		Map<String, String> response = new HashMap<>();
		response.put("message", "Щось пішло не так!");
		response.put("error", e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	
	
}
