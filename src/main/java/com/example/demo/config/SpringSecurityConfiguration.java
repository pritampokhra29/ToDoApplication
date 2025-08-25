package com.example.demo.config;

import com.example.demo.filter.JwtAuthenticationFilter;
import com.example.demo.util.CustomLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

import com.example.demo.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SpringSecurityConfiguration {

	private static final CustomLogger logger = CustomLogger.getLogger(SpringSecurityConfiguration.class);

	@Autowired
	private CustomUserDetailsService customUserDetailsService;

	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Autowired
	private JwtProperties jwtProperties;

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(Arrays.asList("*")); // Allow all origins for development
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);
		configuration.setExposedHeaders(Arrays.asList("Authorization"));
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		
		logger.logSecurityEvent("CORS_CONFIG", "system", "CORS configuration enabled for all origins", "LOW");
		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		logger.logBusinessOperation("SECURITY_CONFIG", "System", "Configuration", 
				"CONFIGURE", "Configuring security with JWT enabled: " + jwtProperties.isEnabled());

		if (jwtProperties.isEnabled()) {
			// JWT Authentication Configuration
			logger.logSecurityEvent("SECURITY_MODE", "system", "JWT authentication mode enabled", "LOW");
			
			http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
					.csrf(csrf -> csrf.disable())
					.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
					.authorizeHttpRequests(auth -> auth
							.requestMatchers("/auth/login", "/auth/refresh", "/auth/validate", "/auth/status", "/auth/config").permitAll() 
							.requestMatchers("/h2-console/**").permitAll() 
							.anyRequest().authenticated())
					.userDetailsService(customUserDetailsService)
					.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
					.headers(headers -> headers
							.frameOptions(frameOptions -> frameOptions.sameOrigin()));
		} else {
			// Basic Authentication Configuration (Fallback)
			logger.logSecurityEvent("SECURITY_MODE", "system", "Basic authentication mode enabled", "LOW");
			
			http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
					.csrf(csrf -> csrf.disable())
					.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
					.authorizeHttpRequests(auth -> auth
							.requestMatchers("/auth/config", "/auth/register").permitAll()
							.requestMatchers("/h2-console/**").permitAll()
							.anyRequest().authenticated())
					.httpBasic(basic -> basic.realmName("ToDo Application"))
					.userDetailsService(customUserDetailsService)
					.headers(headers -> headers
							.frameOptions(frameOptions -> frameOptions.sameOrigin()));
		}
		
		return http.build();
	}
}
