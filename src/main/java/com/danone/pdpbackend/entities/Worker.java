package com.danone.pdpbackend.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    @JoinColumn(name = "entreprise_id")
    @JsonIgnoreProperties({"workers", "pdps", "chantiers"})
    private Entreprise entreprise;

    @ManyToMany
    @JsonIgnore
    @ToString.Exclude // <--- Add this
    private List<Pdp> pdps;

    @ManyToMany
    @JsonIgnore
    @ToString.Exclude // <--- Add this
    private List<Chantier> chantiers;

    // Nouvelle relation avec la table de jointure
    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude // <--- Add this
    private List<WorkerChantierSelection> chantierSelections;

    public Worker(long l) {
        this.id = l;
    }
}
