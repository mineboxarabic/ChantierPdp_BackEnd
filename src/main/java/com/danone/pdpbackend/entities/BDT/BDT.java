package com.danone.pdpbackend.entities.BDT;


import com.danone.pdpbackend.entities.*;
import com.fasterxml.jackson.annotation.*;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity(name = "BDT")
@Getter
@Setter
public class BDT {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String nom;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ObjectAnswered> risques;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ObjectAnswered> auditSecu;

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
}