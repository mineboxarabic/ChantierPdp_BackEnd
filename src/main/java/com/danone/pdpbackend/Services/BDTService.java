package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.entities.BDT.Bdt;
import com.danone.pdpbackend.entities.Signature;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BDTService extends Service<Bdt> {
    @Transactional
    Bdt findOrCreateBdtForDate(Long chantierId, LocalDate date /*, Optional<BdtInitialData> initialData */);

    Optional<Bdt> findBdtForChantierAndDate(Long chantierId, LocalDate date);

    DocumentStatus calculateBdtStatus(Long id);

    @Transactional
    Bdt updateAndSaveBdtStatus(Long bdtId);

    @Transactional
    default Bdt addSignature(Long bdtId, Signature signature, String type) {
        return null;
    }

    Optional<Bdt> findByChantierIdAndDate(Long id, LocalDate today);

    List<Bdt> findByChantierId(Long chantierId);
}