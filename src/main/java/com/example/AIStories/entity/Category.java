package com.example.AIStories.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	// Зв'язок з історіями (один до багатьох)
	@OneToMany(mappedBy = "category")
	@JsonBackReference  // Ігнорується при серіалізації з категорії
	@JsonIgnore
	private Set<GeneratedStory> generatedStories;
}
