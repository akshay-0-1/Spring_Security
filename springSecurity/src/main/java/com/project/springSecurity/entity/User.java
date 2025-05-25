package com.project.springSecurity.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Data
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String password;


    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles;

}
