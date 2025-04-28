package com.danone.pdpbackend.Utils.mappers;

import java.util.List;

public interface Mapper<DTO, ENTITY>
{
    void setDTOFields(DTO dto, ENTITY entity);
    void setEntityFields(DTO dto, ENTITY entity);
    ENTITY toEntity(DTO dto);
    DTO toDTO(ENTITY entity);
    List<ENTITY> toEntityList(List<DTO> dtoList);
    List<DTO> toDTOList(List<ENTITY> entityList);
    ENTITY updateEntityFromDTO (ENTITY entity, DTO dto);
    DTO updateDTOFromEntity (DTO dto, ENTITY entity);


}