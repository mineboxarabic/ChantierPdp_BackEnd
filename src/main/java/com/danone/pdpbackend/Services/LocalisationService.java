package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.Localisation;
import java.util.List;

public interface LocalisationService {
    List<Localisation> getAllLocalisations();
    Localisation getLocalisationById(Long id);
    Localisation createLocalisation(Localisation localisation);
    Localisation updateLocalisation(Long id, Localisation localisationDetails);
    Boolean deleteLocalisation(Long id);
}
