package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.dto.EntrepriseDTO;
import com.danone.pdpbackend.entities.Entreprise;

import java.util.List;


public interface EntrepriseService {
    void create(Entreprise entrepriseDto);
    Entreprise findEntrepriseByNom(String danone);

    Long findMaxId();

    Entreprise findEntrepriseById(Long maxId);

    void createEntreprise(Entreprise entreprise);

    Entreprise createEntreprise(EntrepriseDTO entrepriseDto);

    Entreprise updateEntreprise(Entreprise entrepriseutilisatrice, Long id);
    Entreprise updateEntreprise(EntrepriseDTO entrepriseutilisatrice, Long id);

    List<Entreprise> findAll();

    boolean deleteEntreprise(Long id);
}
