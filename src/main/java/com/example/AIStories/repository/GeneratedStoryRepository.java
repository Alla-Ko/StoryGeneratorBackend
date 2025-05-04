package com.example.AIStories.repository;

import com.example.AIStories.entity.GeneratedStory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeneratedStoryRepository extends JpaRepository<GeneratedStory, Long> {
	// Можна додавати додаткові методи для пошуку або фільтрації історій
	Optional<List<GeneratedStory>> findByCategoryId(Long categoryId);
	Optional<List<GeneratedStory>> findByUserId(Long userId);
	// Отримати всі історії, до яких поставлено лайк певним користувачем
	@Query("SELECT gs FROM GeneratedStory gs JOIN gs.likes l WHERE l.user.id = :userId")
	Optional<List<GeneratedStory>> findLikedStoriesByUserId(Long userId);
}
