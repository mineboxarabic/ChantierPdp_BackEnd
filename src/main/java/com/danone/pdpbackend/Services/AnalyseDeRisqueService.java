package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.AnalyseDeRisque;

import java.util.List;

public interface AnalyseDeRisqueService extends Service<AnalyseDeRisque>{
    List<AnalyseDeRisque> getAll();
    AnalyseDeRisque getById(Long id);
    AnalyseDeRisque create(AnalyseDeRisque analyseDeRisque);
    AnalyseDeRisque update(Long id, AnalyseDeRisque analyseDeRisqueDetails);

    AnalyseDeRisque addRisqueToAnalyse(Long analyseId, Long risqueId);
}