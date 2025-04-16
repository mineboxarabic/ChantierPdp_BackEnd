package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.Chantier;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.ChantierDTO;

import java.util.List;

public interface ChantierService {

    List<Chantier> getAllChantiers();

    Chantier updateChantier(Chantier chantier, Long id);

    Chantier createChantier(Chantier chantier);

    Chantier getChantier(Long id);



    boolean deleteChantier(Long id);

    Long getLastId();

    List<Chantier> getRecent();


    void addPdpToChantier(Long chantierId, Pdp pdpId);

    List<Worker> getWorkersByChantier(Long chantierId);
}
