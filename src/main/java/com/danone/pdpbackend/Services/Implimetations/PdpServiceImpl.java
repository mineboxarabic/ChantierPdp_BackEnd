package com.danone.pdpbackend.Services.Implimetations;


import com.danone.pdpbackend.Repo.*;
import com.danone.pdpbackend.Services.*;

import com.danone.pdpbackend.Utils.ActionType;
import com.danone.pdpbackend.Utils.ChantierStatus;
import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.Utils.mappers.PdpMapper;
import com.danone.pdpbackend.entities.*;
import com.danone.pdpbackend.entities.dto.PdpDTO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.print.Doc;
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

    public PdpServiceImpl(ChantierService chantierService, PdpMapper pdpMapper, WorkerSelectionService workerSelectionService, @Lazy CommonDocumentServiceInterface<Pdp> documentService, DocumentService documentService1, PdpRepo pdpRepo, ObjectAnswerRepo objectAnswerRepo, RisqueService risqueService) {
        this.chantierService = chantierService;
        this.pdpMapper = pdpMapper;
        this.workerSelectionService = workerSelectionService;
        this.documentService = documentService1;
        this.pdpRepo = pdpRepo;
        this.objectAnswerRepo = objectAnswerRepo;
        this.risqueService = risqueService;
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
    public Boolean delete(Long id) {
        Optional<Pdp> pdpOpt = pdpRepo.findById(id);

        if (pdpOpt.isEmpty()) {
            return false;
        }

        pdpRepo.deleteById(id);
        return true;
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
      /*  ChantierStatus chantierStatus = null;
        if (pdp == null) {
            throw new IllegalArgumentException("Document not found");
        }

        if (pdp.getChantier() != null) {
            try {
                // Use calculate, don't trigger save within a calculation
                chantierStatus = chantierService.getById(pdp.getChantier().getId()).getStatus();
                if (chantierStatus == ChantierStatus.COMPLETED) {
                    pdp.setStatus(DocumentStatus.COMPLETED);
                    pdp.setActionType(ActionType.NONE);
                    return (Pdp) pdp;
                }
                else if (chantierStatus == ChantierStatus.CANCELED){
                    pdp.setStatus(DocumentStatus.CANCELED);
                    pdp.setActionType(ActionType.NONE);
                    return (Pdp) pdp;

                }
            } catch (Exception e) {
                log.warn("Could not determine status for Chantier {} linked to Document {}", pdp.getChantier(), pdp.getId(), e);
            }
        }else{
            log.warn("Document {} has no associated chantier ID.", pdp.getId());
            pdp.setStatus(DocumentStatus.DRAFT);
            pdp.setActionType(ActionType.NONE);
            return (Pdp) pdp;

        }


        // 1. Check Expiry
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        if (pdp.getCreationDate() != null && pdp.getCreationDate().isBefore(oneYearAgo)) {
            //It's been over a year since creation
            pdp.setStatus(DocumentStatus.NEEDS_ACTION);
            pdp.setActionType(ActionType.DOCUMENT_EXPIRED);
            return (Pdp) pdp;

        }





        // 4. Check Signatures
        List<Worker> assignedWorkers = List.of();
        if (pdp.getChantier() != null) {
            try {
                assignedWorkers = workerSelectionService.getWorkersForChantier(pdp.getChantier().getId());
            } catch (Exception e) {
                log.warn("Could not get assigned workers for Chantier {} linked to PDP {}", pdp.getChantier(), pdp.getId(), e);
                // If we can't get assigned workers, assume signatures are needed? Or is it an error state?
               // return   DocumentStatus.ACTION_NEEDS_SIGNATURES; // Default to needing action if unsure
                pdp.setStatus(DocumentStatus.NEEDS_ACTION);
                pdp.setActionType(ActionType.SIGHNATURES_MISSING);
                return (Pdp) pdp;

            }
        }

        if (!assignedWorkers.isEmpty()) {
            List<Worker> signedWorkers = pdp.getSignatures().stream()
                    .filter(signature -> signature.getWorker() != null)
                    .map(DocumentSignature::getWorker).toList();

            Set<Long> assignedWorkerIds = assignedWorkers.stream().map(Worker::getId).collect(Collectors.toSet());
            Set<Long> signedWorkerIds = signedWorkers.stream().map(Worker::getId).collect(Collectors.toSet());

            boolean allSigned = assignedWorkerIds.stream().allMatch(signedWorkerIds::contains);
            if (!allSigned) {
                pdp.setStatus(DocumentStatus.NEEDS_ACTION);
                pdp.setActionType(ActionType.SIGHNATURES_MISSING);
                return (Pdp) pdp;
            }
        }
        // If no workers are assigned, maybe signatures aren't required? Depends on rules.
        // 3. Check Permits based on linked Risks
        boolean permitsNeeded = false;

        Boolean isThereNullPermits = pdp.getRelations().stream()
                .filter(r -> r.getObjectType() == ObjectAnsweredObjects.RISQUE)
                .map(r -> {
                    try { return risqueService.getRisqueById(r.getObjectId()); }
                    catch (Exception e) { log.warn("Could not find Risque {} for PDP {}", r.getObjectId(), pdp.getId()); return null; }
                })
                .filter(Objects::nonNull)
                .filter(risque -> risque.getTravaillePermit() != null && risque.getTravaillePermit())
                .map(Risque::getPermitId)
                .anyMatch(Objects::isNull);

        if (isThereNullPermits) {
            pdp.setStatus(DocumentStatus.NEEDS_ACTION);
            pdp.setActionType(ActionType.PERMIT_MISSING);
            return (Pdp) pdp;
        }

        List<Long> requiredPermitIds = pdp.getRelations().stream()
                .filter(r -> r.getObjectType() == ObjectAnsweredObjects.RISQUE)
                .map(r -> {
                    try { return risqueService.getRisqueById(r.getObjectId()); }
                    catch (Exception e) { log.warn("Could not find Risque {} for PDP {}", r.getObjectId(), pdp.getId()); return null; }
                })
                .filter(Objects::nonNull)
                .filter(risque -> risque.getTravaillePermit() != null && risque.getTravaillePermit())
                .map(Risque::getPermitId)
                .toList();

        if (!requiredPermitIds.isEmpty()) {
            Set<Long> linkedPermitObjectIds = pdp.getRelations().stream()
                    .filter(r -> r.getObjectType() == ObjectAnsweredObjects.PERMIT && r.getAnswer() != null && r.getAnswer()) // Check if linked and marked as addressed/valid
                    .map(ObjectAnswered::getObjectId)
                    .collect(Collectors.toSet());

            // Check if all *required* permits (based on risks) are linked and marked valid in the PDP relations
            permitsNeeded = !requiredPermitIds.stream().allMatch(linkedPermitObjectIds::contains);
            // This is a simplified check. You might need to fetch Permit entities and check their validity dates/status.
        }

        if (permitsNeeded) {
            pdp.setStatus(DocumentStatus.NEEDS_ACTION);
            pdp.setActionType(ActionType.PERMIT_MISSING);
            return (Pdp) pdp;

        }
        // 5. If not Expired, Completed, Permit Needed, or Needs Signatures -> Ready
        pdp.setStatus(DocumentStatus.ACTIVE);
        pdp.setActionType(ActionType.NONE);
        return (Pdp) pdp;*/


        Document document = documentService.calculateDocumentState(pdp);

        //Check expiry
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        if (document.getCreationDate() != null && document.getCreationDate().isBefore(oneYearAgo)) {
            //It's been over a year since creation
            pdp.setStatus(DocumentStatus.NEEDS_ACTION);
            pdp.setActionType(ActionType.DOCUMENT_EXPIRED);
            return (Pdp) pdp;
        }

        return (Pdp) document;
    }

    @Transactional
    @Override
    public Pdp updateDocumentStatus(Pdp pdp) {
            /*calculateDocumentState(pdp);
            pdpRepo.save(pdp);
            if(pdp.getChantier() != null){
                try {
                    chantierService.updateAndSaveChantierStatus(pdp.getChantier().getId());
                } catch (Exception e) {
                    log.error("Failed to update chantier status after PDP {} status change", pdp.getId(), e);
                }
            }
            return pdp;*/
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

    // Helper method for renewal copy logic
    private Pdp copyPdpForRenewal(Pdp oldPdp) {
        Pdp newPdp = new Pdp();
        // Copy essential fields
        newPdp.setChantier(oldPdp.getChantier());
        newPdp.setEntrepriseExterieure(oldPdp.getEntrepriseExterieure());
        newPdp.setEntrepriseDInspection(oldPdp.getEntrepriseDInspection());
        newPdp.setHorairesDetails(oldPdp.getHorairesDetails());
        // Deep copy embeddables if they are mutable, otherwise direct assignment is fine
        newPdp.setHoraireDeTravail(oldPdp.getHoraireDeTravail());
        newPdp.setMisesEnDisposition(oldPdp.getMisesEnDisposition());

        // Deep copy relations, linking them to the NEW pdp instance
        if (oldPdp.getRelations() != null) {
            List<ObjectAnswered> newRelations = oldPdp.getRelations().stream().map(oldRelation -> {
                ObjectAnswered newRelation = new ObjectAnswered();
                // newRelation.setPdp(newPdp); // Will be set by the create/save method cascade potentially
                newRelation.setObjectType(oldRelation.getObjectType());
                newRelation.setObjectId(oldRelation.getObjectId());
                // Reset answers/status for renewal
                newRelation.setAnswer(null);
                newRelation.setEe(null);
                newRelation.setEu(null);
                // Save the new relation object if needed before adding to list? Depends on cascade type.
                // objectAnswerRepo.save(newRelation); // Might be needed if cascade isn't setup correctly
                return newRelation;
            }).collect(Collectors.toList());
            newPdp.setRelations(newRelations);
        }

        // Reset fields for a new cycle
        newPdp.setCreationDate(LocalDate.now()); // Set to today
     //   newPdp.setStatus(DocumentStatus.DRAFT); // Start as draft/planned
        // Reset other dates like inspection date etc.
        newPdp.setDateInspection(null);
        newPdp.setIcpdate(null);
        newPdp.setDatePrev(null);
        newPdp.setDatePrevenirCSSCT(null);

        return newPdp;
    }

    @Override
    @Transactional
    public Pdp addSignature(Long documentId, DocumentSignature documentSignature) {
        Pdp pdp = getById(documentId);
        return (Pdp) documentService.addSignature(pdp.getId(), documentSignature);
    }

}