package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.Localisation;
import java.util.List;

public interface LocalisationService extends Service<Localisation> {
    List<Localisation> getAll();
    Localisation getById(Long id);
    Localisation create(Localisation localisation);
    Localisation update(Long id, Localisation localisationDetails);
}
