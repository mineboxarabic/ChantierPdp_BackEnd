package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.entities.Chantier;
import com.danone.pdpbackend.entities.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface ChantierRepo extends Repository<Chantier, Integer> {

    Chantier save(Chantier chantier);

    void delete(Chantier chantier);

    List<Chantier> findAll();

    Optional<Chantier> findById(Long id);

    @Query("SELECT MAX(id) FROM Chantier")
    int findMaxId();

    void deleteById(Long id);

    List<Chantier> getChantierById(Long id);

    List<Chantier> findAllByDonneurDOrdre(User user);

    List<Chantier> findChantiersByIdIn(List<Long> ids);
}
