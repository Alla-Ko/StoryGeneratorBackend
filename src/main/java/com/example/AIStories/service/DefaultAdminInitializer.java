package com.example.AIStories.service;

import com.example.AIStories.entity.Role;
import com.example.AIStories.entity.User;
import com.example.AIStories.repository.RoleRepository;
import com.example.AIStories.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashSet;

@Service
public class DefaultAdminInitializer {

	private static final String ADMIN_EMAIL = "admin@example.com";
	private static final String DELETED_USER_EMAIL = "deleted@example.com"; // Email для DeletedUser
	private static final String DELETED_USER_NICKNAME = "DeletedUser";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@PostConstruct
	public void init() {
		createAdminIfNotExists();
		createDeletedUserIfNotExists();
	}

	private void createAdminIfNotExists() {
		if (userRepository.findByUsername(ADMIN_EMAIL).isEmpty()) {
			Role adminRole = roleRepository.findByName("ADMIN");
			if (adminRole == null) {
				adminRole = new Role();
				adminRole.setName("ADMIN");
				roleRepository.save(adminRole);
				System.out.println("ROLE_ADMIN created!");
			}

			User admin = new User();
			admin.setUsername(ADMIN_EMAIL);
			admin.setNickname("admin");
			admin.setEmail(ADMIN_EMAIL);
			admin.setPassword(passwordEncoder.encode("admin123"));
			admin.setCreatedDate(new java.util.Date());
			admin.setRoles(new HashSet<>());
			admin.getRoles().add(adminRole);

			userRepository.save(admin);
			System.out.println("Default admin created!");
		}
	}

	private void createDeletedUserIfNotExists() {
		if (userRepository.findByUsername(DELETED_USER_EMAIL).isEmpty()) {
			User deletedUser = new User();
			deletedUser.setUsername(DELETED_USER_EMAIL);
			deletedUser.setNickname(DELETED_USER_NICKNAME);
			deletedUser.setEmail(DELETED_USER_EMAIL);
			deletedUser.setPassword(passwordEncoder.encode("deleted")); // Пароль не важливий, бо він не використовується
			deletedUser.setCreatedDate(new java.util.Date());
			deletedUser.setRoles(new HashSet<>()); // DeletedUser не має жодних ролей

			userRepository.save(deletedUser);
			System.out.println("DeletedUser created!");
		}
	}
}
