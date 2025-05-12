package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.entities.Bdt;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BDTRepo extends org.springframework.data.repository.Repository<Bdt, Long> {


    List<Bdt> findAll();
    Optional<Bdt> findById(Long id);
    Bdt save(Bdt bdt);
    Boolean existsById(Long id);

    void deleteById(Long id);

    List<Bdt> findBDTsByIdIn(List<Long> ids);

    List<Bdt> findBDTByChantierIdAndDate(Long chantierId, LocalDate date);
    List<Bdt> findBDTByChantierIdAndCreationDate(Long chantierId, LocalDate date);

    List<Bdt> findBDTByChantierId(Long chantierId);

}