package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.RisqueRepo;
import com.danone.pdpbackend.Services.RisqueService;
import com.danone.pdpbackend.entities.Risque;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

@Service
public class RisqueServiceImpl implements RisqueService {
    private final RisqueRepo risqueRepo;


    public RisqueServiceImpl(RisqueRepo risqueRepo) {
        this.risqueRepo = risqueRepo;

    }

    @Override
    public List<Risque> getAllRisques() {
        return risqueRepo.findAll();
    }

    @Override
    public Risque getRisqueById(Long id) {
        return risqueRepo.findRisqueById(id);
    }

    @Override
    public Risque createRisque(Risque risque) {
        return risqueRepo.save(risque);
    }

    @Override
    public Risque updateRisque(Long id, Risque risqueDetails) {

        Optional<Risque> risque = Optional.ofNullable(risqueRepo.findRisqueById(id));

        if (risque.isEmpty()) {
            return null;
        }

        for(Field field : Risque.class.getDeclaredFields()){
            field.setAccessible(true);
            try {
                Object value = field.get(risqueDetails);
                if(value != null){
                    field.set(risque.get(), value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return risqueRepo.save(risqueDetails);
    }

    @Override
    public Boolean deleteRisque(Long id) {
        //Risque risque = risqueRepo.findRisqueById(id);

        Optional<Risque> risque = Optional.ofNullable(risqueRepo.findRisqueById(id));

        if (risque.isEmpty()) {
            return false;
        }else{
            risqueRepo.deleteById((risque.get().getId()));
            return true;
        }
    }

    @Override
    public List<Risque> getRisquesByIds(List<Long> ids) {
        List<Risque> risques = risqueRepo.findRisqueByIdIn(ids);
        if (risques.isEmpty()) {
            return null;
        } else {
            return risques;
        }
    }
}
