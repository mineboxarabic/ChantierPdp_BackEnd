package com.danone.pdpbackend.entities;


import com.danone.pdpbackend.dto.AnalyseDeRisqueDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;


@Entity(name = "analyse_de_risque")
@Getter
@Setter
public class AnalyseDeRisque{
    @Id
    private Long id;
    private String deroulementDesTaches;
    private String moyensUtilises;
    @OneToOne
    private Risque risque;
    private String mesuresDePrevention;
    private Boolean entrepriseExterieure;
    private Boolean entrepriseUtilisatrice;

    public void buildAnalyseDeRisque(AnalyseDeRisqueDTO analyseDeRisqueDTO) {
        this.deroulementDesTaches = analyseDeRisqueDTO.getDeroulementDesTaches();
        this.moyensUtilises = analyseDeRisqueDTO.getMoyensUtilises();
        this.mesuresDePrevention = analyseDeRisqueDTO.getMesuresDePrevention();
        this.entrepriseExterieure = analyseDeRisqueDTO.getEntrepriseExterieure();
        this.entrepriseUtilisatrice = analyseDeRisqueDTO.getEntrepriseUtilisatrice();
    }
}
