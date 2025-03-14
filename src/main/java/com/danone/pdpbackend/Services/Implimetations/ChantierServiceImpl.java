package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.ChantierRepo;
import com.danone.pdpbackend.Services.ChantierService;
import com.danone.pdpbackend.entities.Chantier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ChantierServiceImpl implements ChantierService {

    private final ChantierRepo chantierRepo;

    public ChantierServiceImpl(ChantierRepo chantierRepo) {
        this.chantierRepo = chantierRepo;
    }

    @Override
    public List<Chantier> getAllChantiers() {
        return chantierRepo.findAll();
    }

    @Override
    public Chantier updateChantier(Chantier updatedChantier, Long id) {
        Chantier existingChantier = chantierRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chantier with id " + id + " not found"));

/*        if (updatedChantier.getNom() != null) existingChantier.setNom(updatedChantier.getNom());
        if (updatedChantier.getEntrepriseExterieur() != null) existingChantier.setEntrepriseExterieur(updatedChantier.getEntrepriseExterieur());
        if (updatedChantier.getResponsable() != null) existingChantier.setResponsable(updatedChantier.getResponsable());
        if (updatedChantier.getLocalisation() != null) existingChantier.setLocalisation(updatedChantier.getLocalisation());
        if (updatedChantier.getBdts() != null) existingChantier.setBdts(updatedChantier.getBdts());
        if (updatedChantier.getPdp() != null) existingChantier.setPdp(updatedChantier.getPdp());
        if (updatedChantier.getDescription() != null) existingChantier.setDescription(updatedChantier.getDescription());

        */
        for(Field field : updatedChantier.getClass().getDeclaredFields()){
            field.setAccessible(true);
            try {
                Object value = field.get(updatedChantier);
                if(value != null){
                    field.set(existingChantier, value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return chantierRepo.save(existingChantier);
    }

    @Override
    public Chantier createChantier(Chantier chantier) {
        return chantierRepo.save(chantier);
    }

    @Override
    public Chantier getChantier(Long id) {
        return chantierRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chantier with id " + id + " not found"));
    }

    @Override
    public boolean deleteChantier(Long id) {
        Optional<Chantier> chantierOpt = chantierRepo.findById(id);
        if (chantierOpt.isEmpty()) {
            return false;
        }
        chantierRepo.deleteById(id);
        return true;
    }

    @Override
    public Long getLastId() {
        return (long) chantierRepo.findMaxId();
    }

    @Override
    public List<Chantier> getRecent() {
        List<Chantier> chantiers = chantierRepo.findAll();
        return chantiers.size() <= 10 ? chantiers : chantiers.subList(chantiers.size() - 10, chantiers.size());
    }
}
