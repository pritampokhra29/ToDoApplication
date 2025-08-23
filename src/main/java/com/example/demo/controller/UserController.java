package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.UserDTO;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/auth")
public class UserController {

	@Autowired
	private UserService userService;

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody UserDTO userDTO) {
		boolean isRegistered = userService.registerUser(userDTO);
		if (isRegistered) {
			return ResponseEntity.status(HttpStatus.CREATED).body("User registered: " + userDTO.getUsername());
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed for user: " + userDTO.getUsername());
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody UserDTO userDTO) {
		try {
			// This will trigger the UserDetailsService to load and authenticate the user
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
				return ResponseEntity.ok("Login successful for user: " + auth.getName());
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: " + e.getMessage());
		}
	}

	@PostMapping("/status")
	public ResponseEntity<?> status() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
			return ResponseEntity.ok("Authenticated as: " + auth.getName());
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
		}
	}
}