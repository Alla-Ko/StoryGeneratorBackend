package com.example.AIStories.repository;

import com.example.AIStories.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
	// Пошук коментарів по історії
	Optional<List<Comment>> findByGeneratedStoryId(Long generatedStoryId);

	// Пошук коментарів по користувачу
	Optional<List<Comment>> findByUserId(Long userId);
	Optional<List<Comment>> findByParentCommentId(Long parentCommentId);

	boolean existsByParentCommentId(Long parentCommentId);

}
