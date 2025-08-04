package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.entities.Dispositif;
import com.danone.pdpbackend.entities.dto.DispositifDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DispositifMapper {

    DispositifMapper INSTANCE = Mappers.getMapper(DispositifMapper.class);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "logo", target = "logo")
    @Mapping(source = "type", target = "type")
    DispositifDTO toDto(Dispositif dispositif);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "logo", target = "logo")
    @Mapping(source = "type", target = "type")
    Dispositif toEntity(DispositifDTO dispositifDTO);
}
