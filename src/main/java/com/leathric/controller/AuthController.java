package com.leathric.controller;

import com.leathric.dto.ApiResponse;
import com.leathric.dto.AuthDtos;
import com.leathric.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.<AuthDtos.AuthResponse>builder()
                .success(true)
                .message("Registration successful")
                .data(authService.register(request))
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.<AuthDtos.AuthResponse>builder()
                .success(true)
                .message("Login successful")
                .data(authService.login(request))
                .build());
    }
}
