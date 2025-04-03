package com.example.Token_Authentication.controller;

import com.example.Token_Authentication.dto.*;
import com.example.Token_Authentication.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            return ResponseEntity.ok(authService.authenticateUser(loginRequest));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Authentication failed", e.getMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        try {
            authService.registerUser(signUpRequest);
            return ResponseEntity.ok(new SuccessResponse("User registered successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Registration failed", e.getMessage()));
        }
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@RequestBody TokenRefreshRequest request) {
        try {
            return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Token refresh failed", e.getMessage()));
        }
    }

    @PostMapping("/signout")
    @Transactional
    public ResponseEntity<?> logoutUser(@RequestBody TokenRefreshRequest request) {
        try {
            authService.signOut(request.getRefreshToken());
            return ResponseEntity.ok(new SuccessResponse("Successfully signed out"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Sign out failed", e.getMessage()));
        }
    }

    @Getter
    @AllArgsConstructor
    private static class SuccessResponse {
        private final String message;
    }

    @Getter
    @AllArgsConstructor
    private static class ErrorResponse {
        private final String error;
        private final String details;
    }
}