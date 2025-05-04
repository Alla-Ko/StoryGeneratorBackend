package com.example.AIStories.service;

import com.example.AIStories.entity.Category;
import com.example.AIStories.exception.CategoryNotFoundException;
import com.example.AIStories.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;

	@Autowired
	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	// Створення або оновлення категорії
	public Category createOrUpdateCategory(Category category) {
		return categoryRepository.save(category);
	}

	// Пошук категорії за ID
	public Category getCategoryById(Long id) {
		return categoryRepository.findById(id)
				.orElseThrow(() -> new CategoryNotFoundException(id)); // Якщо категорія не знайдена, кидаємо виняток
	}

	// Пошук категорії за назвою
	public Optional<Optional<Category>> getCategoryByName(String name) {
		return Optional.ofNullable(categoryRepository.findByName(name));
	}

	// Отримати всі категорії
	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	// Видалити категорію
	public void deleteCategory(Long id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new CategoryNotFoundException(id));
		categoryRepository.deleteById(id);
	}
}
