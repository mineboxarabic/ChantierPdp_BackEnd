package com.danone.pdpbackend.entities;


import com.danone.pdpbackend.Utils.HoraireDeTravaille;
import com.danone.pdpbackend.Utils.MedecinDuTravailleEE;
import com.danone.pdpbackend.Utils.MisesEnDisposition;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity(name = "pdp")
@Getter
@Setter
public class Pdp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String operation;
    private String lieuintervention;
    private Date datedebuttravaux;
    private Date datefintravaux;



    private Date dateInspection;
    @OneToOne
    private Entreprise entrepriseDInspection;


    private Integer effectifmaxisurchantier;
    private Integer nombreinterimaires;

    @Embedded
    private HoraireDeTravaille horaireDeTravail;

    private String horairesdetail;
    private Date icpdate;

    @ManyToMany
    private List<Entreprise> entrepriseexterieure;

    @ManyToOne
    private Entreprise entrepriseutilisatrice;

    @Embedded
    private MisesEnDisposition misesEnDisposition;

    @Embedded
    private MedecinDuTravailleEE medecinDuTravailleEE;
    private String medecintravaileu;
    private String medecintravailee;

    @ManyToMany
    private List<Entreprise> sousTraitants;


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ObjectAnswered> risques;


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ObjectAnswered> dispositifs;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ObjectAnswered> permits;


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ObjectAnsweredEntreprises> analyseDeRisques;

    @ManyToOne
    private Localisation localisation;




    @ManyToMany
    private List<Signature> signatures;

    private Date datePrevenirCSSCT;
    private Date datePrev;
    private String location;
}