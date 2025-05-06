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
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Collections.singleton("ROLE_USER"));
        userRepository.save(user);
        return new RegisterResponse("User registered successfully!");
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid username or password");
        }
        UserDetails user = userRepository.findByUsername(request.getUsername())
                .map(u -> new org.springframework.security.core.userdetails.User(
                        u.getUsername(),
                        u.getPassword(),
                        u.getRoles().stream().map(SimpleGrantedAuthority::new).toList()
                )).orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getUsername());
        String msg = "Login successful! Use this token for Authorization header: Bearer <token>";
        return new LoginResponse(msg, token);
    }
}