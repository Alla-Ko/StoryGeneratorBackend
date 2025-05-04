package com.example.AIStories.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewPasswordDTO {
	String newPassword;

	public NewPasswordDTO(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
}
