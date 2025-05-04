package com.example.AIStories.dto;

import com.example.AIStories.entity.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
public class UserUpdateDTO {


	private String nickname;
	private String avatarUrl;

	public UserUpdateDTO(String nickname, String avatarUrl) {
		this.nickname = nickname;
		this.avatarUrl = avatarUrl;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
}
