package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.Dispositif;
import org.springframework.stereotype.Service;

import java.util.List;


public interface DispositifService {
    List<Dispositif> getAllDispositifs();

    Dispositif getDispositifById(Long id);

    Dispositif createDispositif(Dispositif dispositif);

    Dispositif updateDispositif(Long id, Dispositif dispositifDetails);

    boolean deleteDispositif(Long id);
}
