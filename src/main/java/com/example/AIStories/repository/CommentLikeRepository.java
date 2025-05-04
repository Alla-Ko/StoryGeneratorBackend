package com.example.AIStories.repository;

import com.example.AIStories.entity.CommentLike;
import com.example.AIStories.entity.Like;
import com.example.AIStories.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
	// Пошук лайка по історії і користувачу
	Optional<CommentLike> findByCommentIdAndUserId(Long commentId, Long userId);

	// Можна додавати інші методи для фільтрації лайків
	Long countByCommentId(Long commentId);
	void deleteAllByUserId(Long user_id);

	@Modifying
	@Transactional
	@Query("DELETE FROM CommentLike l WHERE l.user.id = :userId AND l.comment.user.id != :userId")
	void deleteByUserIdAndNotOnUserStories(@Param("userId") Long userId);
}
