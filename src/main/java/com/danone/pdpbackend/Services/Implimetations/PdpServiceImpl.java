package com.danone.pdpbackend.Services.Implimetations;


import com.danone.pdpbackend.Repo.*;
import com.danone.pdpbackend.Services.*;

import com.danone.pdpbackend.Utils.ActionType;
import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.Utils.Image.ImageModel;
import com.danone.pdpbackend.Utils.mappers.PdpMapper;
import com.danone.pdpbackend.entities.*;
import com.danone.pdpbackend.entities.dto.PdpDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PdpServiceImpl implements PdpService {

    @Lazy
    private final ChantierService chantierService;
    private final PdpMapper pdpMapper;
    private final WorkerSelectionService workerSelectionService;
    private final DocumentService documentService;
    private final PdpRepo pdpRepo;
    private final ObjectAnswerRepo objectAnswerRepo;
    private final RisqueService risqueService;
    private final RisqueRepo risqueRepo;

    public PdpServiceImpl(ChantierService chantierService, PdpMapper pdpMapper, WorkerSelectionService workerSelectionService, @Lazy CommonDocumentServiceInterface<Pdp> documentService, DocumentService documentService1, PdpRepo pdpRepo, ObjectAnswerRepo objectAnswerRepo, RisqueService risqueService, RisqueRepo risqueRepo) {
        this.chantierService = chantierService;
        this.pdpMapper = pdpMapper;
        this.workerSelectionService = workerSelectionService;
        this.documentService = documentService1;
        this.pdpRepo = pdpRepo;
        this.objectAnswerRepo = objectAnswerRepo;
        this.risqueService = risqueService;
        this.risqueRepo = risqueRepo;
    }


    public List<Pdp> getAll() {
        return pdpRepo.findAll();
    }

    private List<ObjectAnswered> mergeObjectAnswered(List<ObjectAnswered> incoming, List<ObjectAnswered> existing, Long pdpId) {
        List<ObjectAnswered> result = new ArrayList<>();


        for (ObjectAnswered obj : incoming) {
            if (obj.getId() == null) {
                // New object to add
                result.add(objectAnswerRepo.save(obj));
            }
            else {
                // Existing object to update or delete
                ObjectAnswered existingObj = objectAnswerRepo.findById(obj.getId());
                if (existingObj != null) {
                    if (obj.getAnswer() == null) {
                        // Delete object if answer is null
                        objectAnswerRepo.delete(existingObj);
                        // Don't add to result
                    } else {
                        // Update object
                        existingObj.setAnswer(obj.getAnswer());
                        existingObj.setEe(obj.getEe());
                        existingObj.setEu(obj.getEu());
                        existingObj.setObjectType(obj.getObjectType());
                        result.add(objectAnswerRepo.save(existingObj));
                    }
                }
            }
        }

        return result;
    }

    public Boolean existsById(Long id){
        return pdpRepo.existsById(id);
    }

    @Override
    @Transactional
    public Pdp update(Long id, Pdp updatedPdp) {
        calculateDocumentState(updatedPdp);
        return (Pdp) documentService.update(id,updatedPdp);
    }
    @Override
    public Pdp create(Pdp pdp) {
        calculateDocumentState(pdp);
        return (Pdp) documentService.create(pdp);
    }

    @Override
    public Pdp getById(Long id) {
        Optional<Pdp> pdpOpt = pdpRepo.findById(id);
        return pdpOpt.orElse(null);
    }

    @Override
    @Transactional
    public void delete(Long id) {
       documentService.delete(id);
    }

    @Override
    public Long getLastId() {
        return (long) pdpRepo.findMaxId();
    }
    @Override
    public List<Pdp> getByIds(List<Long> pdps) {
        return pdpRepo.findPdpsByIdIn(pdps);
    }
    @Override
    public List<Pdp> getRecent() {
        List<Pdp> pdps = pdpRepo.findAll();
        if (pdps.size() <= 10) {
            return pdps;
        } else {
            return pdps.subList(pdps.size() - 10, pdps.size());
        }

    }


    @Override
    public List<ObjectAnswered> getObjectAnsweredsByPdpId(Long pdpId, ObjectAnsweredObjects objectType) {
        Pdp pdp = getById(pdpId);
        return pdp.getRelations().stream().filter(o -> o.getObjectType() == objectType ).toList();
    }


    @Override
    @Transactional
    public Pdp saveOrUpdatePdp(PdpDTO dto) {
        Pdp pdp;
        if (dto.getId() != null) {
            pdp = getById(dto.getId());
            if (pdp == null) {
                throw new RuntimeException("Pdp not found with ID: " + dto.getId());
            }

            pdpMapper.updateEntityFromDTO(pdp, dto);
            calculateDocumentState(pdp);
            return (Pdp) documentService.update(pdp.getId(), pdp);
        } else {
            pdp = pdpMapper.toEntity(dto);
            calculateDocumentState(pdp);
            return (Pdp) documentService.create(pdp);
        }

    }



    @Override
    public Pdp calculateDocumentState(Document pdp) {
        Document document = documentService.calculateDocumentState(pdp);

        //Check expiry
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        if (pdp.getDate() != null && pdp.getDate().isBefore(oneYearAgo)) {
            // If the document's base status wasn't already terminal (like CANCELED or COMPLETED by chantier)
            // then apply EXPIRED. Expired should generally take precedence over ACTIVE.
            if (pdp.getStatus() != DocumentStatus.CANCELED && pdp.getStatus() != DocumentStatus.COMPLETED) {
                pdp.setStatus(DocumentStatus.EXPIRED); // Using EXPIRED status
                pdp.setActionType(ActionType.NONE);
            }
        }

        return (Pdp) document;
    }

    @Transactional
    @Override
    public Pdp updateDocumentStatus(Pdp pdp) {
        return (Pdp) documentService.updateDocumentStatus(pdp);
    }

    @Override
    public Document addSignature(Long documentId, DocumentSignature documentSignature) {
        return null;
    }

    @Override
    public Pdp calculateDocumentState(Long documentId) {
        Pdp document = pdpRepo.findById(documentId).orElse(null);
        if (document == null) {
            throw new IllegalArgumentException("Document not found");
        }

        return calculateDocumentState(document);
    }








    private void updateChantierStatusAfterDocumentSigned(Pdp pdp) {
        Chantier chantier = pdp.getChantier();

        // Update chantier status based on PDP signing
        // A PDP being signed enables certain chantier operations
        try {
            chantierService.updateAndSaveChantierStatus(chantier.getId());
        } catch (Exception e) {
            log.error("Failed to update chantier status after PDP signing for chantier {}", chantier.getId(), e);
        }
    }
}
