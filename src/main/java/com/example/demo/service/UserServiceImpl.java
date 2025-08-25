package com.example.demo.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.config.CustomPasswordEncoder;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserResponseDTO;
import com.example.demo.dto.UserUpdateDTO;
import com.example.demo.dto.UserNameDTO;
import com.example.demo.entity.User;
import com.example.demo.repo.UserRepo;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepo repo;

	@Autowired
	CustomPasswordEncoder customPasswordEncoder;

	@Override
	public boolean registerUser(UserDTO userDTO) {
		try {
			User user = new User();
			user.setActive(userDTO.isActive()); // Use the provided active status
			user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
			user.setEmail(userDTO.getEmail());
			user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
			user.setPassword(customPasswordEncoder.encode(userDTO.getPassword()));
			user.setUsername(userDTO.getUsername());
			
			// Set role - default to USER if not specified or invalid
			String role = "USER"; // Default role
			if (userDTO.getRole() != null && !userDTO.getRole().trim().isEmpty()) {
				String providedRole = userDTO.getRole().trim().toUpperCase();
				if (providedRole.equals("ADMIN") || providedRole.equals("USER")) {
					role = providedRole;
				}
			}
			user.setRole(role);
			
			repo.saveAndFlush(user);
			return true; // User creation successful
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			// Handle duplicate email/username or constraint violation
			return false;
		} catch (org.springframework.dao.DataAccessException e) {
			// Handle other database access errors
			return false;
		} catch (Exception e) {
			// Handle any other unexpected errors
			return false;
		}
	}

	@Override
	public List<UserResponseDTO> getAllUsers() {
		try {
			List<User> users = repo.findAll();
			return users.stream()
					.map(this::convertToUserResponseDTO)
					.collect(Collectors.toList());
		} catch (Exception e) {
			// Log the exception and return empty list or throw custom exception
			throw new RuntimeException("Error fetching users: " + e.getMessage(), e);
		}
	}

	@Override
	public UserResponseDTO updateUser(UserUpdateDTO userUpdateDTO) {
		try {
			if (userUpdateDTO.getId() == null) {
				throw new IllegalArgumentException("User ID is required for update");
			}

			// Find the existing user
			User existingUser = repo.findById(userUpdateDTO.getId())
					.orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userUpdateDTO.getId()));

			// Update fields if provided (null means no change)
			if (userUpdateDTO.getUsername() != null && !userUpdateDTO.getUsername().trim().isEmpty()) {
				// Check if username is already taken by another user
				Optional<User> userWithSameUsername = repo.findByUsername(userUpdateDTO.getUsername());
				if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getId().equals(existingUser.getId())) {
					throw new IllegalArgumentException("Username already exists: " + userUpdateDTO.getUsername());
				}
				existingUser.setUsername(userUpdateDTO.getUsername().trim());
			}

			if (userUpdateDTO.getEmail() != null && !userUpdateDTO.getEmail().trim().isEmpty()) {
				// Check if email is already taken by another user
				Optional<User> userWithSameEmail = repo.findByEmail(userUpdateDTO.getEmail());
				if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(existingUser.getId())) {
					throw new IllegalArgumentException("Email already exists: " + userUpdateDTO.getEmail());
				}
				existingUser.setEmail(userUpdateDTO.getEmail().trim());
			}

			if (userUpdateDTO.getRole() != null && !userUpdateDTO.getRole().trim().isEmpty()) {
				String role = userUpdateDTO.getRole().trim().toUpperCase();
				if (!role.equals("USER") && !role.equals("ADMIN")) {
					throw new IllegalArgumentException("Invalid role. Must be USER or ADMIN");
				}
				existingUser.setRole(role);
			}

			if (userUpdateDTO.getIsActive() != null) {
				existingUser.setActive(userUpdateDTO.getIsActive());
			}

			// Update password if provided
			if (userUpdateDTO.getPassword() != null && !userUpdateDTO.getPassword().trim().isEmpty()) {
				existingUser.setPassword(customPasswordEncoder.encode(userUpdateDTO.getPassword()));
			}

			// Update timestamp
			existingUser.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

			// Save the updated user
			User savedUser = repo.saveAndFlush(existingUser);

			// Return the updated user as DTO
			return convertToUserResponseDTO(savedUser);

		} catch (IllegalArgumentException e) {
			// Re-throw validation errors
			throw e;
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			// Handle database constraint violations
			throw new RuntimeException("Data integrity violation: " + e.getMessage(), e);
		} catch (org.springframework.dao.DataAccessException e) {
			// Handle other database access errors
			throw new RuntimeException("Database error during user update: " + e.getMessage(), e);
		} catch (Exception e) {
			// Handle any other unexpected errors
			throw new RuntimeException("Unexpected error during user update: " + e.getMessage(), e);
		}
	}

	@Override
	public List<UserNameDTO> getActiveUserNames() {
		try {
			List<User> activeUsers = repo.findByIsActiveTrue();
			return activeUsers.stream()
					.map(this::convertToUserNameDTO)
					.collect(Collectors.toList());
		} catch (Exception e) {
			// Log the exception and return empty list or throw custom exception
			throw new RuntimeException("Error fetching active users: " + e.getMessage(), e);
		}
	}

	@Override
	public List<UserNameDTO> getActiveUserNames(String excludeUsername) {
		try {
			List<User> activeUsers = repo.findByIsActiveTrue();
			return activeUsers.stream()
					.filter(user -> !user.getUsername().equals(excludeUsername))  // Exclude the specified user
					.map(this::convertToUserNameDTO)
					.collect(Collectors.toList());
		} catch (Exception e) {
			// Log the exception and return empty list or throw custom exception
			throw new RuntimeException("Error fetching active users: " + e.getMessage(), e);
		}
	}

	/**
	 * Convert User entity to UserResponseDTO (excluding password)
	 */
	private UserResponseDTO convertToUserResponseDTO(User user) {
		return new UserResponseDTO(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getRole(),
				user.isActive(),
				user.getCreatedAt(),
				user.getUpdatedAt()
		);
	}

	/**
	 * Convert User entity to UserNameDTO (only id and username)
	 */
	private UserNameDTO convertToUserNameDTO(User user) {
		return new UserNameDTO(
				user.getId(),
				user.getUsername()
		);
	}
}
