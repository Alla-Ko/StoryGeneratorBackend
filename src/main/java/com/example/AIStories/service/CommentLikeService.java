package com.example.AIStories.service;

import com.example.AIStories.entity.Comment;
import com.example.AIStories.entity.CommentLike;
import com.example.AIStories.entity.Like;
import com.example.AIStories.entity.User;
import com.example.AIStories.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CommentLikeService {

	private final CommentLikeRepository commentLikeRepository;
	private final UserRepository userRepository;
	private final CommentRepository commentRepository;

	@Autowired
	public CommentLikeService(CommentLikeRepository commentLikeRepository, UserRepository userRepository, CommentRepository commentRepository) {
		this.commentLikeRepository = commentLikeRepository;
		this.userRepository = userRepository;
		this.commentRepository = commentRepository;
	}

	// Додавання лайку
	public CommentLike addLike(CommentLike like) {
		return commentLikeRepository.save(like);
	}

	// Перевірка чи користувач поставив лайк на цю історію
	public Optional<CommentLike> getLikeByCommentIdAndUserId(Long commentId, Long userId) {
		return commentLikeRepository.findByCommentIdAndUserId(commentId, userId);
	}

	// Підрахувати кількість лайків для історії
	public Long countLikesByCommentId(Long commentId) {
		return commentLikeRepository.countByCommentId(commentId);
	}

	// Видалити лайк
	public void deleteLike(Long id) {
		commentLikeRepository.deleteById(id);
	}
	public Map<String, String> toggleLike(Long commentId, Long userId) {
		// Знайдемо історію та користувача за ID
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new EntityNotFoundException("Коментар не знайдено за ID: " + commentId));

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("Користувача не знайдено за ID: " + userId));

		// Перевірка на існування лайка для цієї комбінації
		Optional<CommentLike> existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId);

		Map<String, String> response = new HashMap<>();

		if (existingLike.isPresent()) {
			// Якщо лайк вже існує, видаляємо
			commentLikeRepository.delete(existingLike.get());
			response.put("message", "Лайк видалено");
		} else {
			// Якщо лайк не існує, створюємо новий і додаємо
			CommentLike newLike = new CommentLike();
			newLike.setComment(comment);
			newLike.setUser(user);
			commentLikeRepository.save(newLike);
			response.put("message", "Лайк додано");
		}

		return response;
	}
	public void deleteAllLikesByUser(Long userId){
		commentLikeRepository.deleteAllByUserId(userId);
	}

	@Transactional
	public void deleteLikesOnOtherUsersStories(Long userId) {
		commentLikeRepository.deleteByUserIdAndNotOnUserStories(userId);
	}
}
