package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.Chantier;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.ChantierDTO;

import java.util.List;

public interface ChantierService extends Service<Chantier> {

    Long getLastId();

    List<Chantier> getRecent();


    void addPdpToChantier(Long chantierId, Pdp pdpId);

    List<Worker> getWorkersByChantier(Long chantierId);
}
