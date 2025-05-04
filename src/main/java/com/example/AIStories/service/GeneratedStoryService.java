package com.example.AIStories.service;

import com.example.AIStories.entity.GeneratedStory;
import com.example.AIStories.repository.CategoryRepository;
import com.example.AIStories.repository.GeneratedStoryRepository;
import com.example.AIStories.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GeneratedStoryService {

	@Autowired
	private GeneratedStoryRepository generatedStoryRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private UserRepository userRepository;


	// Створення або оновлення історії
	public GeneratedStory createOrUpdateGeneratedStory(GeneratedStory generatedStory) {
		if(generatedStory.getId()==null||generatedStory.getId()==0)generatedStory.setCreatedDate(new Date());
		return generatedStoryRepository.save(generatedStory);
	}

	// Пошук історії за ID
	public Optional<GeneratedStory> getGeneratedStoryById(Long id) {
		return generatedStoryRepository.findById(id);
	}

	// Пошук історії за категорією
	public Optional<List<GeneratedStory>> getGeneratedStoriesByCategoryId(Long categoryId) {
		return generatedStoryRepository.findByCategoryId(categoryId);
	}

	// Пошук історії за користувачем
	public Optional<List<GeneratedStory>> getGeneratedStoriesByUserId(Long userId) {
		return generatedStoryRepository.findByUserId(userId);
	}

	// Отримати всі історії
	public List<GeneratedStory> getAllGeneratedStories() {
		return generatedStoryRepository.findAll();
	}

	// Видалити історію
	@Transactional
	public void deleteGeneratedStory(Long id) {
		generatedStoryRepository.deleteById(id);
	}

	// Пошук історій по кількох словах
	public List<GeneratedStory> searchGeneratedStories(String[] queryWords) {
		return generatedStoryRepository.findAll().stream()
				.filter(story -> Arrays.stream(queryWords)
						.allMatch(word -> story.getPrompt().toLowerCase().contains(word.toLowerCase()) ||
								story.getContent().toLowerCase().contains(word.toLowerCase()) ||
								story.getTitle().toLowerCase().contains(word.toLowerCase())))
				.collect(Collectors.toList());
	}


	// Отримати пролайкані історії користувача
	public Optional<List<GeneratedStory>> getLikedStoriesByUserId(Long userId) {
		return generatedStoryRepository.findLikedStoriesByUserId(userId);  // відповідний запит у репозиторії для лайків
	}

	public Optional<GeneratedStory> getStoryById(Long id) {
		return generatedStoryRepository.findById(id);  // відповідний запит у репозиторі�� для отримання ��сторі�� по id
	}
}
