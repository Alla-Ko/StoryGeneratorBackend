package com.example.AIStories.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableMethodSecurity
@Configuration
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final UserDetailsService userDetailsService;
	private final CustomAuthenticationEntryPoint authenticationEntryPoint;

	// Впроваджуємо залежності через конструктор
	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, UserDetailsService userDetailsService, CustomAuthenticationEntryPoint authenticationEntryPoint) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.userDetailsService = userDetailsService;
		this.authenticationEntryPoint = authenticationEntryPoint;
	}
	// Налаштування CORS для Spring Security
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.addAllowedOrigin("http://localhost:4200");  // Дозволити доступ з фронтенду
		corsConfig.addAllowedMethod("*");  // Дозволити всі HTTP методи (GET, POST, PUT, DELETE, OPTIONS)
		corsConfig.addAllowedHeader("*");  // Дозволити всі заголовки
		corsConfig.setAllowCredentials(true);  // Дозволити передачу куків
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);  // Застосувати CORS до всіх маршрутів
		return source;
	}
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/auth/register", "/auth/login","/auth/login/google", "/auth/reset-password","/auth/reset-password/*").permitAll()
						.requestMatchers("/error","/swagger-ui/**", "/swagger-ui/index.html", "/v3/api-docs/**", "/swagger-ui.html").permitAll()  // Allow access to Swagger
						.requestMatchers("/generated-stories","/generated-stories/**","/api/comments/story/**").permitAll()
						.anyRequest().authenticated()  // Require authentication for all other requests
				)
				.exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
				.headers(headers -> headers
						.contentSecurityPolicy(csp -> csp
								.policyDirectives("frame-ancestors 'self'") // Allow only same-origin framing (necessary for Swagger UI)
						)
				)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}


	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);  // Spring автоматично передасть потрібну реалізацію
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}
}
