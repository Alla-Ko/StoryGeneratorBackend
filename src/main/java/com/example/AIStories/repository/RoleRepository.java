package com.example.AIStories.repository;

import com.example.AIStories.entity.Role;
import com.example.AIStories.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

	Role findByName(String name);
}