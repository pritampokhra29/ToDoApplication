package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserResponseDTO;
import com.example.demo.dto.UserUpdateDTO;
import com.example.demo.dto.UserNameDTO;
import java.util.List;

public interface UserService {
	boolean registerUser(UserDTO userDTO);
	List<UserResponseDTO> getAllUsers();
	UserResponseDTO updateUser(UserUpdateDTO userUpdateDTO);
	List<UserNameDTO> getActiveUserNames();
	List<UserNameDTO> getActiveUserNames(String excludeUsername);
}
