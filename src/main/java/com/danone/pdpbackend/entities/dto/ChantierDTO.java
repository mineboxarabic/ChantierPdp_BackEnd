package com.danone.pdpbackend.entities.dto;


import com.danone.pdpbackend.Utils.ChantierStatus;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class ChantierDTO {
    private Long id;
    private String nom;
    private String operation;
    private Date dateDebut;
    private Date dateFin;
    private Integer nbHeurs;
    private Boolean isAnnuelle;
    private Integer effectifMaxiSurChantier;
    private Integer nombreInterimaires;
    private List<Long> entrepriseExterieurs;
    private Long entrepriseUtilisatrice;
    private Long localisation;
    private Long donneurDOrdre;
    private List<Long> bdts = new ArrayList<>();
    private List<Long> pdps = new ArrayList<>();
    private List<Long> workerSelections = new ArrayList<>();
    private ChantierStatus status;
    private Boolean travauxDangereux = false;
}
