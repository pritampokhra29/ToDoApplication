package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomPasswordEncoder implements PasswordEncoder {
	@Value("${security.password.pepper}")
	private String pepper;

	@Override
	public String encode(CharSequence rawPassword) {
		// Add pepper to password before encoding
		return (rawPassword.toString() + pepper);
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		// Add pepper to raw password before matching
		return (rawPassword.toString() + pepper).equals(encodedPassword);
	}
}
