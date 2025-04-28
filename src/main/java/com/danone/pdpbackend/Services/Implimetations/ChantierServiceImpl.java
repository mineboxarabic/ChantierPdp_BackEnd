package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.ChantierRepo;
import com.danone.pdpbackend.Repo.EntrepriseRepo;
import com.danone.pdpbackend.Services.ChantierService;
import com.danone.pdpbackend.entities.Chantier;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.Worker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ChantierServiceImpl implements ChantierService {

    private final ChantierRepo chantierRepo;
    private final EntrepriseRepo entrepriseRepo;

    public ChantierServiceImpl(ChantierRepo chantierRepo, EntrepriseRepo entrepriseRepo) {
        this.chantierRepo = chantierRepo;
        this.entrepriseRepo = entrepriseRepo;
    }

    @Override
    public List<Chantier> getAll() {
        return chantierRepo.findAll();
    }

    public Chantier update(Long id, Chantier updatedChantier) {
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

        if(updatedChantier.getEntrepriseUtilisatrice() != null){
            Long entrepriseUtilisatriceId = updatedChantier.getEntrepriseUtilisatrice().getId();
            existingChantier.setEntrepriseUtilisatrice(entrepriseRepo.findEntrepriseById(entrepriseUtilisatriceId));
        }

        if(updatedChantier.getNbHeurs() != null) updatedChantier.setIsAnnuelle(updatedChantier.getNbHeurs() >= 400);

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
    public Chantier create(Chantier chantier) {
        return chantierRepo.save(chantier);
    }

    @Override
    public Chantier getById(Long id) {
        return chantierRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chantier with id " + id + " not found"));
    }

    @Override
    public Boolean delete(Long id) {
        Optional<Chantier> chantierOpt = chantierRepo.findById(id);
        if (chantierOpt.isEmpty()) {
            return false;
        }
        chantierRepo.deleteById(id);
        return true;
    }

    @Override
    public List<Chantier> getByIds(List<Long> ids) {
        List<Chantier> chantiers = chantierRepo.findChantiersByIdIn(ids);
        if (chantiers.isEmpty()) {
            throw new IllegalArgumentException("No chantiers found with the provided ids");
        }
        return chantiers;
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

    @Override
    public void addPdpToChantier(Long chantierId, Pdp pdp) {
        Chantier chantier = chantierRepo.findById(chantierId)
                .orElseThrow(() -> new IllegalArgumentException("Chantier with id " + chantierId + " not found"));
        chantier.getPdps().add(pdp);
        chantierRepo.save(chantier);
    }

    @Override
    public List<Worker> getWorkersByChantier(Long chantierId) {
        Chantier chantier = chantierRepo.findById(chantierId)
                .orElseThrow(() -> new IllegalArgumentException("Chantier with id " + chantierId + " not found"));
        return chantier.getWorkers();
    }
}
