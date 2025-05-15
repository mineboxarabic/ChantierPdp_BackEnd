package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.Services.ChantierService;
import com.danone.pdpbackend.Services.EntrepriseService;
// Import the new DocumentSignatureMapper and DTO
import com.danone.pdpbackend.entities.DocumentSignature;
import com.danone.pdpbackend.entities.dto.DocumentSignatureDTO;
import com.danone.pdpbackend.entities.Bdt;
import com.danone.pdpbackend.entities.ObjectAnswered; // Import if needed
import com.danone.pdpbackend.entities.dto.BdtDTO;
import lombok.RequiredArgsConstructor; // Use Lombok
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor // Use Lombok for constructor injection
public class BdtMapper implements Mapper<BdtDTO, Bdt> {

    private final ObjectAnsweredMapper objectAnsweredMapper;
    private final ChantierService chantierService;
    private final EntrepriseService entrepriseService;
    private final DocumentSignatureMapper documentSignatureMapper; // Inject the new mapper

    @Override
    public BdtDTO toDTO(Bdt bdt) {
        if (bdt == null) {
            return null;
        }
        BdtDTO bdtDTO = new BdtDTO();
        setDTOFields(bdtDTO, bdt); // Use helper
        return bdtDTO;
    }

    @Override
    public void setDTOFields(BdtDTO bdtDTO, Bdt bdt) {
        if (bdtDTO == null || bdt == null) return;


        DocumentMappingUtils.mapEntityToDtoBase(bdt, bdtDTO, objectAnsweredMapper, documentSignatureMapper);

        bdtDTO.setNom(bdt.getNom());
        bdtDTO.setComplementOuRappels(bdt.getComplementOuRappels()); // Assuming direct copy is ok
        if (bdt.getChantier() != null) {
            bdtDTO.setChantier(bdt.getChantier().getId());
        }

        // REMOVE old specific signature fields
        // if(bdt.getSignatureChargeDeTravail() != null) {
        //     bdtDTO.setSignatureChargeDeTravail(bdt.getSignatureChargeDeTravail().getId());
        // }
        // if(bdt.getSignatureDonneurDOrdre() != null) {
        //     bdtDTO.setSignatureDonneurDOrdre(bdt.getSignatureDonneurDOrdre().getId());
        // }

        // REMOVE old worker list mapping
        // if(bdt.getSignatures() != null) { // This was List<Worker> before
        //     bdtDTO.setSignatures(bdt.getSignatures().stream().map(Worker::getId).toList());
        // }
    }

    // --- Mapping from DTO to Entity ---

    @Override
    public Bdt toEntity(BdtDTO bdtDTO) {
        if (bdtDTO == null) {
            return null;
        }
        Bdt bdt = new Bdt();
        // ID is typically set by persistence context or during update fetch
        // bdt.setId(bdtDTO.getId());
        setEntityFields(bdtDTO, bdt); // Use helper
        return bdt;
    }

    @Override
    public void setEntityFields(BdtDTO bdtDTO, Bdt bdt) {
        if (bdtDTO == null || bdt == null) return;

        // Map common Document fields using the utility
        DocumentMappingUtils.mapDtoToEntityBase(bdtDTO, bdt,chantierService,entrepriseService, objectAnsweredMapper, documentSignatureMapper);


        bdt.setNom(bdtDTO.getNom());
        bdt.setComplementOuRappels(bdtDTO.getComplementOuRappels()); // Direct copy ok for JSON type?

        if (bdtDTO.getChantier() != null) {
            if (bdt.getChantier() == null || !bdt.getChantier().getId().equals(bdtDTO.getChantier())) {
                bdt.setChantier(chantierService.getById(bdtDTO.getChantier()));
            }
        } else {
            bdt.setChantier(null);
        }
    }

    // --- List Mapping ---

    @Override
    public List<Bdt> toEntityList(List<BdtDTO> bdtDTOS) {
        if (bdtDTOS == null) return Collections.emptyList();
        return bdtDTOS.stream()
                .filter(Objects::nonNull)
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BdtDTO> toDTOList(List<Bdt> bdts) {
        if (bdts == null) return Collections.emptyList();
        return bdts.stream()
                .filter(Objects::nonNull)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // --- Update Mapping ---

    @Override
    public Bdt updateEntityFromDTO(Bdt bdt, BdtDTO bdtDTO) {
        setEntityFields(bdtDTO, bdt);
        return bdt;
    }

    @Override
    public BdtDTO updateDTOFromEntity(BdtDTO bdtDTO, Bdt bdt) {
        setDTOFields(bdtDTO, bdt);
        return bdtDTO;
    }

    // --- Helper Methods for Collection Updates (copy from PdpMapper or create a common utility) ---

    private void updateDocumentSignatureCollection(List<DocumentSignature> existingList, List<DocumentSignature> newList) {
        if (newList == null) return;

        Map<Long, DocumentSignature> existingMap = existingList.stream()
                .filter(ds -> ds.getId() != null)
                .collect(Collectors.toMap(DocumentSignature::getId, Function.identity()));

        List<DocumentSignature> finalItems = newList.stream().map(newItem -> {
            if (newItem.getId() != null && existingMap.containsKey(newItem.getId())) {
                DocumentSignature existingItem = existingMap.get(newItem.getId());
                documentSignatureMapper.updateEntityFromDTO(existingItem, documentSignatureMapper.toDTO(newItem));
                return existingItem;
            } else {
                newItem.setDocument(existingList.isEmpty() ? null : existingList.get(0).getDocument()); // Set parent ref
                return newItem;
            }
        }).collect(Collectors.toList());

        existingList.clear();
        existingList.addAll(finalItems);
    }

    private void updateObjectAnsweredCollection(List<ObjectAnswered> existingList, List<ObjectAnswered> newList) {
        if (newList == null) return;

        Map<Long, ObjectAnswered> existingMap = existingList.stream()
                .filter(oa -> oa.getId() != null)
                .collect(Collectors.toMap(ObjectAnswered::getId, Function.identity()));

        List<ObjectAnswered> finalItems = newList.stream().map(newItem -> {
            if (newItem.getId() != null && existingMap.containsKey(newItem.getId())) {
                ObjectAnswered existingItem = existingMap.get(newItem.getId());
                objectAnsweredMapper.updateEntityFromDTO(existingItem, objectAnsweredMapper.toDTO(newItem));
                return existingItem;
            } else {
                newItem.setDocument(existingList.isEmpty() ? null : existingList.get(0).getDocument()); // Set parent ref
                return newItem;
            }
        }).collect(Collectors.toList());

        existingList.clear();
        existingList.addAll(finalItems);
    }
}