package com.example.Token_Authentication.service;

import com.example.Token_Authentication.entity.RefreshTokenEntity;
import com.example.Token_Authentication.repository.RefreshTokenRepository;
import com.example.Token_Authentication.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${app.jwt.refreshTokenExpirationMs}")
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;


    public RefreshTokenEntity createRefreshToken(Long userId) {
        deleteByUserId(userId);

        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setUser(userRepository.findById(userId).orElseThrow());

        Instant expiry = Instant.now()
                .plusMillis(refreshTokenDurationMs)
                .plus(5, ChronoUnit.HOURS)
                .plus(30, ChronoUnit.MINUTES);

        System.out.println("Adjusted expiry (UTC+5:30): " + expiry);

        refreshToken.setExpiryDate(expiry);
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshTokenEntity> findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(t -> {
                    System.out.println("Token found, expires at: " + t.getExpiryDate());
                    return t;
                });
    }

    public RefreshTokenEntity verifyExpiration(RefreshTokenEntity token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired");
        }
        return token;
    }
    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }
}