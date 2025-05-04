package com.example.AIStories.service;

import com.example.AIStories.entity.Comment;
import com.example.AIStories.entity.User;
import com.example.AIStories.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

	private final CommentRepository commentRepository;
	private final UserService userService;

	@Autowired
	public CommentService(CommentRepository commentRepository, @Lazy UserService userService) {
		this.commentRepository = commentRepository;
		this.userService = userService;
	}

	// Створення або оновлення коментаря
	public Comment createOrUpdateComment(Comment comment) {
		if(comment.getId()==null||comment.getId()==0)comment.setCreatedDate(new Date());
		return commentRepository.save(comment);
	}

	// Пошук коментаря за ID
	public Optional<Comment> getCommentById(Long id) {
		return commentRepository.findById(id);
	}

	// Пошук коментарів по історії
	public Optional<List<Comment>> getCommentsByGeneratedStoryId(Long generatedStoryId) {
		return commentRepository.findByGeneratedStoryId(generatedStoryId);
	}

	// Пошук коментарів по користувачу
	public Optional<List<Comment>> getCommentsByUserId(Long userId) {
		return commentRepository.findByUserId(userId);
	}

	// Отримати всі коментарі
	public List<Comment> getAllComments() {
		return commentRepository.findAll();
	}

	// Видалити коментар
	public void deleteComment(Long id) {
		Comment comment = commentRepository.findById(id).orElseThrow (() -> new EntityNotFoundException("Коментар не знайдено"));
		boolean hasChildren = commentRepository.existsByParentCommentId(id);
		if (hasChildren) {
			// Якщо є дочірні коментарі, очищаємо вміст та автора
			comment.setContent(null);
			User deletedUser = userService.findByUsername("deleted@example.com")
					.orElseThrow(() -> new EntityNotFoundException("DeletedUser не знайдено"));

			comment.setUser(deletedUser);
			commentRepository.save(comment);
		} else {
			// Якщо дочірніх коментарів немає – просто видаляємо
			commentRepository.deleteById(id);
		}
	}

	public void updateCommentsSetUserToDeleted(Long userId) {
		User deletedUser = userService.findByUsername("deleted@example.com")
                .orElseThrow(() -> new EntityNotFoundException("DeletedUser не знайдено"));

        List<Comment> commentsByUser = commentRepository.findByUserId(userId).orElseThrow(() -> new EntityNotFoundException("Користувача не знайдено"));
        commentsByUser.forEach(comment -> {
            comment.setUser(deletedUser);
            commentRepository.save(comment);
        });
	}
}
