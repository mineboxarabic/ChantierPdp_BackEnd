package com.danone.pdpbackend.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nom;

    private String prenom;


    @ManyToOne
    private Entreprise entreprise;


    @ManyToOne
    private Pdp pdp;


    @ManyToMany
    private List<Signature> signatures;

    @ManyToOne
    private Chantier chantier;
}
