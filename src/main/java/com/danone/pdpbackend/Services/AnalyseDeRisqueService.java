package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.dto.AnalyseDeRisqueDTO;
import com.danone.pdpbackend.entities.AnalyseDeRisque;
import com.danone.pdpbackend.entities.ObjectAnsweredEntreprises;

import java.util.List;

public interface AnalyseDeRisqueService {
    List<AnalyseDeRisque> getAllAnalyseDeRisques();
    AnalyseDeRisque getAnalyseDeRisqueById(Long id);
    AnalyseDeRisque createAnalyseDeRisque(AnalyseDeRisqueDTO analyseDeRisque);
    AnalyseDeRisque updateAnalyseDeRisque(Long id, AnalyseDeRisque analyseDeRisqueDetails);
    Boolean deleteAnalyseDeRisque(Long id);

    ObjectAnsweredEntreprises addRisqueToAnalyse(Long analyseId, Long risqueId);
}