package com.example.AIStories.repository;

import com.example.AIStories.entity.Like;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
	// Пошук лайка по історії і користувачу
	Optional<Like> findByGeneratedStoryIdAndUserId(Long generatedStoryId, Long userId);

	// Можна додавати інші методи для фільтрації лайків
	Long countByGeneratedStoryId(Long generatedStoryId);

	void deleteAllByUserId(Long userId);

	@Modifying
	@Transactional
	@Query("DELETE FROM Like l WHERE l.user.id = :userId AND l.generatedStory.user.id != :userId")
	void deleteByUserIdAndNotOnUserStories(@Param("userId") Long userId);
}
