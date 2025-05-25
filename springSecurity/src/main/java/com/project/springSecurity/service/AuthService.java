package com.project.springSecurity.service;

import com.project.springSecurity.dto.LoginRequest;
import com.project.springSecurity.dto.LoginResponse;
import com.project.springSecurity.dto.RegisterRequest;
import com.project.springSecurity.dto.RegisterResponse;
import com.project.springSecurity.entity.User;
import com.project.springSecurity.repository.UserRepository;
import com.project.springSecurity.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Collections.singleton("ROLE_USER"));
        userRepository.save(user);

        return new RegisterResponse("User registered successfully!");
    }

    public LoginResponse login(LoginRequest request) {
        // Try to find user by email first
        Optional<User> userOptional = userRepository.findByEmail(request.getEmailOrUsername());

        // If not found by email, try username
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByUsername(request.getEmailOrUsername());
        }

        // If still not found, throw a generic error message
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid email/username or password");
        }

        User user = userOptional.get();

        try {
            // Authenticate using the value they provided (which could be username or email)
            // This relies on our updated MyUserDetailsService to handle either
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmailOrUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid email/username or password");
        }

        // Generate token using username
        String token = jwtUtil.generateToken(user.getUsername());
        String msg = "Login successful! Use this token for Authorization header: Bearer " + token;

        return new LoginResponse(msg, token);
    }
}