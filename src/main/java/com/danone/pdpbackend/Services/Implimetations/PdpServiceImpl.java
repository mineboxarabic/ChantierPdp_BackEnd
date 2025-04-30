package com.danone.pdpbackend.Services.Implimetations;


import com.danone.pdpbackend.Repo.*;
import com.danone.pdpbackend.Services.*;

import com.danone.pdpbackend.Utils.ChantierStatus;
import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.Utils.mappers.PdpMapper;
import com.danone.pdpbackend.entities.*;
import com.danone.pdpbackend.entities.dto.PdpDTO;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class PdpServiceImpl implements PdpService {

    private final ChantierService chantierService;
    private final PdpMapper pdpMapper;
    private final WorkerSelectionService workerSelectionService;
    private final WorkerService workerService;
    PdpRepo pdpRepo;
    EntrepriseService entrepriseService;
    private final ObjectAnswerRepo objectAnswerRepo;
    private final RisqueService risqueService;


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
        if (updatedPdp.getId() != null && !updatedPdp.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID and PDP ID must match");
        }

        Boolean isExisit = existsById(id);

        if(!isExisit){
            return null;
        }

        // ********* RISQUES *********
        updatedPdp.setRelations(mergeObjectAnswered(updatedPdp.getRelations(), updatedPdp.getRelations(), id));


        return pdpRepo.save(updatedPdp);
    }
    @Override
    public Pdp create(Pdp pdp) {
        Pdp pdp1 = pdpRepo.save(pdp);

        if(pdp.getChantier() != null)
        {
         chantierService.addPdpToChantier(pdp.getChantier(), pdp);
        }
        pdpRepo.save(pdp1);

        return pdp1;
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
    public List<Worker> findWorkersByPdp(Long pdpId) {
        return pdpRepo.findById(pdpId).get().getSignatures();
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
        } else {
            pdp = pdpMapper.toEntity(dto);
        }

        return pdpRepo.save(pdp);
    }


    @Override
    public DocumentStatus calculatePdpStatus(Long pdpId) {
        Pdp pdp = getById(pdpId); // Fetches or throws if not found

        // 1. Check Expiry
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        if (pdp.getCreationDate() != null && pdp.getCreationDate().isBefore(oneYearAgo)) {
            //It's been over a year since creation
            return DocumentStatus.EXPIRED;
        }

        // 2. Check associated Chantier status
        ChantierStatus chantierStatus = null;
        if (pdp.getChantier() != null) {
            try {
                // Use calculate, don't trigger save within a calculation
                chantierStatus = chantierService.calculateChantierStatus(pdp.getChantier());
                if (chantierStatus == ChantierStatus.COMPLETED) {
                    return DocumentStatus.COMPLETED; // PDP is completed if Chantier is
                }
                else if (chantierStatus == ChantierStatus.CANCELED){
                    return DocumentStatus.CANCELED;
                }
            } catch (Exception e) {
                log.warn("Could not determine status for Chantier {} linked to PDP {}", pdp.getChantier(), pdpId, e);
            }
        } else {
            log.warn("PDP {} has no associated chantier ID.", pdpId);
            // Decide status if no chantier - likely DRAFT or NEEDS_ACTION based on other criteria
            // For simplicity, let's assume it needs setup/signatures if no chantier info
            return DocumentStatus.NEEDS_SIGNATURES; // Or DRAFT?
        }


        // 3. Check Permits based on linked Risks
        boolean permitsNeeded = false;
        List<Long> requiredPermitIds = pdp.getRelations().stream()
                .filter(r -> r.getObjectType() == ObjectAnsweredObjects.RISQUE)
                .map(r -> {
                    try { return risqueService.getRisqueById(r.getObjectId()); }
                    catch (Exception e) { log.warn("Could not find Risque {} for PDP {}", r.getObjectId(), pdpId); return null; }
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
            return DocumentStatus.PERMIT_NEEDED;
        }

        // 4. Check Signatures
        List<Worker> assignedWorkers = List.of();
        if (pdp.getChantier() != null) {
            try {
                assignedWorkers = workerSelectionService.getWorkersForChantier(pdp.getChantier());
            } catch (Exception e) {
                log.warn("Could not get assigned workers for Chantier {} linked to PDP {}", pdp.getChantier(), pdpId, e);
                // If we can't get assigned workers, assume signatures are needed? Or is it an error state?
                return DocumentStatus.NEEDS_SIGNATURES; // Default to needing action if unsure
            }
        }

        if (!assignedWorkers.isEmpty()) {
            List<Worker> signedWorkers = pdp.getSignatures();
            Set<Long> assignedWorkerIds = assignedWorkers.stream().map(Worker::getId).collect(Collectors.toSet());
            Set<Long> signedWorkerIds = signedWorkers.stream().map(Worker::getId).collect(Collectors.toSet());

            boolean allSigned = assignedWorkerIds.stream().allMatch(signedWorkerIds::contains);
            if (!allSigned) {
                return DocumentStatus.NEEDS_SIGNATURES;
            }
        }
        // If no workers are assigned, maybe signatures aren't required? Depends on rules.

        // 5. If not Expired, Completed, Permit Needed, or Needs Signatures -> Ready
        return DocumentStatus.ACTIVE;
    }


    @Override
    @Transactional
    public Pdp updateAndSavePdpStatus(Long pdpId) {
        Pdp pdp = getById(pdpId); // Use getById to ensure it exists
        DocumentStatus newStatus = calculatePdpStatus(pdpId);
        if (pdp.getStatus() != newStatus) {
            log.info("Updating status for PDP {} from {} to {}", pdpId, pdp.getStatus(), newStatus);
            pdp.setStatus(newStatus);
            pdp = pdpRepo.save(pdp); // Save the change
            // Potentially update linked Chantier status as well
            if(pdp.getChantier() != null) {
                try {
                    chantierService.updateAndSaveChantierStatus(pdp.getChantier());
                } catch (Exception e) {
                    log.error("Failed to update chantier status after PDP {} status change", pdpId, e);
                }
            }
        }
        return pdp;
    }

    @Override
    public Map<DocumentStatus, Long> getPdpStatusCounts() {
        // Optional: Trigger status updates for all PDPs before counting? (Potentially very slow)
        // updateAllPdpStatuses();
        log.info("Calculating PDP status counts...");
        return pdpRepo.findAll().stream()
                .collect(Collectors.groupingBy(
                        pdp -> calculatePdpStatus(pdp.getId()), // Recalculate for accuracy, could be slow
                        // OR use persisted status: pdp -> pdp.getStatus(), requires statuses to be kept up-to-date
                        Collectors.counting()
                ));
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
        newPdp.setSignatures(new ArrayList<>()); // MUST be empty
        newPdp.setCreationDate(LocalDate.now()); // Set to today
        newPdp.setStatus(DocumentStatus.DRAFT); // Start as draft/planned
        // Reset other dates like inspection date etc.
        newPdp.setDateInspection(null);
        newPdp.setIcpdate(null);
        newPdp.setDatePrev(null);
        newPdp.setDatePrevenirCSSCT(null);

        return newPdp;
    }

    @Override
    @Transactional
    public Pdp addSignature(Long pdpId, Long workerId) {
        Pdp pdp = getById(pdpId);
        Worker worker = workerService.getById(workerId); // Fetch the worker

        if (pdp.getSignatures() == null) {
            pdp.setSignatures(new ArrayList<>());
        }

        // Avoid adding duplicate signatures
        if (!pdp.getSignatures().contains(worker)) {
            pdp.getSignatures().add(worker);
            pdp = pdpRepo.save(pdp);
            log.info("Added signature for worker {} to PDP {}", workerId, pdpId);
            // Update status after adding signature
            return updateAndSavePdpStatus(pdpId);
        } else {
            log.warn("Worker {} already signed PDP {}", workerId, pdpId);
            return pdp; // No change
        }
    }

}