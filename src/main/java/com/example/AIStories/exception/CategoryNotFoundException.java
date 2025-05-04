package com.example.AIStories.exception;

public class CategoryNotFoundException  extends RuntimeException {
	public CategoryNotFoundException(Long id) {
		super("Категорія з ID " + id + " не знайдена");
	}
}
