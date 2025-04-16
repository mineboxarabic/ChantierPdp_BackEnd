package com.danone.pdpbackend.entities;


import com.danone.pdpbackend.entities.BDT.BDT;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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
    private Boolean isAnnuelle;

    private Integer effectifMaxiSurChantier;
    private Integer nombreInterimaires;

    @ManyToMany
    @JsonIgnoreProperties({"chantiers", "pdps"})
    private List<Entreprise> entrepriseExterieurs;

    @ManyToOne
    private Entreprise entrepriseUtilisatrice;

    @ManyToOne
    private Localisation localisation;

    @ManyToOne
    @JoinColumn(name = "donneurDOrdre_id")
    private User donneurDOrdre;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "chantier")
    private List<BDT> bdts;

    @OneToMany
    private List<Pdp> pdps;

    // Replace the direct many-to-many with the join entity
    @OneToMany(mappedBy = "chantier", cascade = CascadeType.ALL)
    private List<WorkerChantierSelection> workerSelections = new ArrayList<>();

    // Keep this for backward compatibility if needed
    @ManyToMany
    @Transient // This makes it not persist to the database
    private List<Worker> workers;
}
