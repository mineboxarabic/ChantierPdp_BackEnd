package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.entities.AnalyseDeRisque;
import com.danone.pdpbackend.entities.dto.AnalyseDeRisqueDTO;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;
import com.danone.pdpbackend.entities.AnalyseDeRisque;
import com.danone.pdpbackend.entities.dto.AnalyseDeRisqueDTO;
import org.springframework.stereotype.Component; // Import @Component

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component // Make this class a Spring Bean
public class AnalyseDeRisqueMapper implements Mapper<AnalyseDeRisqueDTO, AnalyseDeRisque> {

    @Override
    public void setDTOFields(AnalyseDeRisqueDTO dto, AnalyseDeRisque entity) {
        if (dto == null || entity == null) {
            return;
        }
        dto.setId(entity.getId());
        dto.setDeroulementDesTaches(entity.getDeroulementDesTaches());
        dto.setMoyensUtilises(entity.getMoyensUtilises());
        dto.setRisque(entity.getRisque()); // Assuming direct object copy is okay
        dto.setMesuresDePrevention(entity.getMesuresDePrevention());
    }

    @Override
    public void setEntityFields(AnalyseDeRisqueDTO dto, AnalyseDeRisque entity) {
        if (dto == null || entity == null) {
            return;
        }
        // Note: We usually don't set the ID from DTO during creation/update this way
        // entity.setId(dto.getId()); // Typically ID is generated or comes from path param
        entity.setDeroulementDesTaches(dto.getDeroulementDesTaches());
        entity.setMoyensUtilises(dto.getMoyensUtilises());
        entity.setRisque(dto.getRisque()); // Assuming direct object copy is okay
        entity.setMesuresDePrevention(dto.getMesuresDePrevention());

        // If the entity has the buildAnalyseDeRisque method, you might call it here
        // entity.buildAnalyseDeRisque(dto);
    }

    @Override
    public AnalyseDeRisque toEntity(AnalyseDeRisqueDTO dto) {
        if (dto == null) {
            return null;
        }
        AnalyseDeRisque entity = new AnalyseDeRisque();
        // Set fields for the new entity based on DTO
        // entity.setId(dto.getId()); // <<<--- REMOVE OR COMMENT OUT THIS LINE
        entity.setDeroulementDesTaches(dto.getDeroulementDesTaches());
        entity.setMoyensUtilises(dto.getMoyensUtilises());
        entity.setRisque(dto.getRisque());
        entity.setMesuresDePrevention(dto.getMesuresDePrevention());
        return entity;
    }

    @Override
    public AnalyseDeRisqueDTO toDTO(AnalyseDeRisque entity) {
        if (entity == null) {
            return null;
        }
        AnalyseDeRisqueDTO dto = new AnalyseDeRisqueDTO();
        setDTOFields(dto, entity); // Reuse the logic
        return dto;
    }

    @Override
    public List<AnalyseDeRisque> toEntityList(List<AnalyseDeRisqueDTO> dtoList) {
        if (dtoList == null) {
            return Collections.emptyList();
        }
        return dtoList.stream()
                .map(this::toEntity) // Use method reference
                .collect(Collectors.toList());
    }

    @Override
    public List<AnalyseDeRisqueDTO> toDTOList(List<AnalyseDeRisque> entityList) {
        if (entityList == null) {
            return Collections.emptyList();
        }
        return entityList.stream()
                .map(this::toDTO) // Use method reference
                .collect(Collectors.toList());
    }

    @Override
    public AnalyseDeRisque updateEntityFromDTO(AnalyseDeRisque entity, AnalyseDeRisqueDTO dto) {
        if (entity == null || dto == null) {
            return entity; // Or handle as error depending on logic
        }
        // Don't update the ID
        entity.setDeroulementDesTaches(dto.getDeroulementDesTaches());
        entity.setMoyensUtilises(dto.getMoyensUtilises());
        entity.setRisque(dto.getRisque());
        entity.setMesuresDePrevention(dto.getMesuresDePrevention());
        return entity;
    }

    @Override
    public AnalyseDeRisqueDTO updateDTOFromEntity(AnalyseDeRisqueDTO dto, AnalyseDeRisque entity) {
        if (entity == null || dto == null) {
            return dto; // Or handle as error
        }
        setDTOFields(dto, entity); // Reuse the logic
        return dto;
    }
}