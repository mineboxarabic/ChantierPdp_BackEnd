package com.danone.pdpbackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity(name = "WorkerChantierSelection")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "worker_chantier_selection")
public class WorkerChantierSelection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    @JsonIgnoreProperties({"pdp", "signatures", "chantiers", "entreprise"})
    private Worker worker;

    @ManyToOne
    @JoinColumn(name = "chantier_id")
    @JsonIgnoreProperties({"entrepriseExterieurs", "entrepriseUtilisatrice", "localisation", "donneurDOrdre", "bdts", "pdps", "workers", "workerSelections"})
    private Chantier chantier;

    private Date selectionDate;

    private Boolean isSelected = true;

    private String selectionNote;

    // Optional: who made the selection
    @ManyToOne
    @JoinColumn(name = "selected_by")
    private User selectedBy;
}