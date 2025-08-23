package com.example.demo.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.cache.Cache;
// import org.springframework.cache.CacheManager;
// import org.springframework.cache.annotation.EnableCaching;
// import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repo.UserRepo;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	@Autowired
	private UserRepo userRepo;

	// Commented out caching for testing
	// private final Cache userCache;
	// @Autowired
	// public CustomUserDetailsService(CacheManager cacheManager) {
	//     this.userCache = cacheManager.getCache("userCache");
	// }

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("Loading user from database (no cache): " + username);
		
		// Commented out cache check
		// System.out.println("Checking cache for user: " + username);
		// UserDetails cachedUser = userCache.get(username, UserDetails.class);
		// if (cachedUser != null) {
		//     System.out.println("User fetched from cache: " + username);
		//     return cachedUser;
		// }

		System.out.println("Querying database for user: " + username);
		User user = userRepo.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		String role = user.getRole() != null ? user.getRole() : "USER";
		List<SimpleGrantedAuthority> authorities = Collections
				.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
		UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getUsername(),
				user.getPassword(), authorities);
		
		// Commented out cache storage
		// System.out.println("Adding user to cache: " + username);
		// userCache.put(username, userDetails);
		
		System.out.println("User loaded successfully from database: " + username);
		return userDetails;
	}
}

// Commented out cache configuration for testing
// @Configuration
// @EnableCaching
// class UserCacheConfig {
//
//	@Bean
//	public ConcurrentMapCacheManager cacheManager() {
//		return new ConcurrentMapCacheManager("userCache");
//	}
// }
