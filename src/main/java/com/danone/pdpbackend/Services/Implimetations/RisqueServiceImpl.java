package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.RisqueRepo;
import com.danone.pdpbackend.Services.RisqueService;
import com.danone.pdpbackend.entities.Risque;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

@Service
public class RisqueServiceImpl implements RisqueService {
    private static final Logger log = LoggerFactory.getLogger(RisqueServiceImpl.class);
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


        log.info("Creating new Risque: {}", risque);
        Risque savedRisque = risqueRepo.save(risque);
        log.info("Risque created with PermitType: {}", savedRisque.getPermitType());
        return savedRisque;
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

        return risqueRepo.save(risque.get()); // Fix: save the updated entity, not risqueDetails
    }

    @Override
    @Transactional
    public void deleteRisque(Long id) {


        if(!risqueRepo.existsById(id)){
            throw new EntityNotFoundException("Risque with id " + id + " not found");
        }
        risqueRepo.deleteById(id);

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
