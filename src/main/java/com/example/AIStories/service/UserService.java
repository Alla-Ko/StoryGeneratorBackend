package com.example.AIStories.service;

import com.example.AIStories.dto.GoogleUserDTO;
import com.example.AIStories.dto.UserUpdateDTO;

import com.example.AIStories.entity.CommentLike;
import com.example.AIStories.entity.Role;
import com.example.AIStories.entity.User;
import com.example.AIStories.exception.EmailAlreadyExistsException;
import com.example.AIStories.exception.IncorrectPasswordException;
import com.example.AIStories.exception.RoleNotFoundException;
import com.example.AIStories.exception.UserNotFoundException;
import com.example.AIStories.repository.RoleRepository;
import com.example.AIStories.repository.UserRepository;
import com.example.AIStories.security.JwtService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final EmailService emailService;
	private final CommentService commentService;
	private final CommentLikeService commentLikeService;
	private final LikeService likeService;

	// 🔹 1. Реєстрація нового користувача по email і паролю
	public User registerUser(String email, String password) {
		if (userRepository.findByEmail(email).isPresent()) {
			throw new EmailAlreadyExistsException("Користувач з такою поштою вже існує!");
		}

		User newUser = new User();
		newUser.setEmail(email);
		newUser.setPassword(passwordEncoder.encode(password));
		newUser.setNickname(generateUniqueNickname(email));
		newUser.setUsername(email);
		newUser.setCreatedDate(new Date());

		// Додати роль "ROLE_USER" за замовчуванням
		// Перевіряємо, чи існує роль "ROLE_USER" в БД, і якщо її немає, додаємо
		// Перевіряємо, чи існує роль "ROLE_USER" в БД, і якщо її немає, додаємо
		Role userRole = roleRepository.findByName("USER");

		if (userRole == null) {
			// Якщо роль не існує, створюємо її
			userRole = new Role();
			userRole.setName("USER"); // задаємо роль
			roleRepository.save(userRole); // зберігаємо роль в базі
		}

		// Додаємо роль до користувача
		if (newUser.getRoles() == null) {
			newUser.setRoles(new HashSet<>());
		}
		newUser.getRoles().add(userRole);

		return userRepository.save(newUser);
	}
	public String authenticateUserWithGoogle(GoogleUserDTO googleUser) {
		// Отримуємо інформацію про користувача з Google
		String email = googleUser.getEmail();
		String imageUrl = googleUser.getPicture();

		User user=registerGoogleUser( email,  imageUrl);
		return jwtService.generateJwtToken(user);
	}



	// 🔹 2. Реєстрація нового користувача через Google (якщо його ще немає в БД)
	public User registerGoogleUser(String email, String avatarUrl) {
		return userRepository.findByEmail(email)
				.orElseGet(() -> {
					User newUser = new User();
					newUser.setEmail(email);
					newUser.setAvatarUrl(avatarUrl);
					newUser.setUsername(email);
					newUser.setCreatedDate(new Date());
					newUser.setNickname(generateUniqueNickname(email));
					// Додати роль "ROLE_USER" за замовчуванням
					// Перевіряємо, чи існує роль "ROLE_USER" в БД, і якщо її немає, додаємо
					Role userRole = roleRepository.findByName("USER");

					if (userRole == null) {
						// Якщо роль не існує, створюємо її
						userRole = new Role();
						userRole.setName("USER"); // задаємо роль
						roleRepository.save(userRole); // зберігаємо роль в базі
					}

					// Додаємо роль до користувача
					if (newUser.getRoles() == null) {
						newUser.setRoles(new HashSet<>());
					}
					newUser.getRoles().add(userRole);

					return userRepository.save(newUser);
				});
	}


	// 🔹 3. Повернення JWT токена після авторизації
	public String authenticateUser(String email, String password) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("Користувача не знайдено!"));

		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new IncorrectPasswordException("Неправильна комбінація електронна пошта/пароль!");
		}

		return jwtService.generateJwtToken(user);
	}

	// 🔹 4. Отримання користувача з токена (Bearer-токен)
	public User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new UserNotFoundException("Користувач не авторизований!");
		}

		String email = authentication.getName();
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("Користувач не знайдений!"));
	}
public boolean isCurrentUserAdmin(){
	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	if (authentication == null || !authentication.isAuthenticated()) {
		throw new UserNotFoundException("Користувач не авторизований!");
	}
	String email = authentication.getName();
	User currentUser= userRepository.findByEmail(email)
			.orElseThrow(() -> new UserNotFoundException("Користувач не знайдений!"));
	return currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"));

}
	// 🔹 5. Редагування користувача

	public User updateUser(Long userId, UserUpdateDTO updatedUser) {
		User existingUser = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("Користувач не знайдений!"));



		if (updatedUser.getNickname() != null) {
			existingUser.setNickname(updatedUser.getNickname());
		}
		if (updatedUser.getAvatarUrl() != null) {
			existingUser.setAvatarUrl(updatedUser.getAvatarUrl());
		}

		return userRepository.save(existingUser);
	}


	public void changePassword(Long userId, String oldPassword, String newPassword) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("Користувач не знайдений!"));

		// Перевіряємо, чи правильний старий пароль
		if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
			throw new IncorrectPasswordException("Неправильний старий пароль!");
		}

		// Хешуємо новий пароль перед збереженням
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
	}



	// 🔹 6. Видалення користувача

		@Transactional
		public void deleteUser(Long userId) {
			User user = userRepository.findById(userId)
					.orElseThrow(() -> new UserNotFoundException("Користувача не знайдено"));

			// Оновлення коментарів перед видаленням користувача
			commentLikeService.deleteLikesOnOtherUsersStories(userId);
			likeService.deleteLikesOnOtherUsersStories(userId);

			commentService.updateCommentsSetUserToDeleted(userId);

			// Видалення користувача
			userRepository.delete(user);
		}


	// 🔹 Генерація унікального username з email
	private String generateUniqueNickname(String email) {
		String baseNickname = email.split("@")[0];
		String nickname = baseNickname;
		int counter = 1;

		while (userRepository.findByNickname(nickname).isPresent()) {
			nickname = baseNickname + "_" + counter;  // можна додавати інші варіанти
			counter++;
		}
		return nickname;
	}

	public User updateRoles(Long userId, Set<String> newRoleNames) {
		// Знайдемо користувача за його ID
		User existingUser = userRepository.findById(userId)
				.orElseThrow(() -> new UsernameNotFoundException("Користувач не знайдений!"));

		// Отримаємо ролі за їхніми іменами
		Set<Role> newRoles = newRoleNames.stream()
				.map(roleName -> {
					Role role = roleRepository.findByName(roleName); // знайдемо роль
					if (role == null) {
						throw new RoleNotFoundException("Роль " + roleName + " не знайдена!");
					}
					return role;
				})
				.collect(Collectors.toSet());

		// Оновлюємо ролі користувача
		existingUser.setRoles(newRoles);

		return userRepository.save(existingUser);
	}


	public String initiatePasswordReset(String email) {
		// Знаходимо користувача по email
		User user = userRepository.findByUsername(email)
				.orElseThrow(() -> new UserNotFoundException("Користувача з такою поштою не знайдено!"));

		// Генеруємо токен для скидання пароля
		String token = jwtService.generatePasswordResetToken(user);

		// Тут ви можете додати виклик для відправки електронного листа
		// Замість цього викликаємо сервіс для надсилання листів
		emailService.sendPasswordResetEmail(user.getEmail(), token);

		return "Якщо така пошта існує в базі даних, лист для скидання паролю надіслано!";
	}
	public String resetPassword(String token, String newPassword) {
		// Перевіряємо токен
		User user = jwtService.verifyPasswordResetToken(token);

		// Хешуємо новий пароль
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		return "Пароль успішно змінено!";
	}
	public User getUserById(Long userId) {
		return userRepository.findById(userId).orElse(null);
	}

	public Optional<User> findByUsername(String mail) {
		return userRepository.findByUsername(mail);
	}
}
