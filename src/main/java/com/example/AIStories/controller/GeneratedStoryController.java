package com.example.AIStories.controller;
import com.example.AIStories.dto.GeneratedStoryDTO;
import com.example.AIStories.entity.Category;
import com.example.AIStories.entity.GeneratedStory;
import com.example.AIStories.entity.User;
import com.example.AIStories.exception.*;
import com.example.AIStories.repository.CategoryRepository;
import com.example.AIStories.security.JwtService;
import com.example.AIStories.service.GeneratedStoryService;
import com.example.AIStories.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/generated-stories")
public class GeneratedStoryController {

	@Autowired
	private GeneratedStoryService generatedStoryService;

	@Autowired
	private JwtService jwtService;
	@Autowired
	private UserService userService;
	@Autowired
	private CategoryRepository categoryRepository;

	@PreAuthorize("hasAnyAuthority('ADMIN','USER')")
	@PostMapping
	public ResponseEntity<GeneratedStory> createGeneratedStory(@RequestBody GeneratedStoryDTO generatedStoryDTO) {
		
		User user = userService.getCurrentUser();
		if(generatedStoryDTO.getContent().isEmpty())
			throw new IllegalStateException("Історія не має контенту");
		if(generatedStoryDTO.getPrompt().isEmpty())
			throw new IllegalStateException("Історія не має підказки");
		if(generatedStoryDTO.getTitle().isEmpty())
			throw new IllegalStateException("Історія не має заголовку");
        Category category = categoryRepository.findById(generatedStoryDTO.getCategoryId()).orElseThrow (() -> new EntityNotFoundException("Категорію не знайдено"));

		GeneratedStory generatedStory =new GeneratedStory();
		generatedStory.setContent(generatedStoryDTO.getContent());
		generatedStory.setPrompt(generatedStoryDTO.getPrompt());
		generatedStory.setUser(user);
		generatedStory.setTitle(generatedStoryDTO.getTitle());
		generatedStory.setCategory(category);
		generatedStory.setPublic(generatedStoryDTO.getIsPublic());
		if(!(generatedStoryDTO.getImageUrl()==null||generatedStoryDTO.getImageUrl().isEmpty()))generatedStory.setImageUrl(generatedStoryDTO.getImageUrl());
		GeneratedStory newStory = generatedStoryService.createOrUpdateGeneratedStory(generatedStory);
		return ResponseEntity.ok(newStory);

	}
	@PreAuthorize("hasAnyAuthority('ADMIN','USER')")
	@PutMapping("/{id}")
	public ResponseEntity<GeneratedStory> updateStory(@PathVariable Long id, @RequestBody GeneratedStoryDTO generatedStoryDTO) {
		
		Long userId = userService.getCurrentUser().getId();

		GeneratedStory generatedStory = generatedStoryService.getGeneratedStoryById(id).orElseThrow (() -> new EntityNotFoundException("Історію не знайдено"));
		if(!generatedStory.getUser().getId().equals(userId)) throw new IllegalStateException("В доступі відмовлено, тільки автор може редагувати історію");
		Category category;
		if(generatedStoryDTO.getCategoryId()!=null) {
			category = categoryRepository.findById(generatedStoryDTO.getCategoryId()).orElseThrow(() -> new EntityNotFoundException("Категорію не знайдено"));
			generatedStory.setCategory(category);
		}
		if(!(generatedStoryDTO.getContent()==null||generatedStoryDTO.getContent().isEmpty()))generatedStory.setContent(generatedStoryDTO.getContent());
		if(!(generatedStoryDTO.getPrompt()==null||generatedStoryDTO.getPrompt().isEmpty())) generatedStory.setPrompt(generatedStoryDTO.getPrompt());

		if(!(generatedStoryDTO.getTitle()==null||generatedStoryDTO.getTitle().isEmpty())) generatedStory.setTitle(generatedStoryDTO.getTitle());

		generatedStory.setPublic(generatedStoryDTO.getIsPublic());
		if(!(generatedStoryDTO.getImageUrl()==null||generatedStoryDTO.getImageUrl().isEmpty()))generatedStory.setImageUrl(generatedStoryDTO.getImageUrl());
		GeneratedStory updatedStory = generatedStoryService.createOrUpdateGeneratedStory(generatedStory);
		return ResponseEntity.ok(updatedStory);

	}

	@GetMapping("/{id}")
	public ResponseEntity<GeneratedStory> getGeneratedStoryById(@PathVariable Long id) {

		GeneratedStory generatedStory = generatedStoryService.getGeneratedStoryById(id).orElseThrow (() -> new EntityNotFoundException("Історію не знайдено"));

		// Перевірка доступу до приватних історій
		if (!generatedStory.isPublic()) {
			if(userService.getCurrentUser()==null||!userService.getCurrentUser().getId().equals(generatedStory.getUser().getId())){
				throw new IllegalStateException("Історія не опублікована на загал"); // Якщо історія не публічна і користувач не автор, повертаємо помилку
			}
		}

		return ResponseEntity.ok(generatedStory);
	}

	@GetMapping
	public ResponseEntity<List<GeneratedStory>> getAllGeneratedStories() {
		List<GeneratedStory> allStories = generatedStoryService.getAllGeneratedStories();

		// Фільтруємо публічні історії
		List<GeneratedStory> publicStories = allStories.stream()
				.filter(GeneratedStory::isPublic)
				.collect(Collectors.toList());

		return ResponseEntity.ok(publicStories);
	}

	@GetMapping("/category/{categoryId}")
	public ResponseEntity<List<GeneratedStory>> getGeneratedStoriesByCategoryId(@PathVariable Long categoryId) {

		Optional<List<GeneratedStory>> generatedStoriesOpt = generatedStoryService.getGeneratedStoriesByCategoryId(categoryId);

		if (generatedStoriesOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		// Фільтруємо публічні історії за категорією
		List<GeneratedStory> publicStories = generatedStoriesOpt.get().stream()
				.filter(GeneratedStory::isPublic)
				.collect(Collectors.toList());

		return ResponseEntity.ok(publicStories);
	}

	@GetMapping("/search")
	public ResponseEntity<List<GeneratedStory>> searchGeneratedStories(@RequestParam String query) {


		// Розбиваємо запит на слова
		String[] queryWords = query.split("[\\s,;.!?]+");

		// Пошук історій по запиту
		List<GeneratedStory> matchedStories = generatedStoryService.searchGeneratedStories(queryWords);

		// Фільтруємо публічні історії
		matchedStories = matchedStories.stream()
				.filter(GeneratedStory::isPublic)
				.collect(Collectors.toList());

		return ResponseEntity.ok(matchedStories);
	}
	@PreAuthorize("hasAnyAuthority('ADMIN','USER')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String,String>> deleteGeneratedStory(@PathVariable Long id) {
		Long userId =  userService.getCurrentUser().getId();

		// Перевірка чи користувач є автором історії
		Optional<GeneratedStory> generatedStory = generatedStoryService.getGeneratedStoryById(id);
		if (generatedStory.isEmpty()) {
			Map<String, String> response = new HashMap<>();
			response.put("message", "Такої історії не існує");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}
		if (!generatedStory.get().getUser().getId().equals(userId)) {
			Map<String, String> response = new HashMap<>();
			response.put("message", "Ви не маєте прав на видалення цієї історії.");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}
		// Видалення історії
		generatedStoryService.deleteGeneratedStory(id);
		Map<String, String> response = new HashMap<>();
		response.put("message", "Історія видалена");
		return ResponseEntity.ok(response);
	}
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<GeneratedStory>> getGeneratedStoriesByUserId(@PathVariable Long userId) {
		Optional<List<GeneratedStory>> generatedStories = generatedStoryService.getGeneratedStoriesByUserId(userId);

		if (generatedStories.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		// Фільтруємо тільки публічні історії
		List<GeneratedStory> publicStories = generatedStories.get().stream()
				.filter(GeneratedStory::isPublic)
				.collect(Collectors.toList());

		return ResponseEntity.ok(publicStories);
	}
	@PreAuthorize("hasAnyAuthority('ADMIN','USER')")
	@GetMapping("/created-me")
	public ResponseEntity<List<GeneratedStory>> getMyStories() {
		Long userId =  userService.getCurrentUser().getId();
		Optional<List<GeneratedStory>> generatedStories = generatedStoryService.getGeneratedStoriesByUserId(userId);

		if (generatedStories.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		// Фільтруємо тільки публічні історії
		List<GeneratedStory> publicStories = new ArrayList<>(generatedStories.get());

		return ResponseEntity.ok(publicStories);
	}
	@PreAuthorize("hasAnyAuthority('ADMIN','USER')")
	@GetMapping("/liked-me")
	public ResponseEntity<List<GeneratedStory>> getLikedStoriesByMe() {
		Long userId =  userService.getCurrentUser().getId();
		Optional<List<GeneratedStory>> likedStories = generatedStoryService.getLikedStoriesByUserId(userId);

		if (likedStories.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		// Фільтруємо тільки публічні історії
		List<GeneratedStory> publicLikedStories = likedStories.get().stream()
				.filter(GeneratedStory::isPublic)
				.collect(Collectors.toList());

		return ResponseEntity.ok(publicLikedStories);
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
	public ResponseEntity<Object> handleUserNotFound(IllegalStateException e) {
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
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

}
