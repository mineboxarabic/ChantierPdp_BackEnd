package com.danone.pdpbackend.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "localisation")
@Getter
@Setter
public class Localisation {

    @Id
    private Long id;

    private String nom;
    private String code;
    private String description;

}
