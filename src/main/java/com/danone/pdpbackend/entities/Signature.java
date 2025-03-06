package com.danone.pdpbackend.entities;


import com.danone.pdpbackend.Utils.Image.ImageModel;
import jakarta.persistence.*;

@Entity(name = "signature")
public class Signature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private ImageModel signature;
}
