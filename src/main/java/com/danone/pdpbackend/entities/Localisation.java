package com.danone.pdpbackend.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "localisation")
@Getter
@Setter
@NoArgsConstructor
public class Localisation {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String code;

    private String description;

    public Localisation(long l) {
        this.id = l;
    }
}
