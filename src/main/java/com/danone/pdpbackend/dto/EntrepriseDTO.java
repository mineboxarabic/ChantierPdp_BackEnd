package com.danone.pdpbackend.dto;

import com.danone.pdpbackend.entities.AppUser;
import com.danone.pdpbackend.entities.Entreprise;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EntrepriseDTO {
    private String nom;
    private String fonction;
    private String numTel;
    private AppUser referentPdp;
    private AppUser responsableChantier;
    private String raisonSociale;
    private Boolean isUtilisatrise;

}
