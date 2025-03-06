package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.entities.Entreprise;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

@org.springframework.stereotype.Repository
public interface EntrepriseRepo extends Repository<Entreprise, Integer> {

    Entreprise save(Entreprise entreprise);

    void delete(Entreprise entreprise);

    List<Entreprise> findAll();


    @Query("SELECT MAX(id) FROM entreprise")
    Long findMaxId();


    Entreprise findEntrepriseByNom(String danone);

    Entreprise findEntrepriseById(Long id);
}
