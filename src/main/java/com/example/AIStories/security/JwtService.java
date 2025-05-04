package com.example.AIStories.security;

import com.example.AIStories.entity.Role;
import com.example.AIStories.entity.User;
import com.example.AIStories.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtService {
	private final String SECRET_KEY = "my-very-secret-key-should-be-long-enough";
	private final UserRepository userRepository;

	public JwtService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}


	// Генерація токену для скидання пароля
	public String generatePasswordResetToken(User user) {
		return Jwts.builder()
				.setSubject(user.getEmail()) // Використовуємо email для ідентифікації користувача
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 15)) // Термін дії токену 15 хвилин
				.signWith(getSigningKey(), SignatureAlgorithm.HS256) // Підписуємо токен
				.compact();
	}

	// Перевірка токену для скидання пароля
	public User verifyPasswordResetToken(String token) throws JwtException, RuntimeException {
		String email = extractEmail(token);
		// Перевірка дії токену
		if (isTokenExpired(token)) {
			throw new RuntimeException("Токен для скидання паролю прострочений.");
		}

		// Повертаємо користувача за email, якщо токен валідний
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("Користувача з такою поштою не знайдено!"));

	}
	public String extractEmail(String token) {
		try {
			return Jwts.parser().setSigningKey(getSigningKey()).build()
					.parseClaimsJws(token).getBody().getSubject();
		}
		catch (JwtException e) {
            throw new RuntimeException("Невалідний JWT token.");
        }
	}

	public boolean validateToken(String token, UserDetails userDetails) {
		final String email = extractEmail(token);
		return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaims(token).getExpiration();
	}

	private Claims extractClaims(String token) {
		return Jwts.parser().setSigningKey(getSigningKey()).build()
				.parseClaimsJws(token).getBody();
	}

	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
	}
	public Long extractUserId(String token) {
		return (Long) Jwts.parser()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody()
				.get("userId");  // Отримуємо userId з токену
	}

	public String generateJwtToken(User user) {
		List<String> roles = user.getRoles().stream()
				.map(role -> role.getName()) // Перетворюємо кожну роль на її ім'я
				.collect(Collectors.toList());
		return Jwts.builder()
				.setSubject(user.getEmail())
				.setIssuedAt(new Date())
				.claim("userId", user.getId())
				.claim("roles", roles)
				.setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
	}
	public boolean isAdmin(String token) {
		Claims claims = extractClaims(token);
		List<String> roles = claims.get("roles", List.class); // Витягуємо список імен ролей з токену

		// Перевіряємо, чи є роль "ADMIN" в списку ролей
		return roles != null && roles.contains("ADMIN");
	}



}
