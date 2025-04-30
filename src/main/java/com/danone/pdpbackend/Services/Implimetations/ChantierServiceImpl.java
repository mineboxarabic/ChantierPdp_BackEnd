package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.ChantierRepo;
import com.danone.pdpbackend.Repo.EntrepriseRepo;
import com.danone.pdpbackend.Services.BDTService;
import com.danone.pdpbackend.Services.ChantierService;
import com.danone.pdpbackend.Services.PdpService;
import com.danone.pdpbackend.Services.WorkerSelectionService;
import com.danone.pdpbackend.Utils.ChantierStatus;
import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.entities.BDT.Bdt;
import com.danone.pdpbackend.entities.Chantier;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.Worker;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChantierServiceImpl implements ChantierService {

    private final ChantierRepo chantierRepo;
    private final EntrepriseRepo entrepriseRepo;
    private final PdpService pdpService;
    private final BDTService bdtService;
    private final WorkerSelectionService workerSelectionService;

    public ChantierServiceImpl(ChantierRepo chantierRepo, EntrepriseRepo entrepriseRepo, @Lazy PdpService pdpService,@Lazy BDTService bdtService, WorkerSelectionService workerSelectionService) {
        this.chantierRepo = chantierRepo;
        this.entrepriseRepo = entrepriseRepo;
        this.pdpService = pdpService;
        this.bdtService = bdtService;
        this.workerSelectionService = workerSelectionService;
    }

    @Override
    public List<Chantier> getAll() {
        return chantierRepo.findAll();
    }

    public Chantier update(Long id, Chantier updatedChantier) {
        updatedChantier.setStatus(calculateChantierStatus(updatedChantier));

        return chantierRepo.save(updatedChantier);
    }

    @Override
    public Chantier create(Chantier chantier) {
        chantier.setStatus(calculateChantierStatus(chantier));
        return chantierRepo.save(chantier);
    }

    @Override
    public Chantier getById(Long id) {
        return chantierRepo.findById(id).orElse(null);
    }

    @Override
    public Boolean delete(Long id) {
        Optional<Chantier> chantierOpt = chantierRepo.findById(id);
        if (chantierOpt.isEmpty()) {
            return false;
        }
        chantierRepo.deleteById(id);
        return true;
    }

    @Override
    public List<Chantier> getByIds(List<Long> ids) {
        List<Chantier> chantiers = chantierRepo.findChantiersByIdIn(ids);
        if (chantiers.isEmpty()) {
            throw new IllegalArgumentException("No chantiers found with the provided ids");
        }
        return chantiers;
    }

    @Override
    public Long getLastId() {
        return (long) chantierRepo.findMaxId();
    }

    @Override
    public List<Chantier> getRecent() {
        List<Chantier> chantiers = chantierRepo.findAll();
        return chantiers.size() <= 10 ? chantiers : chantiers.subList(chantiers.size() - 10, chantiers.size());
    }

    @Override
    public void addPdpToChantier(Long chantierId, Pdp pdp) {
        Chantier chantier = chantierRepo.findById(chantierId)
                .orElseThrow(() -> new IllegalArgumentException("Chantier with id " + chantierId + " not found"));
        chantier.getPdps().add(pdp);
        chantier.setStatus(calculateChantierStatus(chantier)); // Update status after adding PDP
        chantierRepo.save(chantier);
    }

    @Override
    public void addBdtToChantier(Chantier chantier, Bdt bdt) {
       /* Chantier chantier = chantierRepo.findById(chantier)
                .orElseThrow(() -> new IllegalArgumentException("Chantier with id " + chantier + " not found"));*/
        chantier.getBdts().add(bdt);
        chantier.setStatus(calculateChantierStatus(chantier)); // Update status after adding BDT
        chantierRepo.save(chantier);
    }

    @Override
    public List<Worker> getWorkersByChantier(Long chantierId) {
        Chantier chantier = chantierRepo.findById(chantierId)
                .orElseThrow(() -> new IllegalArgumentException("Chantier with id " + chantierId + " not found"));
        return chantier.getWorkers();
    }

    @Override
    public boolean requiresPdp(Long chantierId) {
        Chantier chantier = getById(chantierId); // Fetch the chantier
        chantier.setStatus(calculateChantierStatus(chantier)); // Update status
        return requiresPdp(chantier); // Delegate to helper method
    }
    // Helper method to check requirement based on entity
    private boolean requiresPdp(Chantier chantier) {
        if (chantier == null) return false;
        boolean hoursCondition = chantier.getNbHeurs() != null && chantier.getNbHeurs() >= 400;
        boolean dangerCondition = chantier.getTravauxDangereux() != null && chantier.getTravauxDangereux();
        log.debug("Chantier ID {}: Hours >= 400? {}, Dangerous? {}. Requires PDP? {}",
                chantier.getId(), hoursCondition, dangerCondition, (hoursCondition || dangerCondition));
        return hoursCondition || dangerCondition;
    }
    @Override
    public ChantierStatus calculateChantierStatus(Long chantierId) {
        Chantier chantier = getById(chantierId);
        return calculateChantierStatus(chantier); // Delegate to helper method
    }
    // Helper method to calculate status based on entity
    private ChantierStatus calculateChantierStatus(Chantier chantier) {
        if (chantier == null) {
            throw new IllegalArgumentException("Chantier cannot be null for status calculation");
        }
/*
    PLANIFIED,
    PENDING_PDP,
    PENDING_BDT,
    READY_TO_START,
    ACTIVE,
    SUSPENDED,
    COMPLETED,
    CANCELED,
    INACTIVE_TODAY,
    EXPIRED, it's expired if today > dateFin
    */
        // Check for terminal statuses first
        //If it's canceled or completed, we dont need to check anything else
        if (chantier.getStatus() == ChantierStatus.CANCELED || chantier.getStatus() == ChantierStatus.COMPLETED) {
            return chantier.getStatus();
        }

        // Check for completion based on end date
        LocalDate today = LocalDate.now();


        // Check prerequisite PDP if required
        boolean pdpRequired = requiresPdp(chantier);
        if (pdpRequired) {
            // Find the relevant PDP (assuming one main PDP for now, logic might need refinement)
            // This assumes pdpService.findByChantierId exists or similar logic
            List<Pdp> pdps = chantier.getPdps(); // Get from loaded entity if available
            // Or fetch: List<Pdp> pdps = pdpRepo.findByChantier(chantier.getId());

            Optional<Pdp> activePdp = pdps.stream()
                    .filter(pdp -> pdpService.calculatePdpStatus(pdp.getId()) == DocumentStatus.ACTIVE) // Using unified 'READY' status
                    .findFirst();

            if (activePdp.isEmpty()) {
      /*          // Check if any PDP is pending permits or signatures
                boolean needsPermit = pdps.stream().anyMatch(p -> pdpService.calculatePdpStatus(p.getId()) == DocumentStatus.PERMIT_NEEDED);
                if (needsPermit) return ChantierStatus.PENDING_PDP; // Or a more specific status?

                boolean needsSignature = pdps.stream().anyMatch(p -> pdpService.calculatePdpStatus(p.getId()) == DocumentStatus.NEEDS_SIGNATURES);
                if(needsSignature) return ChantierStatus.PENDING_PDP; // Waiting on PDP
*/
                // If no active PDP and none pending specific actions, assume it's waiting for PDP creation/completion
                return ChantierStatus.PENDING_PDP;
            }
            // If we reach here, PDP requirement is met. Now check daily BDT.
        }

        // If PDP wasn't required or is READY, check for today's BDT
        // Find BDT for today linked to this chantier
         Optional<Bdt> todayBdtOpt = bdtService.findByChantierIdAndDate(chantier.getId(), today); // Need this repo method

        // --- Placeholder: Fetch BDT for today ---
        //Optional<BDT> todayBdtOpt = Optional.empty(); // Replace with actual fetch: bdtService.findBdtForChantierAndDate(chantier.getId(), today);

        if (todayBdtOpt.isPresent()) {
            Bdt todayBdt = todayBdtOpt.get();
            // Assuming BDT has a status field using DocumentStatus
            DocumentStatus bdtStatus = bdtService.calculateBdtStatus(todayBdt.getId()); // Assuming this method exists
            if (bdtStatus == DocumentStatus.ACTIVE) { // Check if BDT is signed/ready
                return ChantierStatus.ACTIVE;
            } else {
                // BDT exists but isn't ready (e.g., DRAFT, NEEDS_SIGNATURES, PERMIT_NEEDED)
                return ChantierStatus.INACTIVE_TODAY; // Work cannot proceed today
            }
        } else {
            // If past start date but no BDT, it's ready but inactive today
            return ChantierStatus.PENDING_BDT; // Pending BDT
        }

    }

    @Override
    @Transactional
    public Chantier updateAndSaveChantierStatus(Long chantierId) {
        Chantier chantier = getById(chantierId);
        ChantierStatus newStatus = calculateChantierStatus(chantier); // Use helper
        if (chantier.getStatus() != newStatus) {
            log.info("Updating status for Chantier {} from {} to {}", chantierId, chantier.getStatus(), newStatus);
            chantier.setStatus(newStatus);
            return chantierRepo.save(chantier);
        }
        return chantier;
    }

    @Override
    public Map<String, Object> getChantierStats(Long chantierId) {
        Chantier chantier = getById(chantierId);
        Map<String, Object> stats = new HashMap<>();

        stats.put("chantierId", chantierId);
        stats.put("chantierStatus", chantier.getStatus());
        stats.put("requiresPdp", requiresPdp(chantier));

        // Count assigned workers
        long workerCount = workerSelectionService.getWorkersForChantier(chantierId).size();
        stats.put("assignedWorkerCount", workerCount);

        // Count PDPs by Status
        Map<DocumentStatus, Long> pdpStatusCounts = chantier.getPdps().stream()
                .collect(Collectors.groupingBy(pdp -> pdpService.calculatePdpStatus(pdp.getId()), Collectors.counting()));
        stats.put("pdpStatusCounts", pdpStatusCounts);

        // Count BDTs by Status (if BDT status exists)
        Map<DocumentStatus, Long> bdtStatusCounts = chantier.getBdts().stream()
                .collect(Collectors.groupingBy(bdt -> bdtService.calculateBdtStatus(bdt.getId()), Collectors.counting())); // Assuming BDT status and service method
        stats.put("bdtStatusCounts", bdtStatusCounts);
        stats.put("totalBdtCount", chantier.getBdts().size());

        // Add more stats as needed

        return stats;
    }


    @Override
    @Transactional
    public Chantier setChantierStatusManually(Long chantierId, ChantierStatus newStatus) {
        Chantier chantier = getById(chantierId);
        ChantierStatus currentStatus = calculateChantierStatus(chantier); // Get current calculated status

        // Add validation logic here if needed
        // e.g., Cannot cancel if already completed? Cannot start if pending PDP?
        log.warn("Manually setting status for Chantier {} from {} (calculated: {}) to {}",
                chantierId, chantier.getStatus(), currentStatus, newStatus);

        chantier.setStatus(newStatus);
        return chantierRepo.save(chantier);
    }


    private LocalDate convertDateToLocalDate(java.util.Date dateToConvert) {
        if (dateToConvert == null) {
            return null;
        }
        return dateToConvert.toInstant()
          .atZone(java.time.ZoneId.systemDefault())
          .toLocalDate();
    }


}
