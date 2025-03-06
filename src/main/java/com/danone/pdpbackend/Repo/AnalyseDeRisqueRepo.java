package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.entities.AnalyseDeRisque;
import com.danone.pdpbackend.entities.Dispositif;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface AnalyseDeRisqueRepo extends Repository<AnalyseDeRisque, Long> {
    List<AnalyseDeRisque> findAll();
    Optional<AnalyseDeRisque> findById(Long id);
    @Query("SELECT MAX(id) FROM analyse_de_risque")
    Long findMaxId();
    AnalyseDeRisque save(AnalyseDeRisque analyseDeRisque);
    boolean deleteById(Long id);

    AnalyseDeRisque findAnalyseDeRisqueById(Long id);
}