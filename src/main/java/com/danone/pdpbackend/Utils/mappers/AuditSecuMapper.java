package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.entities.AuditSecu;
import com.danone.pdpbackend.entities.dto.AuditSecuDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuditSecuMapper implements Mapper<AuditSecuDTO, AuditSecu> {

    @Override
    public AuditSecuDTO toDTO(AuditSecu auditSecu) {
        if (auditSecu == null) {
            return null;
        }
        AuditSecuDTO auditSecuDTO = new AuditSecuDTO();
        setDTOFields(auditSecuDTO, auditSecu);
        return auditSecuDTO;
    }

    @Override
    public void setDTOFields(AuditSecuDTO auditSecuDTO, AuditSecu auditSecu) {
        if (auditSecuDTO == null || auditSecu == null) return;

        auditSecuDTO.setId(auditSecu.getId());
        auditSecuDTO.setTitle(auditSecu.getTitle());
        auditSecuDTO.setDescription(auditSecu.getDescription());
        auditSecuDTO.setLogo(auditSecu.getLogo());
        auditSecuDTO.setTypeOfAudit(auditSecu.getTypeOfAudit());
    }

    @Override
    public AuditSecu toEntity(AuditSecuDTO auditSecuDTO) {
        if (auditSecuDTO == null) {
            return null;
        }
        AuditSecu auditSecu = new AuditSecu();
        setEntityFields(auditSecuDTO, auditSecu);
        return auditSecu;
    }

    @Override
    public void setEntityFields(AuditSecuDTO auditSecuDTO, AuditSecu auditSecu) {
        if (auditSecu == null || auditSecuDTO == null) return;

        auditSecu.setId(auditSecuDTO.getId());
        auditSecu.setTitle(auditSecuDTO.getTitle());
        auditSecu.setDescription(auditSecuDTO.getDescription());
        auditSecu.setLogo(auditSecuDTO.getLogo());
        auditSecu.setTypeOfAudit(auditSecuDTO.getTypeOfAudit());
    }

    @Override
    public List<AuditSecu> toEntityList(List<AuditSecuDTO> dtoList) {
        if (dtoList == null) {
            return List.of();
        }
        return dtoList.stream()
                .map(this::toEntity)
                .toList();
    }

    @Override
    public List<AuditSecuDTO> toDTOList(List<AuditSecu> entityList) {
        if (entityList == null) {
            return List.of();
        }
        return entityList.stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public AuditSecu updateEntityFromDTO(AuditSecu entity, AuditSecuDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }
        setEntityFields(dto, entity);
        return entity;
    }

    @Override
    public AuditSecuDTO updateDTOFromEntity(AuditSecuDTO dto, AuditSecu entity) {
        if (dto == null || entity == null) {
            return dto;
        }
        setDTOFields(dto, entity);
        return dto;
    }
}
