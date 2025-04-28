package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.Entreprise;
import com.danone.pdpbackend.entities.Worker;

import java.util.List;


public interface EntrepriseService extends Service<Entreprise> {
    Entreprise findEntrepriseByNom(String danone);
    List<Worker> getWorkersByEntreprise(Long entrepriseId);
}
