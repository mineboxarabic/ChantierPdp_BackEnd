package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.entities.Pdp;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface PdpRepo extends Repository<Pdp, Integer> {

    Pdp save(Pdp pdp);

    void delete(Pdp pdp);

    List<Pdp>  findAll();


    Optional<Pdp> findById(Long id);

    @Query("SELECT MAX(id) FROM pdp")
    int findMaxId();

    void deleteById(Long id);

    List<Pdp> findPdpsByIdIn(Collection<Long> ids);
}
