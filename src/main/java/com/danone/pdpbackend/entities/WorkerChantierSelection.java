package com.danone.pdpbackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    private Worker worker;

    @ManyToOne
    @JoinColumn(name = "chantier_id")
    private Chantier chantier;

    private Date selectionDate;

    private Boolean isSelected = true;

    private String selectionNote;

    // Optional: who made the selection
    @ManyToOne
    @JoinColumn(name = "selected_by")
    @ToString.Exclude // <--- Add this
    private User selectedBy;

    public WorkerChantierSelection(long l) {
        this.id = l;
    }
}