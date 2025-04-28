package com.danone.pdpbackend.entities.dto;


import com.danone.pdpbackend.entities.Risque;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalyseDeRisqueDTO {
    private Long id;
    private String deroulementDesTaches;
    private String moyensUtilises;
    private Risque risque;
    private String mesuresDePrevention;
}
