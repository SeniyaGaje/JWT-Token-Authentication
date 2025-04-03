package com.example.Token_Authentication.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRefreshRequest {

    private String refreshToken;
}