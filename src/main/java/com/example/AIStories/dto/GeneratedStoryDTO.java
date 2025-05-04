package com.example.AIStories.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneratedStoryDTO {
	private String prompt;
	private String content;
	private String imageUrl;
	private String title;
	private Boolean isPublic=false;
	private Long categoryId;
}
