package com.example.Token_Authentication.repository;

import com.example.Token_Authentication.entity.RefreshTokenEntity;
import com.example.Token_Authentication.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.token = :token")
    void deleteByToken(String token);
    Optional<RefreshTokenEntity> findByToken(String token);
    @Modifying
    void deleteByUser(UserEntity user);
}