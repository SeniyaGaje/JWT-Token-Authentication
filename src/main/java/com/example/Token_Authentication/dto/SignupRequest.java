package com.example.Token_Authentication.dto;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class SignupRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Set<String> roles;
}