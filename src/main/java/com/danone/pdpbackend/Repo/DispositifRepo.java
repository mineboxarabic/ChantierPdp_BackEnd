package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.entities.Dispositif;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

@org.springframework.stereotype.Repository
public interface DispositifRepo  extends Repository<Dispositif, Long> {

    List<Dispositif> findAll();

    Dispositif findDispositifById(Long id);


    Dispositif save(Dispositif dispositif);

    boolean deleteById(Long id);
}
