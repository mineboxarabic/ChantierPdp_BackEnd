package com.danone.pdpbackend.entities;


import com.danone.pdpbackend.entities.BDT.BDT;
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
public class Chantier {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nom;


    private Date dateDebut;

    private Date dateFin;

    private Integer nbHeurs;

    @ManyToOne
    @JoinColumn(name = "entreprise_exterieur_id", nullable = false)
    private Entreprise entrepriseExterieur;

    @OneToOne
    private User responsable;

    @ManyToOne
    private Localisation localisation;

    @OneToMany
    private List<BDT> bdts;

    @ManyToOne
    private Pdp pdp;

    private String description;



}
