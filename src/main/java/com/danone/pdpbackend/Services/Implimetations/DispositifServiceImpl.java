package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.DispositifRepo;
import com.danone.pdpbackend.Services.DispositifService;
import com.danone.pdpbackend.Utils.mappers.DispositifMapper;
import com.danone.pdpbackend.entities.Dispositif;
import com.danone.pdpbackend.entities.dto.DispositifDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DispositifServiceImpl implements DispositifService {

    private final DispositifRepo dispositifRepo;
    private final DispositifMapper dispositifMapper;

    @Autowired
    public DispositifServiceImpl(DispositifRepo dispositifRepo, DispositifMapper dispositifMapper) {
        this.dispositifRepo = dispositifRepo;
        this.dispositifMapper = dispositifMapper;
    }

    @Override
    public List<DispositifDTO> getAllDispositifs() {
        return dispositifRepo.findAll().stream()
                .map(dispositifMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public DispositifDTO getDispositifById(Long id) {
        Dispositif dispositif = dispositifRepo.findDispositifById(id);
        return dispositif != null ? dispositifMapper.toDto(dispositif) : null;
    }

    @Override
    public DispositifDTO createDispositif(DispositifDTO dispositifDTO) {
        Dispositif dispositif = dispositifMapper.toEntity(dispositifDTO);
        Dispositif savedDispositif = dispositifRepo.save(dispositif);
        return dispositifMapper.toDto(savedDispositif);
    }

    @Override
    public DispositifDTO updateDispositif(Long id, DispositifDTO dispositifDetailsDTO) {
        Dispositif existingDispositif = dispositifRepo.findDispositifById(id);
        if (existingDispositif == null) {
            throw new RuntimeException("Dispositif not found with id " + id);
        }
        // Update fields from DTO to entity
        existingDispositif.setTitle(dispositifDetailsDTO.getTitle());
        existingDispositif.setDescription(dispositifDetailsDTO.getDescription());
        existingDispositif.setLogo(dispositifDetailsDTO.getLogo());
        existingDispositif.setType(dispositifDetailsDTO.getType());

        Dispositif updatedDispositif = dispositifRepo.save(existingDispositif);
        return dispositifMapper.toDto(updatedDispositif);
    }

    @Override
    public boolean deleteDispositif(Long id) {
        return dispositifRepo.deleteById(id);
    }

    @Override
    public List<DispositifDTO> getDispositifsByIds(List<Long> ids) {
        return dispositifRepo.findDispositifByIdIn(ids).stream()
                .map(dispositifMapper::toDto)
                .collect(Collectors.toList());
    }
}