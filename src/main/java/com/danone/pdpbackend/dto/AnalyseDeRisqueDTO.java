package com.danone.pdpbackend.dto;


import com.danone.pdpbackend.entities.Risque;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalyseDeRisqueDTO {
    private String deroulementDesTaches;
    private String moyensUtilises;
    private Risque risque;
    private String mesuresDePrevention;
    private Boolean entrepriseExterieure;
    private Boolean entrepriseUtilisatrice;
}
