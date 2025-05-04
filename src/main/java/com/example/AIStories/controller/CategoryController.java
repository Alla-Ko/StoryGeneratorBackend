package com.example.AIStories.controller;

import com.example.AIStories.dto.CategoryDTO;
import com.example.AIStories.entity.Category;
import com.example.AIStories.exception.CategoryNotFoundException;
import com.example.AIStories.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

	private final CategoryService categoryService;

	@Autowired
	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	// Створення нової категорії
	@PreAuthorize("hasAuthority('ADMIN')")
	@PostMapping
	public ResponseEntity<Category> createCategory(@RequestBody CategoryDTO category) {
		Category newCategory = new Category();
		newCategory.setName(category.getCategoryName());
		return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createOrUpdateCategory(newCategory));
	}

	// Оновлення існуючої категорії
	@PreAuthorize("hasAuthority('ADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO category) {
		Category existingCategory = categoryService.getCategoryById(id);

		existingCategory.setName(category.getCategoryName());
		// Тепер передаємо категорію для оновлення
		return ResponseEntity.ok(categoryService.createOrUpdateCategory(existingCategory));
	}

	// Пошук категорії за ID
	@GetMapping("/{id}")
	public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
		Category category = categoryService.getCategoryById(id); // Отримуємо категорію
		return ResponseEntity.ok(category); // Якщо категорія знайдена, повертаємо її
	}


	// Отримати всі категорії
	@GetMapping
	public ResponseEntity<List<Category>> getAllCategories() {
		return ResponseEntity.ok(categoryService.getAllCategories());
	}

	// Видалити категорію
	@PreAuthorize("hasAuthority('ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, String>> deleteCategory(@PathVariable Long id) {

		categoryService.deleteCategory(id); // Видаляємо категорію
		Map<String, String> response = new HashMap<>();
		response.put("message", "Категорія з ID " + id + " успішно видалена");
		return ResponseEntity.status(HttpStatus.OK).body(response); // Повертаємо статус 200 з повідомленням
	}

	@ExceptionHandler(CategoryNotFoundException.class)
	public ResponseEntity<Object> handleCategoryNotFoundError(Exception e) {
		Map<String, String> response = new HashMap<>();
		response.put("message", "Щось з категоріями пішло не так!");
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

