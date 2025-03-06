package com.danone.pdpbackend.Utils;

import com.danone.pdpbackend.dto.EntrepriseDTO;
import com.danone.pdpbackend.entities.Entreprise;

import java.lang.reflect.Field;

public class EntrepriseMapper {
    public static EntrepriseDTO toDTO(Entreprise entreprise) {
        EntrepriseDTO entrepriseDto = EntrepriseDTO.builder()
                .nom(entreprise.getNom())
                .fonction(entreprise.getFonction())
                .numTel(entreprise.getNumTel())
                .referentPdp(entreprise.getReferentPdp())
                .responsableChantier(entreprise.getResponsableChantier())
                .raisonSociale(entreprise.getRaisonSociale())
                .isUtilisatrise(entreprise.getIsUtilisatrice())
                .build();
        return entrepriseDto;
    }

    public static Entreprise toEntity(EntrepriseDTO entrepriseDto) {
        Entreprise entreprise = new Entreprise();
/*        entreprise.setNom(entrepriseDto.getNom());
        entreprise.setFonction(entrepriseDto.getFonction());
        entreprise.setNumTel(entrepriseDto.getNumTel());
        entreprise.setReferentPdp(entrepriseDto.getReferentPdp());
        entreprise.setResponsableChantier(entrepriseDto.getResponsableChantier());
        entreprise.setRaisonSociale(entrepriseDto.getRaisonSociale());
        entreprise.setIsUtilisatrise(entrepriseDto.getIsUtilisatrise());
        */


        for (Field field : entreprise.getClass().getDeclaredFields()) {
            try {
                if (field.getName().equals("id")) {
                    continue;
                }
                Field fieldDto = entrepriseDto.getClass().getDeclaredField(field.getName());
                //if the field is id we don't want to set it

                field.setAccessible(true);
                fieldDto.setAccessible(true);
                field.set(entreprise, fieldDto.get(entrepriseDto));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }



        return entreprise;
    }

}
