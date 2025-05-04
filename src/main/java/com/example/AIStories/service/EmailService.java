package com.example.AIStories.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;
@Service
public class EmailService {

	@Value("${mail.username}")
	private String username;  // Отримуємо значення з конфігурації

	@Value("${mail.app-key}")  // Ключ додатка
	private String appKey;  // Отримуємо значення з конфігурації

	// Надсилаємо лист для скидання паролю
	public void sendPasswordResetEmail(String email, String token) {
		String resetLink = "http://localhost:4200//reset-password/" + token;

		String subject = "Скидання паролю";
		String body = "Для скидання паролю перейдіть за посиланням: " + resetLink;

		try {
			sendEmail(email, subject, body);
		} catch (MessagingException e) {
			throw new RuntimeException("Не вдалося надіслати лист", e);
		}
	}

	private void sendEmail(String to, String subject, String bodyText) throws MessagingException {
		// Налаштування SMTP для Gmail
		Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.gmail.com");
		prop.put("mail.smtp.port", "587");  // Порт для TLS
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.starttls.enable", "true");

		// Створення сесії з аутентифікацією
		Session session = Session.getInstance(prop, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, appKey); // Використовуємо ім'я користувача та пароль додатка
			}
		});

		// Створення листа
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(username)); // Відправник
		message.setRecipients(
				Message.RecipientType.TO,
				InternetAddress.parse(to) // Одержувач
		);
		message.setSubject(subject); // Тема
		message.setText(bodyText); // Текст листа

		// Відправка листа
		Transport.send(message);

		System.out.println("Лист надіслано успішно!");
	}

}
