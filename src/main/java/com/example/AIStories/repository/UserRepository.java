package com.example.AIStories.repository;

import com.example.AIStories.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
	Optional<User> findByUsername(String username);
	Optional<User>findByNickname(String nickname);
	boolean existsByEmail(String email);
}
