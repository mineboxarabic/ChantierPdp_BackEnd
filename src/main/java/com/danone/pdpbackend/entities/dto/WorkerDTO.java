package com.danone.pdpbackend.entities.dto;

import lombok.Data;

import java.util.List;
@Data
public class WorkerDTO {
    private Long id;
    private String nom;
    private String prenom;
    private Long entreprise;
}
