package com.danone.pdpbackend.entities;


import com.danone.pdpbackend.Utils.HoraireDeTravaille;
import com.danone.pdpbackend.Utils.MedecinDuTravailleEE;
import com.danone.pdpbackend.Utils.MisesEnDisposition;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(name = "pdp")
@Getter
@Setter
public class Pdp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private Long chantier; // ✅ PDP is always linked to a chantier

    @ManyToOne
    private Entreprise entrepriseExterieure; // ✅ PDP is linked to an EE (Entreprise Extérieure)


    private Date dateInspection = new Date();
    private Date icpdate = new Date();
    private Date datePrevenirCSSCT = new Date(); // ✅ Notification date for CSSCT (if required)
    private Date datePrev = new Date();// ✅ Planned date for something (depends on business rules)

    private String horairesDetails;


    @OneToOne
    private Entreprise entrepriseDInspection;

    @Embedded
    private HoraireDeTravaille horaireDeTravail;

    @Embedded
    private MisesEnDisposition misesEnDisposition;


    @OneToMany(mappedBy = "pdp", cascade = CascadeType.ALL)
    private List<ObjectAnswered> relations = new ArrayList<>();

    @OneToMany
    private List<Worker> signatures = new ArrayList<>();// ✅ List of workers who signed the PDP

}