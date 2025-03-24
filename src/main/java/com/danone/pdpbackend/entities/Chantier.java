package com.danone.pdpbackend.entities;


import com.danone.pdpbackend.entities.BDT.BDT;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"workers"})

public class Chantier {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nom;
    private String operation;
    private Date dateDebut;
    private Date dateFin;
    private Integer nbHeurs;

    private Integer effectifMaxiSurChantier; // ✅ Max workers on site
    private Integer nombreInterimaires; // ✅ Number of temp workers

    @ManyToMany
    private List<Entreprise> entrepriseExterieurs;

    @ManyToOne
    private Entreprise entrepriseUtilisatrice;
    @ManyToOne
    private Entreprise entrepriseUtilisatricex;
    @ManyToOne
    private Localisation localisation;

    @ManyToOne
    private User donneurDOrdre;


    @OneToMany
    private List<BDT> bdts;

    @OneToMany
    private List<Pdp> pdp;

    @OneToMany(mappedBy = "chantier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Worker> workers; // ✅ Workers assigned to this chantier


}
