package com.danone.pdpbackend.entities;


import com.danone.pdpbackend.entities.dto.AnalyseDeRisqueDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity(name = "analyse_de_risque")
@Getter
@Setter
public class AnalyseDeRisque{
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private String deroulementDesTaches;
    private String moyensUtilises;

    @ManyToOne
    private Risque risque;

    private String mesuresDePrevention;


    public void buildAnalyseDeRisque(AnalyseDeRisqueDTO analyseDeRisqueDTO) {
        this.deroulementDesTaches = analyseDeRisqueDTO.getDeroulementDesTaches();
        this.moyensUtilises = analyseDeRisqueDTO.getMoyensUtilises();
        this.mesuresDePrevention = analyseDeRisqueDTO.getMesuresDePrevention();
    }
}
