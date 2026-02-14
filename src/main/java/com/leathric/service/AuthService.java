package com.leathric.service;

import com.leathric.dto.AuthDtos;

public interface AuthService {
    AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request);

    AuthDtos.AuthResponse login(AuthDtos.LoginRequest request);
}
