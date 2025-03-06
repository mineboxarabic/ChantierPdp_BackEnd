package com.danone.pdpbackend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "object_answered_Entreprises")
@Getter
@Setter
public class ObjectAnsweredEntreprises {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne
    AnalyseDeRisque analyseDeRisque;

    Boolean EE;
    Boolean EU;
}
