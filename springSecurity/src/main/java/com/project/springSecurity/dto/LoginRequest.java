package com.project.springSecurity.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String emailOrUsername;
    private String password;
}