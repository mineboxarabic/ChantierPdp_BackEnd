package com.danone.pdpbackend.Repo;


import com.danone.pdpbackend.entities.Risque;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Collection;
import java.util.List;

@org.springframework.stereotype.Repository
public interface RisqueRepo extends Repository<Risque,Long> {

        Risque save(Risque risque);

        Boolean delete(Risque risque);

        Risque findRisqueById(Long id);

        List<Risque> findAll();

        @Query("SELECT MAX(id) FROM risque ")
        Long findMaxId();

        void deleteById(Long id);

        List<Risque> findRisqueByIdIn(Collection<Long> ids);

        Long count();

    boolean existsById(Long id);

    List<Risque> findRisqueByTravailleDangereuxIsTrueAndPermitIdIsNull();
}
