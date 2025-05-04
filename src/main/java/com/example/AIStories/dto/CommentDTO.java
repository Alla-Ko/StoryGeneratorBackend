package com.example.AIStories.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDTO {


	private String content;
	private Long generatedStoryId;
	private Long parentCommentId;

}
