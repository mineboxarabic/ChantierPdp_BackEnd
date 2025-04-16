package com.danone.pdpbackend.entities.dto;


import com.danone.pdpbackend.entities.BDT.BDT;
import com.danone.pdpbackend.entities.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private List<Long> bdts;
    private List<Long> pdps;
    private List<Long> workerSelections = new ArrayList<>();
    private List<Long> workers;
}
