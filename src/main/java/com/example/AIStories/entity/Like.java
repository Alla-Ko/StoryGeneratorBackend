package com.example.AIStories.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
		name = "likes",
		uniqueConstraints = @UniqueConstraint(columnNames = {"generated_story_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class Like {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "generated_story_id", referencedColumnName = "id", nullable = false)
	@JsonIgnoreProperties({"content", "prompt", "comments", "likes", "user", "category"})  // Ігноруємо всі поля окрім id
	private GeneratedStory generatedStory;

	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
	@JsonIgnoreProperties({"username", "email", "password", "roles"}) // Ігноруємо решту полів User
	private User user;
}

