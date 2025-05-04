package com.example.AIStories.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.Usage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AIResponse {
	private String id;
	private String object;
	private long created;
	private String model;
	private List<Choice> choices;
	private Usage usage;
	@JsonProperty("service_tier")
	private String serviceTier;
	@JsonProperty("system_fingerprint")
	private String systemFingerprint;

	@Getter
	@Setter
	@AllArgsConstructor
	static class Choice {
		private int index;
		private Message message;
		private Object logprobs;
		@JsonProperty("finish_reason")
		private String finishReason;
	}

	@Getter
	@Setter
	@AllArgsConstructor
	static class Message {
		private String role;
		private String content;
		private String refusal;
	}

	@Getter
	@Setter
	@AllArgsConstructor
	static class Usage {
		@JsonProperty("prompt_tokens")
		private int promptTokens;
		@JsonProperty("completion_tokens")
		private int completionTokens;
		@JsonProperty("total_tokens")
		private int totalTokens;
		@JsonProperty("prompt_tokens_details")
		private TokenDetails promptTokensDetails;
		@JsonProperty("completion_tokens_details")
		private TokenDetails completionTokensDetails;
	}

	@Getter
	@Setter
	@AllArgsConstructor
	static class TokenDetails {
		@JsonProperty("cached_tokens")
		private int cachedTokens;

		@JsonProperty("audio_tokens")
		private int audioTokens;

		@JsonProperty("reasoning_tokens")
		private int reasoningTokens;

		@JsonProperty("accepted_prediction_tokens")
		private int acceptedPredictionTokens;

		@JsonProperty("rejected_prediction_tokens")
		private int rejectedPredictionTokens;
	}
}

