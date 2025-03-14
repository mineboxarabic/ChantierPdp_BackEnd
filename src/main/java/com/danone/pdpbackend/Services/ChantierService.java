package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.Chantier;

import java.util.List;

public interface ChantierService {

    List<Chantier> getAllChantiers();

    Chantier updateChantier(Chantier chantier, Long id);

    Chantier createChantier(Chantier chantier);

    Chantier getChantier(Long id);

    boolean deleteChantier(Long id);

    Long getLastId();

    List<Chantier> getRecent();
}
