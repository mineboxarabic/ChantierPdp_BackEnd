package com.danone.pdpbackend.Services.Implimetations;


import com.danone.pdpbackend.Repo.AnalyseDeRisqueRepo;
import com.danone.pdpbackend.Repo.ObjectAnswerEntreprisesRepo;
import com.danone.pdpbackend.Repo.RisqueRepo;
import com.danone.pdpbackend.Services.AnalyseDeRisqueService;
import com.danone.pdpbackend.entities.AnalyseDeRisque;
import com.danone.pdpbackend.entities.Risque;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public List<AnalyseDeRisque> getAll() {
        return analyseDeRisqueRepo.findAll();
    }

    @Override
    public AnalyseDeRisque getById(Long id) {
        return analyseDeRisqueRepo.findAnalyseDeRisqueById((id));
    }

    @Override
    public AnalyseDeRisque create(AnalyseDeRisque analyseDeRisqueDTO) {

        return analyseDeRisqueRepo.save(analyseDeRisqueDTO);
    }

    @Override
    public AnalyseDeRisque update(Long id, AnalyseDeRisque analyseDeRisqueDetails) {
        return analyseDeRisqueRepo.save(analyseDeRisqueDetails);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if(!analyseDeRisqueRepo.existsById(id)){
            throw new EntityNotFoundException("Analyse De RisqueRepo with id " + id + " not found");
        }
        analyseDeRisqueRepo.deleteById(id);
    }


    @Override
    public AnalyseDeRisque addRisqueToAnalyse(Long analyseId, Long risqueId) {
        AnalyseDeRisque analyseDeRisque = analyseDeRisqueRepo.findAnalyseDeRisqueById(analyseId);
        Risque risque = risqueRepo.findRisqueById(risqueId);

        analyseDeRisque.setRisque(risque);
        return analyseDeRisqueRepo.save(analyseDeRisque);
    }

    @Override
    public List<AnalyseDeRisque> getByIds(List<Long> ids){
        return analyseDeRisqueRepo.findAnalyseDeRisqueByIdIn(ids);
    }
}