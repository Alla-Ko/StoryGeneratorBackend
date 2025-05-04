package com.example.AIStories.repository;

import com.example.AIStories.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	// Можна додати додаткові методи запитів, якщо потрібно
	Optional<Category> findByName(String name);
	Optional<Category> findById(Long id);
}
