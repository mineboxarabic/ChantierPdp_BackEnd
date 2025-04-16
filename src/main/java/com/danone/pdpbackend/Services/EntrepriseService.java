package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.dto.EntrepriseDTO;
import com.danone.pdpbackend.entities.Entreprise;
import com.danone.pdpbackend.entities.Worker;

import java.util.List;


public interface EntrepriseService {
    void create(Entreprise entrepriseDto);
    Entreprise findEntrepriseByNom(String danone);

    Long findMaxId();

    Entreprise getEntrepriseById(Long maxId);

    void createEntreprise(Entreprise entreprise);

    Entreprise createEntreprise(EntrepriseDTO entrepriseDto);

    Entreprise updateEntreprise(Entreprise entrepriseutilisatrice, Long id);
    Entreprise updateEntreprise(EntrepriseDTO entrepriseutilisatrice, Long id);

    List<Entreprise> findAll();

    boolean deleteEntreprise(Long id);

    List<Entreprise> getEntreprisesByIds(List<Long> entrepriseIds);

    List<Worker> getWorkersByEntreprise(Long entrepriseId);
}
