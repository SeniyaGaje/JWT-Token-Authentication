package com.example.Token_Authentication.service;

import com.example.Token_Authentication.dto.*;
import com.example.Token_Authentication.entity.*;
import com.example.Token_Authentication.repository.*;
import com.example.Token_Authentication.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return new JwtResponse(jwt, refreshToken.getToken(), userDetails.getUsername());
    }

    public void registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        UserEntity user = new UserEntity();
        user.setEmail(signUpRequest.getEmail());
        user.setHashPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());

        Set<RoleEntity> roles = new HashSet<>();
        if (signUpRequest.getRoles() == null || signUpRequest.getRoles().isEmpty()) {
            RoleEntity userRole = roleRepository.findByName("ROLE_CASHIER")
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            roles.add(userRole);
        } else {
            signUpRequest.getRoles().forEach(role -> {
                RoleEntity userRole = roleRepository.findByName(role)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + role));
                roles.add(userRole);
            });
        }

        user.setRoles(roles);
        userRepository.save(user);
    }


    public JwtResponse refreshToken(String refreshToken) {
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
                .orElseThrow(() -> {
                    System.err.println("Refresh token not found or invalid: " + refreshToken);
                    return new RuntimeException("Refresh token is invalid");
                });
    }

}