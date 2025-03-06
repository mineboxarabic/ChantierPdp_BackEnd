package com.danone.pdpbackend.Repo;


import com.danone.pdpbackend.entities.Localisation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface LocalisationRepo extends Repository<Localisation, Long> {

    //Crud
    Localisation save(Localisation localisation);
    Optional<Localisation> findById(Long id);
    boolean deleteById(Long id);
    List<Localisation> findAll();

    @Query("SELECT MAX(id) FROM localisation")
    Long findMaxId();

    Localisation findLocalisationById(Long id);

    boolean existsById(Long id);
}
