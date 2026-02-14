package com.leathric.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class AuthDtos {

    @Getter
    @Setter
    public static class RegisterRequest {
        @NotBlank
        private String fullName;

        @Email
        @NotBlank
        private String email;

        @NotBlank
        private String password;
    }

    @Getter
    @Setter
    public static class LoginRequest {
        @Email
        @NotBlank
        private String email;

        @NotBlank
        private String password;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String email;
        private String fullName;
    }
}
