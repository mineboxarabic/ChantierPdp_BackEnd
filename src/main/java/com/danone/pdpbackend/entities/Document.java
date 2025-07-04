package com.danone.pdpbackend.entities;

import com.danone.pdpbackend.Utils.ActionType;
import com.danone.pdpbackend.Utils.DocumentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED) // or SINGLE_TABLE if you prefer
@Getter
@Setter
public abstract class Document {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne
    private Chantier chantier;

    @ManyToOne
    private Entreprise entrepriseExterieure;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status = DocumentStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    private ActionType actionType = ActionType.NONE;

    private LocalDate date;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DocumentSignature> signatures = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ObjectAnswered> relations = new ArrayList<>();

    private LocalDate creationDate = LocalDate.now(); // Track when it was created/became valid
    private LocalDate lastUpdate = LocalDate.now(); // Track when it was last updated


    @PreUpdate
    protected void onUpdate() {
        lastUpdate = LocalDate.now();
    }
}
