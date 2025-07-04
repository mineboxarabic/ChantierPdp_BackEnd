package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.LocalisationRepo;
import com.danone.pdpbackend.Services.LocalisationService;
import com.danone.pdpbackend.entities.Localisation;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class LocalisationServiceImpl implements LocalisationService {

    private final LocalisationRepo localisationRepo;

    public LocalisationServiceImpl(LocalisationRepo localisationRepo) {
        this.localisationRepo = localisationRepo;
    }

    @Override
    public List<Localisation> getAll() {
        return localisationRepo.findAll();
    }

    @Override
    public Localisation getById(Long id) {
        return localisationRepo.findById(id).orElse(null);
    }

    @Override
    public Localisation create(Localisation localisation) {
        return localisationRepo.save(localisation);
    }

    @Override
    public Localisation update(Long id, Localisation localisationDetails) {
        Optional<Localisation> localisationOptional = localisationRepo.findById(id);
        if (localisationOptional.isEmpty()) {
            return null;
        }

        Localisation localisation = localisationOptional.get();
        if (localisationDetails.getNom() != null) localisation.setNom(localisationDetails.getNom());
        if (localisationDetails.getCode() != null) localisation.setCode(localisationDetails.getCode());
        if (localisationDetails.getDescription() != null) localisation.setDescription(localisationDetails.getDescription());

        return localisationRepo.save(localisation);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if(!localisationRepo.existsById(id)){
            throw new EntityNotFoundException("Entreprise with id " + id + " not found");
        }
        localisationRepo.deleteById(id);
    }

    @Override
    public List<Localisation> getByIds(List<Long> ids) {
        return localisationRepo.findLocalisationsByIdIn(ids);
    }
}
