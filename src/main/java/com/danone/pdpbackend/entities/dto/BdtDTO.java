package com.danone.pdpbackend.entities.dto;


import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.entities.BDT.ComplementOuRappel;
import com.danone.pdpbackend.entities.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class BdtDTO {


    private Long id;

    private String nom;

    private List<ObjectAnsweredDTO> relations;

    private List<ComplementOuRappel> complementOuRappels;


    private Long chantier;

    private Long entrepriseExterieure; // ✅ BDT is linked to an EE (Entreprise Extérieure)}

    private Long signatureChargeDeTravail;
    private Long signatureDonneurDOrdre;


    private DocumentStatus status = DocumentStatus.DRAFT;

    private LocalDate date;

    private List<ObjectAnsweredDTO> permitRelations;

    private List<Long> signatures = new ArrayList<>();// ✅ List of workers who signed the PDP
}