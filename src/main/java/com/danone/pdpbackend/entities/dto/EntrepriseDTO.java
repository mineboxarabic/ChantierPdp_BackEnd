package com.danone.pdpbackend.entities.dto;

import com.danone.pdpbackend.entities.User;
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
    private User referentPdp;
    private User responsableChantier;
    private String raisonSociale;
    private Boolean isUtilisatrise;

}
