package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.EntrepriseRepo;
import com.danone.pdpbackend.Services.EntrepriseService;
import com.danone.pdpbackend.entities.Entreprise;
import com.danone.pdpbackend.entities.Worker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class EntrepriseServiceImpl implements EntrepriseService {

    EntrepriseRepo entrepriseRepo;

    public EntrepriseServiceImpl(EntrepriseRepo entrepriseRepo) {
        this.entrepriseRepo = entrepriseRepo;
    }
    @Override
    public Entreprise findEntrepriseByNom(String danone) {
        return entrepriseRepo.findEntrepriseByNom(danone);
    }

    @Override
    public Entreprise getById(Long id) {
        try {
            return entrepriseRepo.findEntrepriseById(id);

        }catch (Exception e) {
            log.error("Error fetching entreprise: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Entreprise create(Entreprise entreprise) {
       return entrepriseRepo.save(entreprise);
    }

    @Override
    public Entreprise update(Long id, Entreprise newData) {
        Entreprise existing = entrepriseRepo.findEntrepriseById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Entreprise with id " + id + " not found");
        }
        return entrepriseRepo.save(newData);
    }

    @Override
    public List<Entreprise> getAll() {
        return entrepriseRepo.findAll();
    }

    @Override
    public Boolean delete(Long id) {
        Entreprise entreprise = entrepriseRepo.findEntrepriseById(id);
        if (entreprise == null) {
            return false;
        }
        entrepriseRepo.delete(entreprise);
        return true;
    }

    @Override
    public List<Entreprise> getByIds(List<Long> entrepriseIds) {
        //return entrepriseRepo.findAllById(entrepriseIds);
        try{
            List<Entreprise> entreprises = entrepriseRepo.findEntreprisesByIdIn(entrepriseIds);
            return entreprises;

        }catch (Exception e) {
            log.error("Error fetching entreprises: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<Worker> getWorkersByEntreprise(Long entrepriseId) {
        Entreprise entreprise = entrepriseRepo.findEntrepriseById(entrepriseId);
        if (entreprise == null) {
            throw new IllegalArgumentException("Entreprise with id " + entrepriseId + " not found");
        }
        return entreprise.getWorkers();
    }
}
