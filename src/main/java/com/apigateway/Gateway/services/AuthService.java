package com.apigateway.Gateway.services;

import com.apigateway.Gateway.entities.AuthResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;

    public AuthResponse register() {
        String accessToken  = "";
        String refreshToken = "";

        return new AuthResponse(accessToken, refreshToken);
    }

}