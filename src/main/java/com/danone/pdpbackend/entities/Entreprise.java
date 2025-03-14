package com.danone.pdpbackend.entities;


import com.danone.pdpbackend.Utils.Image.ImageModel;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity(name = "entreprise")
@Getter
@Setter
public class Entreprise {

    @Id
    private Long id;

    private Boolean isUtilisatrice;

    private String nom;
    private String fonction;
    @Column(name = "numerotelephone")
    private String numTel;

    @JoinColumn(name = "referentpdp")
    @ManyToOne
    private User referentPdp;

    @JoinColumn(name = "responsablechantier")
    @ManyToOne
    private User responsableChantier;

    @Column(name = "raisonsociale")
    private String raisonSociale;

    @Column(name = "image")
    @Embedded
    private ImageModel image; // For storing binary data

    public Entreprise() {

    }

}
