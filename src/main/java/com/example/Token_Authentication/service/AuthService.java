package com.example.Token_Authentication.service;

import com.example.Token_Authentication.dto.*;
import com.example.Token_Authentication.entity.*;
import com.example.Token_Authentication.repository.*;
import com.example.Token_Authentication.utils.JwtUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private RefreshTokenService refreshTokenService;

    @Transactional
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            UserEntity user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + loginRequest.getEmail()));

            refreshTokenService.deleteByUserId(user.getUserId());

            String jwt = jwtUtils.generateJwtToken(authentication);
            RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(user.getUserId());

            return new JwtResponse(jwt, refreshToken.getToken(), user.getEmail());
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid email or password");
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    public void registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Email '" + signUpRequest.getEmail() + "' is already in use");
        }

        try {
            UserEntity user = new UserEntity();
            user.setEmail(signUpRequest.getEmail());
            user.setHashPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            user.setFirstName(signUpRequest.getFirstName());
            user.setLastName(signUpRequest.getLastName());

            Set<RoleEntity> roles = new HashSet<>();
            if (signUpRequest.getRoles() == null || signUpRequest.getRoles().isEmpty()) {
                RoleEntity userRole = roleRepository.findByName("ROLE_CASHIER")
                        .orElseThrow(() -> new RuntimeException("Default role 'ROLE_CASHIER' not found"));
                roles.add(userRole);
            } else {
                signUpRequest.getRoles().forEach(role -> {
                    RoleEntity userRole = roleRepository.findByName(role)
                            .orElseThrow(() -> new RuntimeException("Role '" + role + "' not found"));
                    roles.add(userRole);
                });
            }

            user.setRoles(roles);
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    @Transactional
    public void signOut(String refreshToken) {
        try {
            refreshTokenService.findByToken(refreshToken).ifPresentOrElse(
                    token -> {
                        refreshTokenService.deleteByToken(token.getToken());
                        System.out.println("Deleted refresh token for user: " + token.getUser().getEmail());
                    },
                    () -> {
                        throw new RuntimeException("Refresh token not found");
                    }
            );
        } catch (Exception e) {
            throw new RuntimeException("Sign out failed: " + e.getMessage());
        }
    }

    public JwtResponse refreshToken(String refreshToken) {
        try {
            System.out.println("Attempting refresh with token: " + refreshToken);

            return refreshTokenService.findByToken(refreshToken)
                    .map(foundToken -> {
                        System.out.println("Found token, expiry: " + foundToken.getExpiryDate());
                        return refreshTokenService.verifyExpiration(foundToken);
                    })
                    .map(RefreshTokenEntity::getUser)
                    .map(user -> {
                        String newAccessToken = jwtUtils.generateTokenFromUsername(user.getEmail());
                        return new JwtResponse(newAccessToken, refreshToken, user.getEmail());
                    })
                    .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        } catch (Exception e) {
            throw new RuntimeException("Token refresh failed: " + e.getMessage());
        }
    }
}