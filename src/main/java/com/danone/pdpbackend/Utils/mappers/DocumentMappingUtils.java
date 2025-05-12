package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.Services.EntrepriseService;
import com.danone.pdpbackend.entities.Document;
import com.danone.pdpbackend.entities.DocumentSignature;
import com.danone.pdpbackend.entities.ObjectAnswered;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.dto.DocumentDTO;
import com.danone.pdpbackend.entities.dto.DocumentSignatureDTO;
import com.danone.pdpbackend.entities.dto.ObjectAnsweredDTO;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class containing static methods for mapping common Document fields.
 */
public class DocumentMappingUtils {

    /**
     * Maps common fields from a Document entity to a DocumentDTO.
     *
     * @param source                  The source Document entity.
     * @param target                  The target DocumentDTO.
     * @param objectAnsweredMapper    Mapper for ObjectAnswered.
     * @param documentSignatureMapper Mapper for DocumentSignature.
     */
    public static void mapEntityToDtoBase(
            Document source,
            DocumentDTO target,
            ObjectAnsweredMapper objectAnsweredMapper,
            DocumentSignatureMapper documentSignatureMapper) {

        if (source == null || target == null) return;

        target.setId(source.getId());
        target.setStatus(source.getStatus());
        target.setDate(source.getDate());
        target.setCreationDate(source.getCreationDate());

        if (source.getEntrepriseExterieure() != null) {
            target.setEntrepriseExterieure(source.getEntrepriseExterieure().getId());
        } else {
            target.setEntrepriseExterieure(null);
        }

        // Use injected mappers for lists
        target.setRelations(objectAnsweredMapper.toDTOList(source.getRelations()));
        target.setSignatures(documentSignatureMapper.toDTOList(source.getSignatures()));
    }

    /**
     * Maps common fields from a DocumentDTO to a Document entity.
     *
     * @param source                  The source DocumentDTO.
     * @param target                  The target Document entity.
     * @param entrepriseService       Service to fetch Entreprise by ID.
     * @param objectAnsweredMapper    Mapper for ObjectAnswered.
     * @param documentSignatureMapper Mapper for DocumentSignature.
     */
    public static void mapDtoToEntityBase(
            DocumentDTO source,
            Document target,
            EntrepriseService entrepriseService,
            ObjectAnsweredMapper objectAnsweredMapper,
            DocumentSignatureMapper documentSignatureMapper) {

        if (source == null || target == null) return;

        // ID is usually managed by JPA, don't map it directly unless updating
        // target.setId(source.getId());
        target.setStatus(source.getStatus());
        target.setDate(source.getDate());
        // target.setCreationDate(source.getCreationDate()); // Usually set automatically

        // Map Entreprise relation
        if (source.getEntrepriseExterieure() != null) {
            // Fetch only if the ID has changed or wasn't set
            if (target.getEntrepriseExterieure() == null || !target.getEntrepriseExterieure().getId().equals(source.getEntrepriseExterieure())) {
                target.setEntrepriseExterieure(entrepriseService.getById(source.getEntrepriseExterieure()));
            }
        } else {
            target.setEntrepriseExterieure(null);
        }

        // Use helper methods to update collections (important for relationship management)
        updateObjectAnsweredCollection(target, target.getRelations(), objectAnsweredMapper.toEntityList(source.getRelations(), (Pdp) target));
        updateDocumentSignatureCollection(target, target.getSignatures(), documentSignatureMapper.toEntityList(source.getSignatures(), target));
    }

    // --- Helper Methods for Collection Updates ---
    // (These manage adding/updating/removing items in the collection correctly)

    private static void updateDocumentSignatureCollection(Document parent, List<DocumentSignature> existingList, List<DocumentSignature> newList) {
        if (newList == null) return; // Nothing to update with

        Map<Long, DocumentSignature> existingMap = existingList.stream()
                .filter(ds -> ds.getId() != null)
                .collect(Collectors.toMap(DocumentSignature::getId, Function.identity()));

        // Note: Need DocumentSignatureMapper instance here. Pass it as argument if static.
        // For simplicity, assuming direct field updates for now. Real implementation should use the mapper.
        List<DocumentSignature> finalItems = newList.stream().map(newItem -> {
            if (newItem.getId() != null && existingMap.containsKey(newItem.getId())) {
                // Update existing signature entity
                DocumentSignature existingItem = existingMap.get(newItem.getId());
                // Manually update fields or use mapper if available
                existingItem.setActive(newItem.isActive());
                existingItem.setSignatureDate(newItem.getSignatureDate());
                existingItem.setSignatureVisual(newItem.getSignatureVisual());
              //  existingItem.setSignerRole(newItem.getSignerRole());
                // Ensure worker/document links are correct if they can change via DTO ID
                // existingItem.setWorker(...) // Fetch if needed
                return existingItem;
            } else {
                // New signature entity
                newItem.setDocument(parent); // Set parent relationship
                // Ensure worker is set if newItem.worker is null but ID exists in DTO
                // newItem.setWorker(...) // Fetch if needed
                return newItem;
            }
        }).collect(Collectors.toList());

        existingList.clear();
        existingList.addAll(finalItems);
    }

    private static void updateObjectAnsweredCollection(Document parent, List<ObjectAnswered> existingList, List<ObjectAnswered> newList) {
        if (newList == null) return;

        Map<Long, ObjectAnswered> existingMap = existingList.stream()
                .filter(oa -> oa.getId() != null)
                .collect(Collectors.toMap(ObjectAnswered::getId, Function.identity()));

        List<ObjectAnswered> finalItems = newList.stream().map(newItem -> {
            if (newItem.getId() != null && existingMap.containsKey(newItem.getId())) {
                ObjectAnswered existingItem = existingMap.get(newItem.getId());
                // Manually update or use mapper
                existingItem.setAnswer(newItem.getAnswer());
                existingItem.setEe(newItem.getEe());
                existingItem.setEu(newItem.getEu());
                existingItem.setObjectId(newItem.getObjectId());
                existingItem.setObjectType(newItem.getObjectType());
                return existingItem;
            } else {
                newItem.setDocument(parent); // Set parent relationship
                return newItem;
            }
        }).collect(Collectors.toList());

        existingList.clear();
        existingList.addAll(finalItems);
    }
}