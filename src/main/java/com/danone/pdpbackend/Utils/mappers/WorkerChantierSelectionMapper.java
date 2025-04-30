package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.Services.ChantierService;
import com.danone.pdpbackend.Services.UserService;
import com.danone.pdpbackend.Services.WorkerService;
import com.danone.pdpbackend.entities.Chantier;
import com.danone.pdpbackend.entities.User;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.WorkerChantierSelection;
import com.danone.pdpbackend.entities.dto.WorkerChantierSelectionDTO;
import lombok.RequiredArgsConstructor; // Use Lombok for constructor injection
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component // Make this class a Spring Bean
@RequiredArgsConstructor // Lombok annotation for constructor injection of final fields
public class WorkerChantierSelectionMapper implements Mapper<WorkerChantierSelectionDTO, WorkerChantierSelection> {

    // Inject services needed to fetch related entities by ID
    private final WorkerService workerService;
    private final ChantierService chantierService;
    private final UserService userService; // Assuming you have a UserService

    @Override
    public void setDTOFields(WorkerChantierSelectionDTO dto, WorkerChantierSelection entity) {
        if (dto == null || entity == null) {
            return;
        }
        dto.setId(entity.getId());
        dto.setSelectionDate(entity.getSelectionDate());
        dto.setIsSelected(entity.getIsSelected());
        dto.setSelectionNote(entity.getSelectionNote());

        // Map related entities to their IDs
        dto.setWorker(entity.getWorker() != null ? entity.getWorker().getId() : null);
        dto.setChantier(entity.getChantier() != null ? entity.getChantier().getId() : null);
        dto.setSelectedBy(entity.getSelectedBy() != null ? entity.getSelectedBy().getId() : null);
    }

    @Override
    public void setEntityFields(WorkerChantierSelectionDTO dto, WorkerChantierSelection entity) {
        if (dto == null || entity == null) {
            return;
        }
        // Usually, ID is not set from DTO directly unless it's an update scenario
        // entity.setId(dto.getId());
        entity.setSelectionDate(dto.getSelectionDate());
        entity.setIsSelected(dto.getIsSelected());
        entity.setSelectionNote(dto.getSelectionNote());

        // Fetch related entities using injected services based on IDs from DTO
        // Handle potential null IDs and cases where entities are not found
        if (dto.getWorker() != null) {
            Worker worker = workerService.getById(dto.getWorker());
            // Consider throwing an exception or logging if worker is null but ID wasn't
            entity.setWorker(worker);
        } else {
            entity.setWorker(null);
        }

        if (dto.getChantier() != null) {
            Chantier chantier = chantierService.getById(dto.getChantier());
            entity.setChantier(chantier);
        } else {
            entity.setChantier(null);
        }

        if (dto.getSelectedBy() != null) {
            User user = userService.getUserById(dto.getSelectedBy()); // Use correct method from UserService
            entity.setSelectedBy(user);
        } else {
            entity.setSelectedBy(null);
        }
    }

    @Override
    public WorkerChantierSelection toEntity(WorkerChantierSelectionDTO dto) {
        if (dto == null) {
            return null;
        }
        WorkerChantierSelection entity = new WorkerChantierSelection();
        // ID might be null in DTO for creation
        entity.setId(dto.getId());
        setEntityFields(dto, entity); // Reuse logic, but be mindful of ID handling if needed
        return entity;
    }

    @Override
    public WorkerChantierSelectionDTO toDTO(WorkerChantierSelection entity) {
        if (entity == null) {
            return null;
        }
        WorkerChantierSelectionDTO dto = new WorkerChantierSelectionDTO();
        setDTOFields(dto, entity);
        return dto;
    }

    @Override
    public List<WorkerChantierSelection> toEntityList(List<WorkerChantierSelectionDTO> dtoList) {
        if (dtoList == null) {
            return Collections.emptyList();
        }
        return dtoList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkerChantierSelectionDTO> toDTOList(List<WorkerChantierSelection> entityList) {
        if (entityList == null) {
            return Collections.emptyList();
        }
        return entityList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public WorkerChantierSelection updateEntityFromDTO(WorkerChantierSelection entity, WorkerChantierSelectionDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }
        // Use setEntityFields logic, but explicitly skip ID
        entity.setSelectionDate(dto.getSelectionDate());
        entity.setIsSelected(dto.getIsSelected());
        entity.setSelectionNote(dto.getSelectionNote());

        if (dto.getWorker() != null) {
            if (entity.getWorker() == null || !dto.getWorker().equals(entity.getWorker().getId())) {
                entity.setWorker(workerService.getById(dto.getWorker()));
            }
        } else {
            entity.setWorker(null);
        }

        if (dto.getChantier() != null) {
            if (entity.getChantier() == null || !dto.getChantier().equals(entity.getChantier().getId())) {
                entity.setChantier(chantierService.getById(dto.getChantier()));
            }
        } else {
            entity.setChantier(null);
        }

        if (dto.getSelectedBy() != null) {
            if (entity.getSelectedBy() == null || !dto.getSelectedBy().equals(entity.getSelectedBy().getId())) {
                entity.setSelectedBy(userService.getUserById(dto.getSelectedBy()));
            }
        } else {
            entity.setSelectedBy(null);
        }

        return entity;
    }

    @Override
    public WorkerChantierSelectionDTO updateDTOFromEntity(WorkerChantierSelectionDTO dto, WorkerChantierSelection entity) {
        if (entity == null || dto == null) {
            return dto;
        }
        setDTOFields(dto, entity);
        return dto;
    }
}