package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.Services.CommonDocumentServiceInterface; // Assuming a service to get Document by ID
import com.danone.pdpbackend.Services.DocumentService;
import com.danone.pdpbackend.Services.Implimetations.DocumentServiceImpl;
import com.danone.pdpbackend.Services.WorkerService; // Assuming a service to get Worker by ID
import com.danone.pdpbackend.entities.Document;
import com.danone.pdpbackend.entities.DocumentSignature;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.DocumentSignatureDTO;
import lombok.RequiredArgsConstructor; // Use Lombok for constructor injection
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor // Lombok annotation for constructor injection
public class DocumentSignatureMapper implements Mapper<DocumentSignatureDTO, DocumentSignature> {

    // Inject necessary services (ensure these services exist and have getById methods)
    private final CommonDocumentServiceInterface<Document> commonDocumentServiceInterface; // Use generic if applicable
    private final WorkerService workerService;
    private final DocumentService documentService;

    @Override
    public void setDTOFields(DocumentSignatureDTO dto, DocumentSignature entity) {
        if (dto == null || entity == null) {
            return;
        }
        dto.setId(entity.getId());
        dto.setSignatureDate(entity.getSignatureDate());
        dto.setSignatureVisual(entity.getSignatureVisual()); // Copy embedded object
      //  dto.setSignerRole(entity.getSignerRole());
        dto.setActive(entity.isActive());

        // Map related entities to their IDs
        if (entity.getDocument() != null) {
            dto.setDocumentId(entity.getDocument().getId());
        }
        if (entity.getWorker() != null) {
            dto.setWorkerId(entity.getWorker().getId());
            // Optionally set worker name for display
            dto.setWorkerName(entity.getWorker().getPrenom() + " " + entity.getWorker().getNom());
        }
    }

    @Override
    public void setEntityFields(DocumentSignatureDTO dto, DocumentSignature entity) {
        if (dto == null || entity == null) {
            return;
        }
        // ID is usually set by persistence context, avoid setting from DTO unless for update
        // entity.setId(dto.getId());
        entity.setSignatureDate(dto.getSignatureDate());
        entity.setSignatureVisual(dto.getSignatureVisual()); // Copy embedded object
        //entity.setSignerRole(dto.getSignerRole());
        entity.setActive(dto.isActive());

        // Fetch and set related entities from IDs
        if (dto.getDocumentId() != null) {
            // Only fetch if the document isn't already set or differs
            if (entity.getDocument() == null || !entity.getDocument().getId().equals(dto.getDocumentId())) {
                Document document = documentService.getById(dto.getDocumentId());
                if (document == null) {
                    throw new IllegalArgumentException("Document not found with ID: " + dto.getDocumentId());
                }
                entity.setDocument(document);
            }
        } else {
            entity.setDocument(null); // Explicitly set to null if ID is null
        }

        if (dto.getWorkerId() != null) {
            // Only fetch if the worker isn't already set or differs
            if (entity.getWorker() == null || !entity.getWorker().getId().equals(dto.getWorkerId())) {
                Worker worker = workerService.getById(dto.getWorkerId());
                if (worker == null) {
                    throw new IllegalArgumentException("Worker not found with ID: " + dto.getWorkerId());
                }
                entity.setWorker(worker);
            }
        } else {
            entity.setWorker(null); // Explicitly set to null if ID is null
        }
    }

    @Override
    public DocumentSignature toEntity(DocumentSignatureDTO dto) {
        if (dto == null) {
            return null;
        }
        DocumentSignature entity = new DocumentSignature();
        // ID might be null in DTO for creation
        entity.setId(dto.getId());
        setEntityFields(dto, entity); // Reuse logic
        return entity;
    }

    // Overload to associate with a parent document during mapping if needed
    public DocumentSignature toEntity(DocumentSignatureDTO dto, Document parentDocument) {
        if (dto == null) {
            return null;
        }
        DocumentSignature entity = toEntity(dto); // Base mapping
        entity.setDocument(parentDocument); // Set the parent
        return entity;
    }


    @Override
    public DocumentSignatureDTO toDTO(DocumentSignature entity) {
        if (entity == null) {
            return null;
        }
        DocumentSignatureDTO dto = new DocumentSignatureDTO();
        setDTOFields(dto, entity);
        return dto;
    }

    @Override
    public List<DocumentSignature> toEntityList(List<DocumentSignatureDTO> dtoList) {
        if (dtoList == null) {
            return Collections.emptyList();
        }
        return dtoList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    // Overload to associate with a parent document during list mapping
    public List<DocumentSignature> toEntityList(List<DocumentSignatureDTO> dtoList, Document parentDocument) {
        if (dtoList == null) {
            return Collections.emptyList();
        }
        return dtoList.stream()
                .map(dto -> toEntity(dto, parentDocument)) // Use overloaded toEntity
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentSignatureDTO> toDTOList(List<DocumentSignature> entityList) {
        if (entityList == null) {
            return Collections.emptyList();
        }
        return entityList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DocumentSignature updateEntityFromDTO(DocumentSignature entity, DocumentSignatureDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }
        // Use setEntityFields logic, but explicitly skip ID if necessary
        entity.setSignatureDate(dto.getSignatureDate());
        entity.setSignatureVisual(dto.getSignatureVisual());
     //   entity.setSignerRole(dto.getSignerRole());
        entity.setActive(dto.isActive());

        // Re-fetch/update relations only if IDs change
        setEntityFields(dto, entity); // This handles relation updates based on ID changes

        return entity;
    }

    @Override
    public DocumentSignatureDTO updateDTOFromEntity(DocumentSignatureDTO dto, DocumentSignature entity) {
        if (entity == null || dto == null) {
            return dto;
        }
        setDTOFields(dto, entity);
        return dto;
    }
}