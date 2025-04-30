package com.danone.pdpbackend.entities.dto;


import com.danone.pdpbackend.Utils.HoraireDeTravaille;
import com.danone.pdpbackend.Utils.MisesEnDisposition;
import com.danone.pdpbackend.Utils.DocumentStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
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

    private List<ObjectAnsweredDTO> relations = new ArrayList<>();

    private List<Long> signatures; // ✅ List of workers who signed the PDP


    private DocumentStatus status; // Default status

    private LocalDate creationDate;
}