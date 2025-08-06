package com.danone.pdpbackend.entities;


import com.danone.pdpbackend.Utils.EntrepriseType;
import com.danone.pdpbackend.Utils.Image.ImageModel;
import com.danone.pdpbackend.Utils.MedecinDuTravailleEE;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Entity(name = "entreprise")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

    @Column(name = "raisonsociale")
    private String raisonSociale;

    @Column(name = "address")
    private String address; // ✅ Address of the company

    @Column(name = "image")
    @Embedded
    private ImageModel image; // ✅ For storing binary data (e.g., logos)

    @Embedded
    private MedecinDuTravailleEE medecinDuTravailleEE;

    @OneToMany(mappedBy = "entrepriseExterieure", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Pdp> pdps = new ArrayList<>(); // ✅ If this entreprise is an EE, it has PDPs

    @OneToMany(mappedBy = "entrepriseExterieure", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Bdt> bdts = new ArrayList<>();// ✅ If this entreprise is an EE, it has BDTs

    @OneToMany(mappedBy = "entreprise", cascade = CascadeType.ALL)
    private List<Worker> workers = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "responsable_chantier_id")
    private User responsableChantier;

    public Entreprise(long l) {
        this.id = l;
    }
}
