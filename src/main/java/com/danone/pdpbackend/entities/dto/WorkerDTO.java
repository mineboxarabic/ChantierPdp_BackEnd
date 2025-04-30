package com.danone.pdpbackend.entities.dto;

import lombok.Data;

import java.util.List;
@Data
public class WorkerDTO {
    private Long id;
    private String nom;
    private String prenom;
    private Long entreprise;
    private List<Long> pdps;
    private List<Long> signatures;
    private List<Long> chantiers;
    private List<Long> chantierSelections;
}
