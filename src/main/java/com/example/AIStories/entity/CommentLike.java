package com.example.AIStories.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
		name = "comment_likes",
		uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class CommentLike {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "comment_id", referencedColumnName = "id", nullable = false)
	@JsonIgnoreProperties({"content", "createdDate", "generatedStory", "user", "likes", "parentComment", "createdAt", "updatedAt"}) // Ігноруємо всі поля окрім id
	private Comment comment;

	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
	@JsonIgnoreProperties({"username", "email", "password", "roles"}) // Ігноруємо решту полів User
	private User user;
}
