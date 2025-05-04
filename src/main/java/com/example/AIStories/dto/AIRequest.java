package com.example.AIStories.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AIRequest {
	private String model="gpt-4o-mini";
	private Boolean store=true;
	private List<Message> messages;
	@JsonProperty("max_tokens")
	private Integer maxTokens=300;
	public AIRequest(String content){
		this.messages=List.of(new Message("user", content));
	}
	public AIRequest(String content, Integer maxTokens){
		this.messages=List.of(new Message("user", content));
		this.setMaxTokens(maxTokens);
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class Message {
		private String role="user";
		private String content;

	}


}
