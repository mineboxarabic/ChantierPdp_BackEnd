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


    @ManyToOne
    private Chantier chantier; // ✅ PDP is always linked to a chantier

    @ManyToOne
    private Entreprise entrepriseExterieure; // ✅ PDP is linked to an EE (Entreprise Extérieure)


    private Date dateInspection;
    private Date icpdate;
    private String horairesDetails;


    @OneToOne
    private Entreprise entrepriseDInspection;

    @Embedded
    private HoraireDeTravaille horaireDeTravail;

    @Embedded
    private MisesEnDisposition misesEnDisposition;


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ObjectAnswered> risques;


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ObjectAnswered> dispositifs;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ObjectAnswered> permits;


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ObjectAnsweredEntreprises> analyseDeRisques;


    @OneToMany(mappedBy = "pdp", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Worker> signatures; // ✅ List of workers who signed the PDP


    private Date datePrevenirCSSCT; // ✅ Notification date for CSSCT (if required)
    private Date datePrev; // ✅ Planned date for something (depends on business rules)

}