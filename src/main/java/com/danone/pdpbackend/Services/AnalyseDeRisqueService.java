package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.AnalyseDeRisque;

import java.util.List;

public interface AnalyseDeRisqueService {
    List<AnalyseDeRisque> getAllAnalyseDeRisques();
    AnalyseDeRisque getAnalyseDeRisqueById(Long id);
    AnalyseDeRisque createAnalyseDeRisque(AnalyseDeRisque analyseDeRisque);
    AnalyseDeRisque updateAnalyseDeRisque(Long id, AnalyseDeRisque analyseDeRisqueDetails);
    Boolean deleteAnalyseDeRisque(Long id);

    AnalyseDeRisque addRisqueToAnalyse(Long analyseId, Long risqueId);
}