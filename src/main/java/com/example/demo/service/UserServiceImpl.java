package com.example.demo.service;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.config.CustomPasswordEncoder;
import com.example.demo.dto.UserDTO;
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
			user.setActive(true);
			user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
			user.setEmail(userDTO.getEmail());
			user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
			user.setPassword(customPasswordEncoder.encode(userDTO.getPassword()));
			user.setUsername(userDTO.getUsername());
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
}
