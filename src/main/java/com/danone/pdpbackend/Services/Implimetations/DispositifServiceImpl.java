package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.DispositifRepo;
import com.danone.pdpbackend.Services.DispositifService;
import com.danone.pdpbackend.entities.Dispositif;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class DispositifServiceImpl implements DispositifService {


    private final DispositifRepo dispositifRepo;

    public DispositifServiceImpl(DispositifRepo dispositifRepo) {
        this.dispositifRepo = dispositifRepo;
    }

    @Override
    public List<Dispositif> getAllDispositifs() {
        return dispositifRepo.findAll();
    }

    @Override
    public Dispositif getDispositifById(Long id) {
        return dispositifRepo.findDispositifById(id);
    }

    @Override
    public Dispositif createDispositif(Dispositif dispositif) {
        return dispositifRepo.save(dispositif);
    }

    @Override
    public Dispositif updateDispositif(Long id, Dispositif dispositifDetails) {
        return null;
    }

    @Override
    public boolean deleteDispositif(Long id) {
        return dispositifRepo.deleteById(id);
    }

    @Override
    public List<Dispositif> getDispositifsByIds(List<Long> ids) {
        return dispositifRepo.findDispositifByIdIn(ids);
    }


}
