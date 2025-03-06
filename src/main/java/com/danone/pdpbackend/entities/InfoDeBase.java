package com.danone.pdpbackend.entities;

import com.danone.pdpbackend.Utils.Image.ImageModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class InfoDeBase {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private String title;
    private String description;
    @Column(name = "logo")
    @Embedded
    private ImageModel logo; // For storing binary data
}
