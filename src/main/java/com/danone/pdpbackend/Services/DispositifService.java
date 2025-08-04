package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.dto.DispositifDTO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface DispositifService {
    List<DispositifDTO> getAllDispositifs();

    DispositifDTO getDispositifById(Long id);

    DispositifDTO createDispositif(DispositifDTO dispositifDTO);

    DispositifDTO updateDispositif(Long id, DispositifDTO dispositifDetailsDTO);

    boolean deleteDispositif(Long id);

    List<DispositifDTO> getDispositifsByIds(List<Long> ids);
}