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
@RequestMapping("/User/auth")
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