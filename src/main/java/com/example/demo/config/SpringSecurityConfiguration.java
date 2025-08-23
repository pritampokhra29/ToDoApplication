package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.example.demo.service.CustomUserDetailsService;

@Configuration
public class SpringSecurityConfiguration {

	@Autowired
	private CustomUserDetailsService customUserDetailsService;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/auth/login").permitAll() // Allow login endpoint
						.requestMatchers("/auth/register").hasRole("ADMIN") // Restrict register endpoint to ADMIN role
						.anyRequest().authenticated())
				.userDetailsService(customUserDetailsService).httpBasic(httpBasic -> {
				}); // disables default login page, uses HTTP Basic
		return http.build();
	}

}
