package com.danone.pdpbackend.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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
    @JsonIgnoreProperties({"workers", "pdps", "chantiers"})
    private Entreprise entreprise;

    @ManyToMany
    @JsonIgnore
    private List<Pdp> pdp;

    @ManyToMany
    private List<Signature> signatures;

    @ManyToMany
    @JsonIgnore
    private List<Chantier> chantiers;

    // Nouvelle relation avec la table de jointure
    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<WorkerChantierSelection> chantierSelections;
}
