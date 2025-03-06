package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.Risque;

import java.util.List;

public interface RisqueService {
    List<Risque> getAllRisques();

    Risque getRisqueById(Long id);

    Risque createRisque(Risque risque);

    Risque updateRisque(Long id, Risque risqueDetails);

    Boolean deleteRisque(Long id);
}
