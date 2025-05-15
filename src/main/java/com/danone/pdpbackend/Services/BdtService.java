package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.entities.Bdt;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BdtService extends CommonDocumentServiceInterface<Bdt> {
    @Transactional
    Bdt findOrCreateBdtForDate(Long chantierId, LocalDate date /*, Optional<BdtInitialData> initialData */);

    Optional<Bdt> findBdtForChantierAndDate(Long chantierId, LocalDate date);


    Optional<Bdt> findByChantierIdAndDate(Long id, LocalDate today);

    Optional<Bdt> findByChantierIdAndCreationDate(Long id, LocalDate today);

    List<Bdt> findByChantierId(Long chantierId);
}