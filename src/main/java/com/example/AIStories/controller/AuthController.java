	package com.example.AIStories.controller;

import com.example.AIStories.dto.*;
import com.example.AIStories.entity.User;
import com.example.AIStories.exception.*;
import com.example.AIStories.service.GoogleOAuthService;
import com.example.AIStories.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	private final UserService userService;
	private final GoogleOAuthService googleOAuthService;
	// 🔹 1. Реєстрація нового користувача
	@PostMapping("/register")
	public ResponseEntity<User> registerUser(@RequestBody UserDto user) {
		// Спроба зареєструвати користувача без try-catch
		User newUser = userService.registerUser(user.getEmail(), user.getPassword());
		return ResponseEntity.ok(newUser);
	}


	// 🔹 2. Реєстрація користувача через Google
	@PostMapping("/login/google")
	public ResponseEntity<AuthResponse> authenticateUserWithGoogle(@RequestBody GoogleCodeDTO googleCode) {
		GoogleUserDTO user= GoogleOAuthService.getUserInfo(googleCode.getCode());
		String jwtToken = userService.authenticateUserWithGoogle(user);
		return ResponseEntity.ok(new AuthResponse(jwtToken));
	}

	// 🔹 3. Авторизація користувача (отримання JWT токена)
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> authenticateUser(@RequestBody AuthRequest authRequest) {
		String token = userService.authenticateUser(authRequest.getEmail(), authRequest.getPassword());
		return ResponseEntity.ok(new AuthResponse(token));
	}

	// 🔹 4. Отримання даних поточного користувача (потрібен JWT токен)
	@GetMapping("/me")
	public ResponseEntity<User> getCurrentUser() {
		User currentUser = userService.getCurrentUser();
		return ResponseEntity.ok(currentUser);
	}

	@GetMapping("/{userId}")
	public ResponseEntity<UserDto> getUser(@PathVariable Long userId) {
		User user = userService.getUserById(userId);
		UserDto userDto=new UserDto(user.getEmail(),user.getNickname(),"secret",user.getAvatarUrl());
		return ResponseEntity.ok(userDto);
	}

	// 🔹 5. Редагування користувача (зміна ніку, аватарки)
	@PreAuthorize("hasAuthority('ADMIN') or #userId == authentication.principal.getUserId()")
	@PutMapping("/{userId}")
	public ResponseEntity<User> updateUser(@PathVariable Long userId, @RequestBody UserUpdateDTO updatedUser) {
		//System.out.println("Principal: " + principal);
		User updated = userService.updateUser(userId, updatedUser);
		return ResponseEntity.ok(updated);
	}

	// 🔹 6. Зміна пароля
	@PreAuthorize("hasAuthority('ADMIN') or #userId == authentication.principal.getUserId()")
	@PutMapping("/{userId}/password")
	public ResponseEntity<Map<String, String>> changePassword(@PathVariable Long userId,
	                                             @RequestBody PasswordChangeDto passwordChangeDto) {
		userService.changePassword(userId, passwordChangeDto.getOldPassword(), passwordChangeDto.getNewPassword());
		Map<String, String> response = new HashMap<>();
		response.put("message", "Пароль змінено успішно!");
		return ResponseEntity.ok().body(response);

	}

	// 🔹 7. Видалення користувача
	@PreAuthorize("hasAuthority('ADMIN') or #userId == authentication.principal.getUserId()")
	@DeleteMapping("/{userId}")
	public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
		userService.deleteUser(userId);
		Map<String, String> response = new HashMap<>();
		response.put("message", "Користувача видалено успішно");
		return ResponseEntity.ok().body(response);
	}

	// 🔹 8. Зміна ролей користувача
	@PreAuthorize("hasAuthority('ADMIN')")
	@PatchMapping("/{userId}/roles")
	public ResponseEntity<User> updateRoles(@PathVariable Long userId, @RequestBody Set<String> newRoleNames) {
		User updatedUser = userService.updateRoles(userId, newRoleNames);
		return ResponseEntity.ok(updatedUser);
	}

	// 🔹 9. Ініціалізація скидання пароля
	@PostMapping("/reset-password")
	public ResponseEntity<Map<String, String>> initiatePasswordReset(@RequestBody ResetPasswordDTO resetPasswordDTO) {
		String message = userService.initiatePasswordReset(resetPasswordDTO.getEmail());
		Map<String, String> response = new HashMap<>();
		response.put("message", message);
		return ResponseEntity.ok().body(response);
	}

	// 🔹 10. Скидання пароля за токеном
	@PostMapping("/reset-password/{token}")
	public ResponseEntity<Map<String, String>> resetPassword(@PathVariable String token, @RequestBody NewPasswordDTO newPasswordDTO) {
		String message = userService.resetPassword(token, newPasswordDTO.getNewPassword());
		Map<String, String> response = new HashMap<>();
		response.put("message", message);
		return ResponseEntity.ok().body(response);
	}
	// Зміни в AuthController для обробки нових ексепшенів


//	----------------------------------------------------------------
//			ВИКЛЮЧЕННЯ
//	----------------------------------------------------------------
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<Object> handleUserNotFound(UserNotFoundException e) {
		Map<String, String> response = new HashMap<>();
		response.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(IncorrectPasswordException.class)
	public ResponseEntity<Object> handleIncorrectPassword(IncorrectPasswordException e) {
		Map<String, String> response = new HashMap<>();
		response.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(RoleNotFoundException.class)
	public ResponseEntity<Object> handleRoleNotFound(RoleNotFoundException e) {
		Map<String, String> response = new HashMap<>();
		response.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(TokenExpiredException.class)
	public ResponseEntity<Object> handleTokenExpired(TokenExpiredException e) {
		Map<String, String> response = new HashMap<>();
		response.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}
	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<Object> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
		Map<String, String> response = new HashMap<>();
		response.put("message", "Такий email вже існує");
		return ResponseEntity.badRequest().body(response);
	}
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleGeneralError(Exception e) {
		Map<String, String> response = new HashMap<>();
		response.put("message", "Щось пішло не так!");
		response.put("error", e.getMessage());
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

}
