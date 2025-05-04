package com.example.AIStories.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GenerateStoryDTO {
	private String prompt;

	public String getPrompt() {
		return this.prompt;
	}
}
