package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.Services.EntrepriseService;
import com.danone.pdpbackend.Services.WorkerService;
import com.danone.pdpbackend.entities.ObjectAnswered;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.PdpDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class PdpMapper implements Mapper<PdpDTO, Pdp> {

    private final EntrepriseService entrepriseService;
    private final WorkerService workerService;
    private final ObjectAnsweredMapper objectAnsweredMapper;

    public PdpMapper(EntrepriseService entrepriseService, WorkerService workerService, ObjectAnsweredMapper objectAnsweredMapper) {
        this.entrepriseService = entrepriseService;
        this.workerService = workerService;
        this.objectAnsweredMapper = objectAnsweredMapper;
    }


    private void updateObjectAnsweredCollection(List<ObjectAnswered> existingList, List<ObjectAnswered> newItems) {
        // Create a map of existing items by ID for quick lookup
        Map<Long, ObjectAnswered> existingMap = existingList.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(ObjectAnswered::getId, Function.identity()));

        // Clear the list but maintain the same reference
        existingList.clear();

        // Add all items from the new list
        for (ObjectAnswered newItem : newItems) {
            if (newItem.getId() != null && existingMap.containsKey(newItem.getId())) {
                // Update existing entity
                ObjectAnswered existingItem = existingMap.get(newItem.getId());
                updateObjectAnswered(existingItem, newItem);
                existingList.add(existingItem);
            } else {
                // Add new entity
                existingList.add(newItem);
            }
        }
    }
    private void updateObjectAnswered(ObjectAnswered target, ObjectAnswered source) {
        target.setObjectType(source.getObjectType());
        target.setObjectId(source.getObjectId());
        target.setAnswer(source.getAnswer());
        target.setEe(source.getEe());
        target.setEu(source.getEu());
        // Don't update pdp reference to maintain relationship
    }
    @Override
    public PdpDTO toDTO(Pdp pdp) {
        if (pdp == null) {
            return null;
        }

        PdpDTO pdpDTO = new PdpDTO();

       setDTOFields(pdpDTO, pdp);

        // Map complex object references to their IDs
        if (pdp.getEntrepriseExterieure() != null) {
            pdpDTO.setEntrepriseExterieure(pdp.getEntrepriseExterieure().getId());
        }

        if (pdp.getEntrepriseDInspection() != null) {
            pdpDTO.setEntrepriseDInspection(pdp.getEntrepriseDInspection().getId());
        }

        // Map Worker list to ID list
        if (pdp.getSignatures() != null) {
            List<Long> signatureIds = pdp.getSignatures().stream()
                    .map(Worker::getId)
                    .collect(Collectors.toList());
            pdpDTO.setSignatures(signatureIds);
        }

        return pdpDTO;
    }

    @Override
    public void setDTOFields(PdpDTO pdpDTO, Pdp pdp) {
        // Map basic properties

        if(pdp.getId() != null) pdpDTO.setId(pdp.getId());

        pdpDTO.setChantier(pdp.getChantier());
        pdpDTO.setDateInspection(pdp.getDateInspection());
        pdpDTO.setIcpdate(pdp.getIcpdate());
        pdpDTO.setDatePrevenirCSSCT(pdp.getDatePrevenirCSSCT());
        pdpDTO.setDatePrev(pdp.getDatePrev());
        pdpDTO.setHorairesDetails(pdp.getHorairesDetails());

        // Map embedded objects
        pdpDTO.setHoraireDeTravail(pdp.getHoraireDeTravail());
        pdpDTO.setMisesEnDisposition(pdp.getMisesEnDisposition());

        // Map lists
        pdpDTO.setRelations(objectAnsweredMapper.toDTOList(pdp.getRelations()));

        if(pdpDTO.getEntrepriseDInspection() != null) pdpDTO.setEntrepriseDInspection(pdp.getEntrepriseDInspection().getId());
        if(pdpDTO.getEntrepriseExterieure() != null) pdpDTO.setEntrepriseExterieure(pdp.getEntrepriseExterieure().getId());
        if(pdpDTO.getSignatures() != null) pdpDTO.setSignatures(pdp.getSignatures().stream().map(s -> s.getId()).toList());


        // Update complex object references

    }



    @Override
    public void setEntityFields(PdpDTO pdpDTO, Pdp pdp) {
        // Basic fields

        if(pdpDTO.getId() != null) pdp.setId(pdpDTO.getId());


        pdp.setChantier(pdpDTO.getChantier());
        pdp.setDateInspection(pdpDTO.getDateInspection());
        pdp.setIcpdate(pdpDTO.getIcpdate());
        pdp.setDatePrevenirCSSCT(pdpDTO.getDatePrevenirCSSCT());
        pdp.setDatePrev(pdpDTO.getDatePrev());
        pdp.setHorairesDetails(pdpDTO.getHorairesDetails());
        pdp.setHoraireDeTravail(pdpDTO.getHoraireDeTravail());
        pdp.setMisesEnDisposition(pdpDTO.getMisesEnDisposition());

        // Complex objects
        if(pdpDTO.getEntrepriseExterieure() != null) {
            pdp.setEntrepriseExterieure(entrepriseService.getById(pdpDTO.getEntrepriseExterieure()));
        }
        if (pdpDTO.getEntrepriseDInspection() != null) {
            pdp.setEntrepriseDInspection(entrepriseService.getById(pdpDTO.getEntrepriseDInspection()));
        }

        // Signatures
        if (pdpDTO.getSignatures() != null) {
            List<Worker> signatures = workerService.getWorkersByIds(pdpDTO.getSignatures());
            pdp.getSignatures().clear();
            pdp.getSignatures().addAll(signatures);
        }

        // ObjectAnswered lists
        pdp.setRelations(objectAnsweredMapper.toEntityList(pdpDTO.getRelations(), pdp));


    }

    /**
     * Converts a PdpDTO to a Pdp entity.
     * Note: This doesn't set complex objects that need to be retrieved from repositories.
     *
     * @param pdpDTO The PdpDTO to convert
     * @return A partially populated Pdp entity
     */
    public Pdp toEntity(PdpDTO pdpDTO) {
        if (pdpDTO == null) {
            return null;
        }

        Pdp pdp = new Pdp();

        setEntityFields(pdpDTO, pdp);

        if(pdpDTO.getEntrepriseExterieure() != null) {
            pdp.setEntrepriseExterieure(entrepriseService.getById(pdpDTO.getEntrepriseExterieure()));
        }

        if(pdpDTO.getEntrepriseDInspection() != null) {
            pdp.setEntrepriseDInspection(entrepriseService.getById(pdpDTO.getEntrepriseDInspection()));
        }

        if (pdpDTO.getSignatures() != null) {
            List<Worker> signatures =workerService.getWorkersByIds(pdpDTO.getSignatures());
            pdp.setSignatures(signatures);
        }




        return pdp;
    }



    @Override
    public List<Pdp> toEntityList(List<PdpDTO> pdpDTOS) {
        return pdpDTOS.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PdpDTO> toDTOList(List<Pdp> pdps) {
        return pdps.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Pdp updateEntityFromDTO(Pdp pdp, PdpDTO pdpDTO) {
        if(pdpDTO == null) {
            return pdp;
        }
        setEntityFields(pdpDTO, pdp);

        return pdp;
    }

    @Override
    public PdpDTO updateDTOFromEntity(PdpDTO pdpDTO, Pdp pdp) {
        if(pdp == null) {
            return pdpDTO;
        }
        setDTOFields(pdpDTO, pdp);


        return pdpDTO;
    }
}