package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.entities.Chantier;
import com.danone.pdpbackend.entities.Localisation;
import com.danone.pdpbackend.entities.dto.ChantierDTO;
import com.danone.pdpbackend.entities.dto.LocalisationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class LocalisationMapper {


    public abstract Localisation toEntity(LocalisationDTO dto);
    public abstract LocalisationDTO toDTO(Localisation localisation);

    @Mapping(target = "id" , ignore = true)
    public abstract void updateEntity(LocalisationDTO dto, @MappingTarget Localisation localisation);
    @Mapping(target = "id" , ignore = true)
    public abstract void updateDTO(Localisation localisation, @MappingTarget LocalisationDTO dto);

    public abstract List<LocalisationDTO> toDTOList(List<Localisation> localisations);
    public abstract List<Localisation> toEntityList(List<LocalisationDTO> dtos);


}
