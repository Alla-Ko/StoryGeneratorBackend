package com.example.AIStories.dto;

import com.example.AIStories.entity.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Getter
@Setter

public class ResetPasswordDTO {
	private String email;

	public String getEmail() {
		return email;
	}

	public ResetPasswordDTO(String email) {
		this.email = email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
