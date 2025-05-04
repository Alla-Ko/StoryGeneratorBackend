package com.example.AIStories.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "generated_stories")
@Getter
@Setter
@NoArgsConstructor
public class GeneratedStory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String prompt;
	private String content;
	private String imageUrl;
	private String title;
	private boolean isPublic=false;
	@Column(unique = false, nullable = false)
	private Date createdDate;
	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
	@JsonIgnoreProperties({"username", "nickname","avatarUrl", "createdDate", "email", "password", "roles"}) // Ігноруємо решту полів User
	private User user;

	@ManyToOne
	@JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)
	@JsonManagedReference  // Серіалізується з історії
	private Category category;

	@OneToMany(mappedBy = "generatedStory", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonBackReference  // не Серіалізується з історії
	private Set<Comment> comments;

	@OneToMany(mappedBy = "generatedStory", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonBackReference  // Серіалізується з історії
	private Set<Like> likes;

	// Обчислювальне поле для кількості лайків
	@Transient  // Це вказує, що поле не буде збережено в базі даних
	@JsonProperty("likesCount")
	public int getLikesCount() {
		return likes != null ? likes.size() : 0;
	}

}
