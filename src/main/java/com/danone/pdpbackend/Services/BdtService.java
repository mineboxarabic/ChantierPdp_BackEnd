package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.Bdt;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BdtService extends CommonDocumentServiceInterface<Bdt> {

    Optional<Bdt> findByChantierIdAndDate(Long id, LocalDate today);

    Optional<Bdt> findByChantierIdAndCreationDate(Long id, LocalDate today);

    List<Bdt> findByChantierId(Long chantierId);


}