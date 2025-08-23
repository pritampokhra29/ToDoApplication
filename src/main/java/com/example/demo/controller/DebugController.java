package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @GetMapping("/auth")
    public ResponseEntity<?> debugAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("authenticationExists", auth != null);
        
        if (auth != null) {
            debugInfo.put("isAuthenticated", auth.isAuthenticated());
            debugInfo.put("principal", auth.getName());
            debugInfo.put("authClass", auth.getClass().getSimpleName());
            debugInfo.put("authorities", auth.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(Collectors.toList()));
            debugInfo.put("hasRoleAdmin", auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        } else {
            debugInfo.put("message", "No authentication found");
        }
        
        return ResponseEntity.ok(debugInfo);
    }
}
