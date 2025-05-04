package com.example.AIStories.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String email; // Email обов'язковий та унікальний!

	@Column(unique = true, nullable = false)
	private String username; // Тепер обов’язковий, але генерується автоматично
	@Column(unique = true, nullable = false)
	private String nickname;

	@Column(nullable = true)
	private String password;
	@Column(unique = false, nullable = true)
	private String avatarUrl;
	@Column(unique = false, nullable = false)
	private Date createdDate;
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(

			name = "user_roles",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id")
	)
	private Set<Role> roles;

	// Власні історії користувача
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonBackReference // уникнення зациклення
	private Set<GeneratedStory> ownStories;

	@OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
	@JsonBackReference
	private Set<Like> likes;
	@OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
	@JsonBackReference
	private Set<CommentLike> commentLikes;

}
