package com.danone.pdpbackend.entities.dto;


import com.danone.pdpbackend.Utils.HoraireDeTravaille;
import com.danone.pdpbackend.Utils.MisesEnDisposition;
import com.danone.pdpbackend.entities.Entreprise;
import com.danone.pdpbackend.entities.ObjectAnswered;
import com.danone.pdpbackend.entities.ObjectAnsweredEntreprises;
import com.danone.pdpbackend.entities.Worker;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Data
public class PdpDTO {
    private Long id;
    private Long chantier; // ✅ PDP is always linked to a chantier
    private Long entrepriseExterieure; // ✅ PDP is linked to an EE (Entreprise Extérieure)
    private Date dateInspection;
    private Date icpdate;
    private Date datePrevenirCSSCT; // ✅ Notification date for CSSCT (if required)
    private Date datePrev; // ✅ Planned date for something (depends on business rules)

    private String horairesDetails;
    private Long entrepriseDInspection;

    private HoraireDeTravaille horaireDeTravail;
    private MisesEnDisposition misesEnDisposition;

    private List<ObjectAnswered> risques;
    private List<ObjectAnswered> dispositifs;
    private List<ObjectAnswered> permits;
    private List<ObjectAnsweredEntreprises> analyseDeRisques;

    private List<Long> signatures; // ✅ List of workers who signed the PDP

}