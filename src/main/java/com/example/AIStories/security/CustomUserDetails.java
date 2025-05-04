package com.example.AIStories.security;

import com.example.AIStories.entity.Role;
import com.example.AIStories.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {
	private final User user;  // це твій користувач з бази даних
	@Getter
	private final Long userId;

	public CustomUserDetails(User user) {
		this.user = user;
		this.userId = user.getId();  // отримаємо ID з твоєї бази даних
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// Повертаємо ролі
		Set<Role> roles = user.getRoles();

		List<GrantedAuthority> authorities = roles.stream()
				.map(role -> new SimpleGrantedAuthority(role.getName()))
				.collect(Collectors.toList());

		return authorities;
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

}
