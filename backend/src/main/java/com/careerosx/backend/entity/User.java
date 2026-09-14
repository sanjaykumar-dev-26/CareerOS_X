package com.careerosx.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false,unique = true)
    private String email;
    @Column(name = "password_hash",nullable = false)
    private String password_hash;
    
}
