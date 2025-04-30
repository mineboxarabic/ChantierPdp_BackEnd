package com.danone.pdpbackend.entities.BDT;


import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.entities.*;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "BDT")
@Getter
@Setter
public class Bdt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String nom;


    @OneToMany
    private List<ObjectAnswered> relations;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<ComplementOuRappel> complementOuRappels;

    @ManyToOne
    @JsonIgnoreProperties({"bdts", "pdps", "workers"})
    private Chantier chantier;

    @ManyToOne
    private Entreprise entrepriseExterieure; // ✅ BDT is linked to an EE (Entreprise Extérieure)}

    @OneToOne
    private Signature signatureChargeDeTravail;
    @OneToOne
    private Signature signatureDonneurDOrdre;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status = DocumentStatus.DRAFT;

    private LocalDate date;

    @OneToMany
    private List<ObjectAnswered> permitRelations;

    @OneToMany
    private List<Worker> signatures = new ArrayList<>();// ✅ List of workers who signed the PDP
}