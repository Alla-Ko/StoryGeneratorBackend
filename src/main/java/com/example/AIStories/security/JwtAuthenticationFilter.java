package com.example.AIStories.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;

	public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
	                                HttpServletResponse response,
	                                FilterChain filterChain) throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response); // Якщо заголовка немає або він некоректний, пропускаємо запит далі
			return;
		}

		String token = authHeader.substring(7); // Видаляємо "Bearer " з початку токена
		try {
			String email = jwtService.extractEmail(token); // Отримуємо email із токена

			if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

				// Якщо токен валідний
				if (jwtService.validateToken(token, userDetails)) {
					UsernamePasswordAuthenticationToken authToken =
							new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					SecurityContextHolder.getContext().setAuthentication(authToken);
				} else {
					// Якщо токен не валідний, можна відразу відправити помилку
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
					response.setContentType("application/json");
					response.setCharacterEncoding("UTF-8");

					String jsonResponse = "{\"message\": \"Неавторизовано. JWT токен не валідний.\"}";
					response.getWriter().write(jsonResponse);
					return; // Зупинити обробку фільтру
				}
			}
		}
		catch (UsernameNotFoundException e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");

			String jsonResponse = "{\"message\": \"Токен не валідний. Користувача не знайдено\"}";
			response.getWriter().write(jsonResponse);
			return;

		}

		catch (RuntimeException e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");

			String jsonResponse = "{\"message\": \"JWT токен не валідний.\"}";
			response.getWriter().write(jsonResponse);
			return;

		}

		filterChain.doFilter(request, response); // Продовжити ланцюжок фільтрів
	}
}
