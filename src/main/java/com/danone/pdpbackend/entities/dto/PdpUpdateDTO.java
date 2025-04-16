package com.danone.pdpbackend.entities.dto;

import com.danone.pdpbackend.Utils.HoraireDeTravaille;
import com.danone.pdpbackend.Utils.MedecinDuTravailleEE;
import com.danone.pdpbackend.Utils.MisesEnDisposition;
import com.danone.pdpbackend.entities.Entreprise;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class PdpUpdateDTO {
    private String operation;
    private String lieuintervention;
    private Date datedebuttravaux;
    private Date datefintravaux;
    private Integer effectifmaxisurchantier;
    private Integer nombreinterimaires;
    private HoraireDeTravaille horaireDeTravail;
    private String horairesdetail;
    private Date icpdate;
    private MedecinDuTravailleEE medecinDuTravailleEE;

    private Date dateInspection;
    private Entreprise entrepriseDInspection;
    private List<Entreprise> entrepriseexterieure;
    private Entreprise entrepriseetutilisatrise;
    private MisesEnDisposition misesEnDisposition;

    private String medecintravaileu;
    private String medecintravailee;
    private Date dateprevenircssct;
    private Date dateprev;
    private String location;
}
