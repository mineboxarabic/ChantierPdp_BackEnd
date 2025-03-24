package com.danone.pdpbackend.entities;


import com.danone.pdpbackend.Utils.EntrepriseType;
import com.danone.pdpbackend.Utils.Image.ImageModel;
import com.danone.pdpbackend.Utils.MedecinDuTravailleEE;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Entity(name = "entreprise")
@Getter
@Setter
public class Entreprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EntrepriseType type = EntrepriseType.EE; // ✅ Defines if it's EU or EE

    private String nom; // ✅ Name of the company

    private String description;

    @Column(name = "numerotelephone")
    private String numTel;

/*
    @OneToMany(mappedBy = "entrepriseUtilisatrice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Chantier> chantiers; // ✅ If this entreprise is an EU, it has chantiers
*/

    @Column(name = "raisonsociale")
    private String raisonSociale;

    @Column(name = "image")
    @Embedded
    private ImageModel image; // ✅ For storing binary data (e.g., logos)

    @Embedded
    private MedecinDuTravailleEE medecinDuTravailleEE;

    @OneToMany(mappedBy = "entrepriseExterieure", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pdp> pdps; // ✅ If this entreprise is an EE, it has PDPs

    @OneToMany(mappedBy = "entreprise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Worker> workers; // ✅ Workers employed by this entreprise

    public Entreprise() {
    }

}
