package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.*;
import com.danone.pdpbackend.Services.BDTService;
import com.danone.pdpbackend.Services.ChantierService;
import com.danone.pdpbackend.Services.RisqueService;
import com.danone.pdpbackend.Services.WorkerSelectionService;
import com.danone.pdpbackend.Utils.ChantierStatus;
import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.entities.*;
import com.danone.pdpbackend.entities.BDT.Bdt;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BDTServiceImpl implements BDTService {
    private final BDTRepo bdtRepo;
    private final RisqueService risqueService;
    private final ChantierService chantierService;
    private final SignatureRepo signatureRepo;
    private final WorkerSelectionService workerSelectionService;
    private final ObjectAnswerRepo objectAnswerRepo;

    public BDTServiceImpl(BDTRepo bdtRepo, RisqueService risqueService, ChantierService chantierService, SignatureRepo signatureRepo, WorkerSelectionService workerSelectionService, ObjectAnswerRepo objectAnswerRepo) {
        this.bdtRepo = bdtRepo;
        this.risqueService = risqueService;
        this.chantierService = chantierService;
        this.signatureRepo = signatureRepo;
        this.workerSelectionService = workerSelectionService;
        this.objectAnswerRepo = objectAnswerRepo;
    }

    @Override
    public List<Bdt> getAll() {
        return bdtRepo.findAll();
    }

    @Override
    public Bdt getById(Long id) {
        return bdtRepo.findById(id).orElse(null);
    }

    @Override
    public List<Bdt> getByIds(List<Long> id) {
        return bdtRepo.findBDTsByIdIn(id);
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


    @Override
    @Transactional
    public Bdt create(Bdt bdt) {



        Bdt savedBdt = bdtRepo.save(bdt);

        if(bdt.getChantier() != null) {
            chantierService.addBdtToChantier(bdt.getChantier(), savedBdt);
        }

        return savedBdt;
    }

    @Override
    @Transactional
    public Bdt update(Long id, Bdt bdt) {

        Bdt existingBdt = bdtRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("BDT not found"));

        bdt.setRelations(
                mergeObjectAnswered(bdt.getRelations(),existingBdt.getRelations(), id)
        );

        return bdtRepo.save(bdt);
    }

    @Override
    public Boolean delete(Long id) {
        if (!bdtRepo.existsById(id)) {
            return false;
        }
        bdtRepo.deleteById(id);
        return true;
    }


    @Override
    @Transactional // Important for create part
    public Bdt findOrCreateBdtForDate(Long chantierId, LocalDate date /*, Optional<BdtInitialData> initialData */) {
        // Use the CORRECT repository method name here
      //  Optional<Bdt> existingBdt = bdtRepo.findBDTByChantierIdAndDate(chantierId, date);

        List<Bdt> existingBdtList = bdtRepo.findBDTByChantierIdAndDate(chantierId, date);
        Optional<Bdt> existingBdt = existingBdtList.stream().findFirst();


        if (existingBdt.isPresent()) {
            log.debug("Found existing BDT ID {} for Chantier {} on {}", existingBdt.get().getId(), chantierId, date);
            return existingBdt.get();
        } else {
            log.info("Creating new BDT for Chantier {} on {}", chantierId, date);
            // Fetch the Chantier entity to link
            Chantier chantier = chantierService.getById(chantierId);
            Bdt newBdt = new Bdt();
            newBdt.setChantier(chantier); // Link the actual Chantier entity
            newBdt.setDate(date);         // Set the date
            newBdt.setStatus(DocumentStatus.DRAFT); // Set initial status
            newBdt.setNom("BDT " + chantier.getNom() + " - " + date.toString()); // Example name
            // Initialize collections
            newBdt.setRelations(new ArrayList<>());
            // newBdt.setPermits(new ArrayList<>()); // If using permits linked to BDT

            // Populate with initialData if provided and needed
            /*
            if (initialData.isPresent()) {
                // Apply initial data...
            }
            */

            Bdt savedBdt = bdtRepo.save(newBdt); // Save the new BDT
            // Update chantier status after creating BDT
            try {
                chantierService.updateAndSaveChantierStatus(chantierId);
            } catch(Exception e) {
                log.error("Failed to update chantier status after creating BDT for chantier {}", chantierId, e);
            }
            return savedBdt;
        }
    }

    // Ensure findBdtForChantierAndDate also uses the correct repo method name
    @Override
    public Optional<Bdt> findBdtForChantierAndDate(Long chantierId, LocalDate date) {
        // Use the CORRECT repository method name here
        List<Bdt> list =  bdtRepo.findBDTByChantierIdAndDate(chantierId, date);
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(list.get(0));
        }
    }

    @Override
    public DocumentStatus calculateBdtStatus(Long bdtId) {
        Bdt bdt = getById(bdtId); // Fetches or throws if not found


        // 2. Check associated Chantier status
        ChantierStatus chantierStatus = null;
        if (bdt.getChantier() != null) {
            try {
                // Use calculate, don't trigger save within a calculation
                chantierStatus = bdt.getChantier().getStatus();
                if (chantierStatus == ChantierStatus.COMPLETED) {
                    return DocumentStatus.COMPLETED; // PDP is completed if Chantier is
                }
                else if (chantierStatus == ChantierStatus.CANCELED){
                    return DocumentStatus.CANCELED;
                }
            } catch (Exception e) {
                log.warn("Could not determine status for Chantier {} linked to bdt {}", bdt.getChantier(), bdtId, e);
            }
        } else {
            log.warn("bdt {} has no associated chantier ID.", bdtId);
            return DocumentStatus.NEEDS_SIGNATURES; // Or DRAFT?
        }


        // 3. Check Permits based on linked Risks
        boolean permitsNeeded = false;

        if(bdt.getPermitRelations() == null || bdt.getPermitRelations().isEmpty()) {
            log.warn("No permit relations found for BDT {}", bdtId);
            return DocumentStatus.PERMIT_NEEDED;
        }

        List<Long> requiredPermitIds = bdt.getPermitRelations().stream()
                .filter(r -> r.getObjectType() == ObjectAnsweredObjects.RISQUE)
                .map(r -> {
                    try { return risqueService.getRisqueById(r.getObjectId()); }
                    catch (Exception e) { log.warn("Could not find Risque {} for PDP {}", r.getObjectId(), bdtId); return null; }
                })
                .filter(Objects::nonNull)
                .filter(risque -> risque.getTravaillePermit() != null && risque.getTravaillePermit())
                .map(Risque::getPermitId)
                .toList();

        if (!requiredPermitIds.isEmpty()) {
            Set<Long> linkedPermitObjectIds = bdt.getPermitRelations().stream()
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
        if (bdt.getChantier() != null) {
            try {
                assignedWorkers = workerSelectionService.getWorkersForChantier(bdt.getChantier().getId());
            } catch (Exception e) {
                log.warn("Could not get assigned workers for Chantier {} linked to PDP {}", bdt.getChantier(), bdtId, e);
                // If we can't get assigned workers, assume signatures are needed? Or is it an error state?
                return DocumentStatus.NEEDS_SIGNATURES; // Default to needing action if unsure
            }
        }

        if (!assignedWorkers.isEmpty()) {
            List<Worker> signedWorkers = bdt.getSignatures();
            Set<Long> assignedWorkerIds = assignedWorkers.stream().map(Worker::getId).collect(Collectors.toSet());
            Set<Long> signedWorkerIds = signedWorkers.stream().map(Worker::getId).collect(Collectors.toSet());

            boolean allSigned = signedWorkerIds.containsAll(assignedWorkerIds);
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
    public Bdt updateAndSaveBdtStatus(Long bdtId) {
        Bdt bdt = getById(bdtId);
        DocumentStatus newStatus = calculateBdtStatus(bdtId);
        if (bdt.getStatus() != newStatus) {
            log.info("Updating status for BDT {} from {} to {}", bdtId, bdt.getStatus(), newStatus);
            bdt.setStatus(newStatus);
            bdt = bdtRepo.save(bdt);
            // Trigger chantier status update
            if (bdt.getChantier() != null) {
                chantierService.updateAndSaveChantierStatus(bdt.getChantier().getId());
            }
        }
        return bdt;
    }


    @Override
    @Transactional
    public Bdt addSignature(Long bdtId, Signature signature, String type) {
        Bdt bdt = getById(bdtId);
        // Save the signature first to get an ID if it's new
        Signature savedSignature = signatureRepo.save(signature);

        if ("CHARGE_DE_TRAVAIL".equalsIgnoreCase(type)) {
            bdt.setSignatureChargeDeTravail(savedSignature);
        } else if ("DONNEUR_D_ORDRE".equalsIgnoreCase(type)) {
            bdt.setSignatureDonneurDOrdre(savedSignature);
        } else {
            throw new IllegalArgumentException("Invalid signature type for BDT: " + type);
        }

        Bdt updatedBdt = bdtRepo.save(bdt);
        log.info("Added {} signature to BDT {}", type, bdtId);
        // Update status after adding signature
        return updateAndSaveBdtStatus(updatedBdt.getId());
    }

    @Override
    public Optional<Bdt> findByChantierIdAndDate(Long id, LocalDate today) {
        List<Bdt> list =  bdtRepo.findBDTByChantierIdAndDate(id, today);
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(list.get(0));
        }
    }

    @Override
    public List<Bdt> findByChantierId(Long chantierId) {
        List<Bdt> list =  bdtRepo.findBDTByChantierId(chantierId);
        if (list.isEmpty()) {
            return List.of();
        } else {
            return list;
        }
    }


}