package com.danone.pdpbackend.Services.Implimetations;


import com.danone.pdpbackend.Repo.*;
import com.danone.pdpbackend.Services.*;

import com.danone.pdpbackend.Utils.ActionType;
import com.danone.pdpbackend.Utils.ChantierStatus;
import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.Utils.Image.ImageModel;
import com.danone.pdpbackend.Utils.mappers.PdpMapper;
import com.danone.pdpbackend.entities.*;
import com.danone.pdpbackend.entities.dto.PdpDTO;
import com.danone.pdpbackend.entities.dto.DocumentSignatureStatusDTO;
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
    public Pdp calculateDocumentState(Long documentId) {
        Pdp document = pdpRepo.findById(documentId).orElse(null);
        if (document == null) {
            throw new IllegalArgumentException("Document not found");
        }

        return calculateDocumentState(document);
    }





    @Override
    @Transactional
    public void triggerPdpRenewalCheck() {
        // This implements the logic directly. Alternatively, call PdpMaintenanceService.
        log.info("Starting annual PDP check...");
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);

        // Find PDPs to renew (needs custom query or filter)
        // Example: Find READY or COMPLETED ones older than a year
        List<Pdp> pdpsToRenew = pdpRepo.findByStatusInAndCreationDateBefore(
                List.of(DocumentStatus.ACTIVE, DocumentStatus.COMPLETED), oneYearAgo
        );
        // Fallback filter if no custom query:
         /*
         List<Pdp> pdpsToRenew = pdpRepo.findAll().stream()
             .filter(pdp -> pdp.getCreationDate() != null && pdp.getCreationDate().isBefore(oneYearAgo))
             .filter(pdp -> pdp.getStatus() == DocumentStatus.READY || pdp.getStatus() == DocumentStatus.COMPLETED)
             .toList();
         */

        log.info("Found {} PDPs requiring renewal.", pdpsToRenew.size());

        for (Pdp oldPdp : pdpsToRenew) {
            try {
                log.info("Renewing PDP ID {}.", oldPdp.getId());
                oldPdp.setStatus(DocumentStatus.EXPIRED); // Mark old one as expired
                pdpRepo.save(oldPdp);

                Pdp newPdp = copyPdpForRenewal(oldPdp);
                create(newPdp); // Use the create method to save the new one (handles relations, initial status etc)
                log.info("Renewal complete for old PDP ID {}. New PDP created.", oldPdp.getId());
                // updateAndSavePdpStatus(createdNewPdp.getId()); // Ensure status is correct after creation if needed

            } catch (Exception e) {
                log.error("Error renewing PDP ID {}: {}", oldPdp.getId(), e.getMessage(), e);
            }
        }
        log.info("Finished annual PDP check.");
    }

    @Override
    public List<ObjectAnswered> getRisquesWithoutPermits(Long pdpId) {
        return objectAnswerRepo.findRisquesWithoutPermitsByPdpId(pdpId);
    }

    // Signature methods implementation
    @Override
    @Transactional
    public Pdp signDocument(Long pdpId, Long workerId, ImageModel signatureImage) {
        Pdp pdp = pdpRepo.findById(pdpId)
                .orElseThrow(() -> new EntityNotFoundException("PDP not found with id: " + pdpId));

        Worker worker = workerSelectionService.getWorkerById(workerId);
        if (worker == null) {
            throw new IllegalArgumentException("Worker not found with id: " + workerId);
        }

        // Check if worker is assigned to this chantier
        if (!workerSelectionService.isWorkerAssignedToChantier(workerId, pdp.getChantier().getId())) {
            throw new IllegalArgumentException("Worker is not assigned to this chantier");
        }

        // Check if worker has already signed this document
        boolean alreadySigned = pdp.getSignatures().stream()
                .anyMatch(sig -> sig.getWorker().getId().equals(workerId) && sig.isActive());

        if (alreadySigned) {
            throw new IllegalArgumentException("Worker has already signed this document");
        }

        // Create signature
        DocumentSignature signature = new DocumentSignature();
        signature.setDocument(pdp);
        signature.setWorker(worker);
        signature.setSignatureVisual(signatureImage);
        signature.setActive(true);

        pdp.getSignatures().add(signature);

        // Update document status based on signature completion
        updateDocumentStatusAfterSignature(pdp);

        return pdpRepo.save(pdp);
    }

    @Override
    @Transactional
    public Pdp removeSignature(Long pdpId, Long signatureId) {
        Pdp pdp = pdpRepo.findById(pdpId)
                .orElseThrow(() -> new EntityNotFoundException("PDP not found with id: " + pdpId));

        DocumentSignature signature = pdp.getSignatures().stream()
                .filter(sig -> sig.getId().equals(signatureId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Signature not found with id: " + signatureId));

        signature.setActive(false);

        // Update document status after removing signature
        updateDocumentStatusAfterSignature(pdp);

        return pdpRepo.save(pdp);
    }

    @Override
    public DocumentSignatureStatusDTO getSignatureStatus(Long pdpId) {
        Pdp pdp = pdpRepo.findById(pdpId)
                .orElseThrow(() -> new EntityNotFoundException("PDP not found with id: " + pdpId));

        List<Worker> requiredWorkers = workerSelectionService.getWorkersByChantier(pdp.getChantier().getId());

        List<DocumentSignatureStatusDTO.SignatureInfoDTO> signatures = pdp.getSignatures().stream()
                .filter(DocumentSignature::isActive)
                .map(sig -> new DocumentSignatureStatusDTO.SignatureInfoDTO(
                        sig.getId(),
                        sig.getWorker().getId(),
                        sig.getWorker().getNom() + " " + sig.getWorker().getPrenom(),
                        sig.getSignatureDate().toString(),
                        sig.isActive()
                ))
                .collect(Collectors.toList());

        List<DocumentSignatureStatusDTO.WorkerSignatureStatusDTO> workerStatuses = requiredWorkers.stream()
                .map(worker -> {
                    boolean hasSigned = pdp.getSignatures().stream()
                            .anyMatch(sig -> sig.getWorker().getId().equals(worker.getId()) && sig.isActive());

                    return new DocumentSignatureStatusDTO.WorkerSignatureStatusDTO(
                            worker.getId(),
                            worker.getNom() + " " + worker.getPrenom(),
                            worker.getRole(),
                            hasSigned
                    );
                })
                .collect(Collectors.toList());

        List<DocumentSignatureStatusDTO.WorkerSignatureStatusDTO> missingSignatures = workerStatuses.stream()
                .filter(ws -> !ws.isHasSigned())
                .collect(Collectors.toList());

        int requiredSignatures = requiredWorkers.size();
        int currentSignatures = signatures.size();
        boolean isFullySigned = currentSignatures >= requiredSignatures;

        return new DocumentSignatureStatusDTO(
                pdpId,
                "PDP",
                requiredSignatures,
                currentSignatures,
                isFullySigned,
                signatures,
                missingSignatures
        );
    }

    @Override
    public boolean isDocumentFullySigned(Long pdpId) {
        DocumentSignatureStatusDTO status = getSignatureStatus(pdpId);
        return status.isFullySigned();
    }

    private void updateDocumentStatusAfterSignature(Pdp pdp) {
        boolean isFullySigned = isDocumentFullySigned(pdp.getId());

        if (isFullySigned) {
            pdp.setStatus(DocumentStatus.SIGNED);
            pdp.setActionType(ActionType.NONE);

            // Update chantier status if needed
            updateChantierStatusAfterDocumentSigned(pdp);
        } else {
            pdp.setStatus(DocumentStatus.NEEDS_SIGNATURES);
            pdp.setActionType(ActionType.SIGN);
        }
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
