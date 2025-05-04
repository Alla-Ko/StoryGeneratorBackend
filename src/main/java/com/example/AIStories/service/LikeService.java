package com.example.AIStories.service;

import com.example.AIStories.entity.GeneratedStory;
import com.example.AIStories.entity.Like;
import com.example.AIStories.entity.User;
import com.example.AIStories.repository.GeneratedStoryRepository;
import com.example.AIStories.repository.LikeRepository;
import com.example.AIStories.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class LikeService {

	private final LikeRepository likeRepository;
	private final GeneratedStoryRepository generatedStoryRepository;
	private final UserRepository userRepository;

	@Autowired
	public LikeService(LikeRepository likeRepository, GeneratedStoryRepository generatedStoryRepository, UserRepository userRepository) {
		this.likeRepository = likeRepository;
		this.generatedStoryRepository = generatedStoryRepository;
		this.userRepository = userRepository;
	}

	// Додавання лайку
	public Like addLike(Like like) {
		return likeRepository.save(like);
	}

	// Перевірка чи користувач поставив лайк на цю історію
	public Optional<Like> getLikeByGeneratedStoryIdAndUserId(Long generatedStoryId, Long userId) {
		return likeRepository.findByGeneratedStoryIdAndUserId(generatedStoryId, userId);
	}

	// Підрахувати кількість лайків для історії
	public Long countLikesByGeneratedStoryId(Long generatedStoryId) {
		return likeRepository.countByGeneratedStoryId(generatedStoryId);
	}

	// Видалити лайк
	public void deleteLike(Long id) {
		likeRepository.deleteById(id);
	}
	public Map<String, String> toggleLike(Long generatedStoryId, Long userId) {
		// Знайдемо історію та користувача за ID
		GeneratedStory generatedStory = generatedStoryRepository.findById(generatedStoryId)
				.orElseThrow(() -> new EntityNotFoundException("Історію не знайдено за ID: " + generatedStoryId));

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("Користувача не знайдено за ID: " + userId));

		// Перевірка на існування лайка для цієї комбінації
		Optional<Like> existingLike = likeRepository.findByGeneratedStoryIdAndUserId(generatedStoryId, userId);

		Map<String, String> response = new HashMap<>();

		if (existingLike.isPresent()) {
			// Якщо лайк вже існує, видаляємо
			likeRepository.delete(existingLike.get());
			response.put("message", "Лайк видалено");
		} else {
			// Якщо лайк не існує, створюємо новий і додаємо
			Like newLike = new Like();
			newLike.setGeneratedStory(generatedStory);
			newLike.setUser(user);
			likeRepository.save(newLike);
			response.put("message", "Лайк додано");
		}

		return response;
	}

	public void deleteAllLikesByUser(Long userId) {
		likeRepository.deleteAllByUserId(userId);
	}

	@Transactional
	public void deleteLikesOnOtherUsersStories(Long userId) {
		likeRepository.deleteByUserIdAndNotOnUserStories(userId);
	}
}
