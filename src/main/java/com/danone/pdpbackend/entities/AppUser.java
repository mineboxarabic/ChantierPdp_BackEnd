package com.danone.pdpbackend.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "users")
@Getter
@Setter
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment primary key
    private Long id;
    @Column(name = "name")
    private String name;
    private String role;
    private String fonction;
    private String notel;
    private String email;
    private String password;
}
