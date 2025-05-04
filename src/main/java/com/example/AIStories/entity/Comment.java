package com.example.AIStories.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
public class Comment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = true)
	private String content;
	@Column(unique = false, nullable = false)
	private Date createdDate;
	@ManyToOne
	@JoinColumn(name = "generated_story_id", referencedColumnName = "id", nullable = false)
	@JsonIgnoreProperties({"content", "prompt", "comments", "imageUrl", "createdDate", "public", "likesCount", "title","likes", "user", "category"})  // Ігноруємо всі поля окрім id
	private GeneratedStory generatedStory;

	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id", nullable = true) // nullable = true!
	@JsonIgnoreProperties({"username",  "avatarUrl", "createdDate", "email", "password", "roles"})
	private User user;


	@ManyToOne
	@JoinColumn(name = "parent_comment_id", referencedColumnName = "id")
	@JsonIgnoreProperties({"content", "createdDate", "generatedStory", "user", "likes", "parentComment", "createdAt", "updatedAt"})  // Ігноруємо всі поля, крім id
	private Comment parentComment;




	@OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonBackReference  // Серіалізується з історії
	private Set<CommentLike> likes;

	// Обчислювальне поле для кількості лайків
	@Transient  // Це вказує, що поле не буде збережено в базі даних
	@JsonProperty("likesCount")
	public int getLikesCount() {
		return likes != null ? likes.size() : 0;
	}
}

