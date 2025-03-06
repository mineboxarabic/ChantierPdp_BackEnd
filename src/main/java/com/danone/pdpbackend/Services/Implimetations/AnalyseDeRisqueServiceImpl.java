package com.danone.pdpbackend.Services.Implimetations;


import com.danone.pdpbackend.Repo.AnalyseDeRisqueRepo;
import com.danone.pdpbackend.Repo.ObjectAnswerEntreprisesRepo;
import com.danone.pdpbackend.Repo.RisqueRepo;
import com.danone.pdpbackend.Services.AnalyseDeRisqueService;
import com.danone.pdpbackend.dto.AnalyseDeRisqueDTO;
import com.danone.pdpbackend.entities.AnalyseDeRisque;
import com.danone.pdpbackend.entities.ObjectAnsweredEntreprises;
import com.danone.pdpbackend.entities.Risque;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

@Service
public class AnalyseDeRisqueServiceImpl implements AnalyseDeRisqueService {

    @Autowired
    private final AnalyseDeRisqueRepo analyseDeRisqueRepo;
    @Autowired
    private RisqueRepo risqueRepo;
    @Autowired
    private ObjectAnswerEntreprisesRepo objectAnswerEntreprisesRepo;

    public AnalyseDeRisqueServiceImpl(AnalyseDeRisqueRepo analyseDeRisqueRepo) {
        this.analyseDeRisqueRepo = analyseDeRisqueRepo;
    }

    @Override
    public List<AnalyseDeRisque> getAllAnalyseDeRisques() {
        return analyseDeRisqueRepo.findAll();
    }

    @Override
    public AnalyseDeRisque getAnalyseDeRisqueById(Long id) {
        return analyseDeRisqueRepo.findAnalyseDeRisqueById((id));
    }

    @Override
    public AnalyseDeRisque createAnalyseDeRisque(AnalyseDeRisqueDTO analyseDeRisqueDTO) {
        AnalyseDeRisque analyseDeRisque = new AnalyseDeRisque();

        analyseDeRisque.setId(analyseDeRisqueRepo.findMaxId() + 1L);
        analyseDeRisque.buildAnalyseDeRisque(analyseDeRisqueDTO);
        return analyseDeRisqueRepo.save(analyseDeRisque);
    }

    @Override
    public AnalyseDeRisque updateAnalyseDeRisque(Long id, AnalyseDeRisque analyseDeRisqueDetails) {
        Optional<AnalyseDeRisque> analyseDeRisque = Optional.ofNullable(analyseDeRisqueRepo.findAnalyseDeRisqueById(id));

        if (analyseDeRisque.isEmpty()) {
            return null;
        }

        for (Field field : AnalyseDeRisque.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(analyseDeRisqueDetails);
                if (value != null) {
                    field.set(analyseDeRisque.get(), value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return analyseDeRisqueRepo.save(analyseDeRisqueDetails);
    }

    @Override
    public Boolean deleteAnalyseDeRisque(Long id) {
        Optional<AnalyseDeRisque> analyseDeRisque = Optional.ofNullable(analyseDeRisqueRepo.findAnalyseDeRisqueById(id));

        if (analyseDeRisque.isEmpty()) {
            return false;
        } else {
            analyseDeRisqueRepo.deleteById(id);
            return true;
        }
    }

    @Override
    public ObjectAnsweredEntreprises addRisqueToAnalyse(Long analyseId, Long risqueId) {
        ObjectAnsweredEntreprises objectAnsweredEntreprises = new ObjectAnsweredEntreprises();
        objectAnsweredEntreprises.setEU(false);
        objectAnsweredEntreprises.setEE(false);

        AnalyseDeRisque analyseDeRisque = analyseDeRisqueRepo.findAnalyseDeRisqueById(analyseId);
        Risque risque = risqueRepo.findRisqueById(risqueId);

        objectAnsweredEntreprises.setAnalyseDeRisque(analyseDeRisque);
        analyseDeRisque.setRisque(risque);
        analyseDeRisqueRepo.save(analyseDeRisque);

        return objectAnswerEntreprisesRepo.save(objectAnsweredEntreprises);
    }

}