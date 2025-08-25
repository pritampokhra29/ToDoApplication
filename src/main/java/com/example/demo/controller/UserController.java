package com.example.demo.controller;

import com.example.demo.config.JwtProperties;
import com.example.demo.dto.JwtAuthenticationResponse;
import com.example.demo.dto.LogoutRequest;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.dto.UserResponseDTO;
import com.example.demo.dto.UserUpdateDTO;
import com.example.demo.dto.UserNameDTO;
import com.example.demo.service.AuthenticationService;
import com.example.demo.service.TokenBlacklistService;
import com.example.demo.util.CustomLogger;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.UserDTO;
import com.example.demo.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class UserController {

	private static final CustomLogger logger = CustomLogger.getLogger(UserController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private JwtProperties jwtProperties;

	@Autowired
	private TokenBlacklistService tokenBlacklistService;

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody UserDTO userDTO) {
		// Extract current authentication to check roles
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		// Debug logging
		logger.logSecurityEvent("REGISTRATION_DEBUG", "system", 
				"Registration attempt - Auth: " + (auth != null ? auth.getClass().getSimpleName() : "null") + 
				", Authenticated: " + (auth != null ? auth.isAuthenticated() : "N/A") + 
				", Principal: " + (auth != null ? auth.getName() : "null") + 
				", Authorities: " + (auth != null ? auth.getAuthorities().toString() : "null"), "LOW");
		
		if (auth == null || !auth.isAuthenticated()) {
			logger.logSecurityEvent("REGISTRATION_UNAUTHORIZED", "anonymous", 
					"Attempted registration without authentication", "HIGH");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
		}
		
		// Check if user has ADMIN role
		boolean hasAdminRole = auth.getAuthorities().stream()
				.anyMatch(a -> {
					logger.logSecurityEvent("REGISTRATION_ROLE_CHECK", auth.getName(), 
							"Checking authority: " + a.getAuthority(), "LOW");
					System.out.println("DEBUG: Checking authority: " + a.getAuthority());
					return a.getAuthority().equals("ROLE_ADMIN");
				});
		
		// TEMPORARY: Also print all authorities for debugging
		System.out.println("DEBUG: All authorities: " + auth.getAuthorities());
		System.out.println("DEBUG: Has admin role: " + hasAdminRole);
		
		logger.logSecurityEvent("REGISTRATION_ROLE_RESULT", auth.getName(), 
				"Has admin role: " + hasAdminRole, "LOW");
		
		if (!hasAdminRole) {
			logger.logSecurityEvent("REGISTRATION_FORBIDDEN", auth.getName(), 
					"Attempted registration without ADMIN role. Authorities: " + auth.getAuthorities(), "HIGH");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
		}
		
		logger.logUserActivity("admin", "REGISTER_USER", "/auth/register", 
				"Admin attempting to register user: " + userDTO.getUsername() + " with role: " + 
				(userDTO.getRole() != null ? userDTO.getRole() : "USER (default)"));
		
		boolean isRegistered = userService.registerUser(userDTO);
		if (isRegistered) {
			logger.logBusinessOperation("USER_REGISTRATION", "User", userDTO.getUsername(), "CREATE", "SUCCESS");
			
			String roleMessage = userDTO.getRole() != null && !userDTO.getRole().trim().isEmpty() ? 
					userDTO.getRole().trim().toUpperCase() : "USER";
			
			return ResponseEntity.status(HttpStatus.CREATED)
					.body("User registered successfully: " + userDTO.getUsername() + " with role: " + roleMessage);
		} else {
			logger.logBusinessOperation("USER_REGISTRATION", "User", userDTO.getUsername(), "CREATE", "FAILED");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed for user: " + userDTO.getUsername());
		}
	}

	@GetMapping("/users/active")
	public ResponseEntity<?> getActiveUsers() {
		// Extract current authentication - any authenticated user can access this
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null || !auth.isAuthenticated()) {
			logger.logSecurityEvent("ACTIVE_USERS_UNAUTHORIZED", "anonymous", 
					"Attempted to access active users without authentication", "MEDIUM");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
		}
		
		try {
			logger.logUserActivity(auth.getName(), "GET_ACTIVE_USERS", "/auth/users/active", 
					"User retrieving active users for task collaboration");
			
			// Get active users excluding the current logged-in user
			List<UserNameDTO> activeUsers = userService.getActiveUserNames(auth.getName());
			
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("totalActiveUsers", activeUsers.size());
			response.put("users", activeUsers);
			response.put("message", "Active users retrieved successfully (excluding current user)");
			
			logger.logBusinessOperation("USER_ACTIVE_LIST", "User", auth.getName(), "READ", "SUCCESS");
			
			return ResponseEntity.ok(response);
			
		} catch (Exception e) {
			logger.logException("ACTIVE_USERS_ERROR", e, "Error retrieving active users");
			logger.logBusinessOperation("USER_ACTIVE_LIST", "User", auth.getName(), "READ", "FAILED");
			
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "Failed to retrieve active users");
			errorResponse.put("message", e.getMessage());
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	@GetMapping("/admin/users")
	public ResponseEntity<?> getAllUsers() {
		// Extract current authentication to check roles
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null || !auth.isAuthenticated()) {
			logger.logSecurityEvent("ADMIN_USERS_UNAUTHORIZED", "anonymous", 
					"Attempted to access user list without authentication", "HIGH");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
		}
		
		// Check if user has ADMIN role
		boolean hasAdminRole = auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		
		if (!hasAdminRole) {
			logger.logSecurityEvent("ADMIN_USERS_FORBIDDEN", auth.getName(), 
					"Attempted to access user list without ADMIN role. Authorities: " + auth.getAuthorities(), "HIGH");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
		}
		
		try {
			logger.logUserActivity(auth.getName(), "GET_ALL_USERS", "/auth/admin/users", 
					"Admin retrieving all users list");
			
			List<UserResponseDTO> users = userService.getAllUsers();
			
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("totalUsers", users.size());
			response.put("users", users);
			response.put("message", "Users retrieved successfully");
			
			logger.logBusinessOperation("ADMIN_USER_LIST", "Admin", auth.getName(), "READ", "SUCCESS");
			logger.logSecurityEvent("ADMIN_ACCESS_GRANTED", auth.getName(), 
					"Admin successfully accessed user list", "LOW");
			
			return ResponseEntity.ok(response);
			
		} catch (Exception e) {
			logger.logException("ADMIN_USERS_ERROR", e, "Error retrieving users list");
			logger.logBusinessOperation("ADMIN_USER_LIST", "Admin", auth.getName(), "READ", "FAILED");
			
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "Failed to retrieve users");
			errorResponse.put("message", e.getMessage());
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	@PostMapping("/admin/users/{id}")
	public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO userUpdateDTO) {
		// Extract current authentication to check roles
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null || !auth.isAuthenticated()) {
			logger.logSecurityEvent("ADMIN_USER_UPDATE_UNAUTHORIZED", "anonymous", 
					"Attempted to update user without authentication", "HIGH");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
		}
		
		// Check if user has ADMIN role
		boolean hasAdminRole = auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		
		if (!hasAdminRole) {
			logger.logSecurityEvent("ADMIN_USER_UPDATE_FORBIDDEN", auth.getName(), 
					"Attempted to update user without ADMIN role. Authorities: " + auth.getAuthorities(), "HIGH");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
		}
		
		// Validate that user ID is provided
		if (id == null) {
			logger.logUserActivity(auth.getName(), "UPDATE_USER_VALIDATION_FAILED", "/auth/admin/users/" + id, 
					"User update failed: ID is required");
			
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "Validation failed");
			errorResponse.put("message", "User ID is required");
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}
		
		// Set the ID from path parameter to the DTO
		userUpdateDTO.setId(id);
		
		try {
			logger.logUserActivity(auth.getName(), "UPDATE_USER", "/auth/admin/users/" + id, 
					"Admin attempting to update user ID: " + id);
			
			UserResponseDTO updatedUser = userService.updateUser(userUpdateDTO);
			
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("user", updatedUser);
			response.put("message", "User updated successfully");
			
			logger.logBusinessOperation("ADMIN_USER_UPDATE", "User", updatedUser.getUsername(), "UPDATE", "SUCCESS");
			logger.logSecurityEvent("ADMIN_USER_MODIFIED", auth.getName(), 
					"Admin successfully updated user: " + updatedUser.getUsername() + " (ID: " + updatedUser.getId() + ")", "MEDIUM");
			
			return ResponseEntity.ok(response);
			
		} catch (IllegalArgumentException e) {
			// Handle validation errors (user not found, invalid data, etc.)
			logger.logUserActivity(auth.getName(), "UPDATE_USER_VALIDATION_FAILED", "/auth/admin/users/update", 
					"User update validation failed: " + e.getMessage());
			logger.logBusinessOperation("ADMIN_USER_UPDATE", "User", userUpdateDTO.getId().toString(), "UPDATE", "VALIDATION_FAILED");
			
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "Validation failed");
			errorResponse.put("message", e.getMessage());
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
			
		} catch (Exception e) {
			logger.logException("ADMIN_USER_UPDATE_ERROR", e, "Error updating user");
			logger.logBusinessOperation("ADMIN_USER_UPDATE", "User", userUpdateDTO.getId().toString(), "UPDATE", "FAILED");
			
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "Update failed");
			errorResponse.put("message", "An error occurred while updating the user");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody UserDTO userDTO) {
		// Check if JWT is enabled
		if (!jwtProperties.isEnabled()) {
			logger.logSecurityEvent("JWT_DISABLED_LOGIN", userDTO.getUsername(),
					"Login attempt when JWT is disabled", "MEDIUM");
			
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "JWT Authentication is disabled");
			errorResponse.put("message", "Please use Basic Authentication instead");
			errorResponse.put("authMethod", "Basic Auth");
			
			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
		}
		
		try {
			logger.logUserActivity(userDTO.getUsername(), "LOGIN_ATTEMPT", "/auth/login", 
					"User attempting JWT login");

			JwtAuthenticationResponse response = authenticationService.authenticateUser(userDTO);
			
			logger.logBusinessOperation("USER_LOGIN", "User", userDTO.getUsername(), "AUTHENTICATE", "SUCCESS");
			
			return ResponseEntity.ok(response);
			
		} catch (Exception e) {
			logger.logSecurityEvent("LOGIN_FAILED", userDTO.getUsername(), 
					"Login failed: " + e.getMessage(), "MEDIUM");
			logger.logBusinessOperation("USER_LOGIN", "User", userDTO.getUsername(), "AUTHENTICATE", "FAILED");
			
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "Authentication failed");
			errorResponse.put("message", e.getMessage());
			
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		}
	}

	@PostMapping("/refresh")
	public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
		try {
			logger.logSecurityEvent("TOKEN_REFRESH_REQUEST", "unknown", 
					"Refresh token request received", "LOW");

			JwtAuthenticationResponse response = authenticationService.refreshToken(refreshTokenRequest.getRefreshToken());
			
			return ResponseEntity.ok(response);
			
		} catch (Exception e) {
			logger.logSecurityEvent("TOKEN_REFRESH_FAILED", "unknown", 
					"Token refresh failed: " + e.getMessage(), "MEDIUM");
			
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "Token refresh failed");
			errorResponse.put("message", e.getMessage());
			
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		}
	}

	@PostMapping("/validate")
	public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
		try {
			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				String token = authHeader.substring(7);
				boolean isValid = authenticationService.validateToken(token);
				
				if (isValid) {
					String username = authenticationService.getUsernameFromToken(token);
					long remainingValidity = authenticationService.getTokenRemainingValidity(token);
					
					Map<String, Object> response = new HashMap<>();
					response.put("valid", true);
					response.put("username", username);
					response.put("remainingValiditySeconds", remainingValidity);
					
					logger.logUserActivity(username, "TOKEN_VALIDATION", "/auth/validate", 
							"Token validation successful");
					
					return ResponseEntity.ok(response);
				} else {
					Map<String, Object> response = new HashMap<>();
					response.put("valid", false);
					response.put("message", "Invalid or expired token");
					
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
				}
			} else {
				Map<String, Object> response = new HashMap<>();
				response.put("valid", false);
				response.put("message", "Authorization header missing or invalid format");
				
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}
		} catch (Exception e) {
			logger.logException("TOKEN_VALIDATION_ERROR", e, "Error validating token");
			
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "Token validation failed");
			errorResponse.put("message", e.getMessage());
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	@PostMapping("/status")
	public ResponseEntity<?> getAuthStatus() {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
				String username = auth.getName();
				
				Map<String, Object> response = new HashMap<>();
				response.put("authenticated", true);
				response.put("username", username);
				response.put("authorities", auth.getAuthorities());
				
				logger.logUserActivity(username, "STATUS_CHECK", "/auth/status", 
						"Authentication status checked");
				
				return ResponseEntity.ok(response);
			} else {
				Map<String, Object> response = new HashMap<>();
				response.put("authenticated", false);
				response.put("message", "User not authenticated");
				
				return ResponseEntity.ok(response);
			}
		} catch (Exception e) {
			logger.logException("AUTH_STATUS_ERROR", e, "Error checking authentication status");
			
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "Status check failed");
			errorResponse.put("message", e.getMessage());
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpServletRequest request, @RequestBody(required = false) LogoutRequest logoutRequest) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			String username = auth != null ? auth.getName() : "unknown";
			
			// If JWT is enabled, add tokens to blacklist
			if (jwtProperties.isEnabled()) {
				// Blacklist access token from Authorization header
				String accessToken = extractTokenFromRequest(request);
				if (accessToken != null) {
					tokenBlacklistService.blacklistToken(accessToken);
					logger.logSecurityEvent("ACCESS_TOKEN_BLACKLISTED", username, 
							"JWT access token blacklisted on logout", "LOW");
				}
				
				// Blacklist refresh token if provided in request body
				if (logoutRequest != null && logoutRequest.getRefreshToken() != null) {
					tokenBlacklistService.blacklistToken(logoutRequest.getRefreshToken());
					logger.logSecurityEvent("REFRESH_TOKEN_BLACKLISTED", username, 
							"JWT refresh token blacklisted on logout", "LOW");
				}
			}
			
			// Clear security context
			SecurityContextHolder.clearContext();
			
			logger.logUserActivity(username, "LOGOUT", "/auth/logout", 
					"User logged out successfully");
			logger.logSecurityEvent("USER_LOGOUT", username, "User logged out", "LOW");
			
			Map<String, String> response = new HashMap<>();
			response.put("message", "Logout successful");
			
			return ResponseEntity.ok(response);
			
		} catch (Exception e) {
			logger.logException("LOGOUT_ERROR", e, "Error during logout");
			
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "Logout failed");
			errorResponse.put("message", e.getMessage());
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}
	
	/**
	 * Extract JWT token from Authorization header
	 */
	private String extractTokenFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7); // Remove "Bearer " prefix
		}
		
		return null;
	}

	@GetMapping("/config")
	public ResponseEntity<?> getAuthConfig() {
		Map<String, Object> config = new HashMap<>();
		config.put("jwtEnabled", jwtProperties.isEnabled());
		config.put("authenticationMethod", jwtProperties.isEnabled() ? "JWT" : "Basic Auth");
		config.put("jwtEndpoints", jwtProperties.isEnabled() ? 
				new String[]{"/auth/login", "/auth/refresh", "/auth/validate", "/auth/status", "/auth/logout"} : 
				new String[]{"Use HTTP Basic Authentication"});
		config.put("message", jwtProperties.isEnabled() ? 
				"JWT Authentication is enabled" : 
				"Basic Authentication is enabled");
		
		logger.logUserActivity("anonymous", "CONFIG_CHECK", "/auth/config", 
				"Authentication configuration requested");
		
		return ResponseEntity.ok(config);
	}
}