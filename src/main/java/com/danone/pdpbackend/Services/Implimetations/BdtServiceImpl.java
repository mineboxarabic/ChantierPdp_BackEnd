package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.*;
import com.danone.pdpbackend.Services.*;
import com.danone.pdpbackend.Utils.ActionType;
import com.danone.pdpbackend.Utils.ChantierStatus;
import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.Utils.Image.ImageModel;
import com.danone.pdpbackend.entities.*;
import com.danone.pdpbackend.entities.Bdt;
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
public class BdtServiceImpl implements BdtService {
    private final BDTRepo bdtRepo;
    private final DocumentService documentService;
    private final RisqueService risqueService;
    private final ChantierService chantierService;
    private final SignatureRepo signatureRepo;
    private final WorkerSelectionService workerSelectionService;
    private final ObjectAnswerRepo objectAnswerRepo;

    public BdtServiceImpl(BDTRepo bdtRepo, RisqueService risqueService, ChantierService chantierService, SignatureRepo signatureRepo, WorkerSelectionService workerSelectionService, ObjectAnswerRepo objectAnswerRepo, @Lazy CommonDocumentServiceInterface<Bdt> documentService, DocumentService documentService1) {
        this.bdtRepo = bdtRepo;
        this.risqueService = risqueService;
        this.chantierService = chantierService;
        this.signatureRepo = signatureRepo;
        this.workerSelectionService = workerSelectionService;
        this.objectAnswerRepo = objectAnswerRepo;
        this.documentService = documentService1;
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


    @Override
    public Bdt create(Bdt bdt) {
        calculateDocumentState(bdt);
        return (Bdt) documentService.create(bdt);
    }

    @Override
    public Bdt update(Long id, Bdt bdt) {
        calculateDocumentState(bdt);
        return (Bdt) documentService.update(id,bdt);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if(!bdtRepo.existsById(id)){
            throw new EntityNotFoundException("Bdt with id " + id + " not found");
        }
        bdtRepo.deleteById(id);
    }


    @Override
    @Transactional // Important for create part
    public Bdt findOrCreateBdtForDate(Long chantierId, LocalDate date /*, Optional<BdtInitialData> initialData */) {
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
    public Bdt calculateDocumentState(Document bdt) {
        return (Bdt) documentService.calculateDocumentState(bdt);
    }

    @Override
    public Bdt updateDocumentStatus(Bdt document) {
        return (Bdt) documentService.updateDocumentStatus(document);
    }


    @Override
    @Transactional
    public Bdt addSignature(Long documentId, DocumentSignature documentSignature){
        
        return (Bdt) documentService.addSignature(documentId, documentSignature);
    }


    @Override
    public Bdt calculateDocumentState(Long documentId) {
        if (documentId != null) {
            Bdt bdt = bdtRepo.findById(documentId).orElse(null);
            if (bdt == null) {
                throw new IllegalArgumentException("Document not found");
            }
            return calculateDocumentState(bdt);
        } else {
            throw new IllegalArgumentException("Unsupported document type");
        }
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
    public Optional<Bdt> findByChantierIdAndCreationDate(Long id, LocalDate today) {
        List<Bdt> list =  bdtRepo.findBDTByChantierIdAndCreationDate(id, today);
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(list.get(0));
        }
    }
    @Override
    public List<Bdt> findByChantierId(Long chantierId) {
        return bdtRepo.findByChantierIdOrderByDateDesc(chantierId);
    }

    // Signature methods implementation
    @Override
    @Transactional
    public Bdt signDocument(Long bdtId, Long workerId, ImageModel signatureImage) {
        Bdt bdt = bdtRepo.findById(bdtId)
                .orElseThrow(() -> new EntityNotFoundException("BDT not found with id: " + bdtId));

        Worker worker = workerSelectionService.getWorkerById(workerId);
        if (worker == null) {
            throw new IllegalArgumentException("Worker not found with id: " + workerId);
        }

        // Check if worker is assigned to this chantier
        if (!workerSelectionService.isWorkerAssignedToChantier(workerId, bdt.getChantier().getId())) {
            throw new IllegalArgumentException("Worker is not assigned to this chantier");
        }

        // Check if worker has already signed this document
        boolean alreadySigned = bdt.getSignatures().stream()
                .anyMatch(sig -> sig.getWorker().getId().equals(workerId) && sig.isActive());

        if (alreadySigned) {
            throw new IllegalArgumentException("Worker has already signed this document");
        }

        // Create signature
        DocumentSignature signature = new DocumentSignature();
        signature.setDocument(bdt);
        signature.setWorker(worker);
        signature.setSignatureVisual(signatureImage);
        signature.setActive(true);

        bdt.getSignatures().add(signature);

        // Update document status based on signature completion
        updateDocumentStatusAfterSignature(bdt);

        return bdtRepo.save(bdt);
    }

    @Override
    @Transactional
    public Bdt removeSignature(Long bdtId, Long signatureId) {
        Bdt bdt = bdtRepo.findById(bdtId)
                .orElseThrow(() -> new EntityNotFoundException("BDT not found with id: " + bdtId));

        DocumentSignature signature = bdt.getSignatures().stream()
                .filter(sig -> sig.getId().equals(signatureId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Signature not found with id: " + signatureId));

        signature.setActive(false);

        // Update document status after removing signature
        updateDocumentStatusAfterSignature(bdt);

        return bdtRepo.save(bdt);
    }

    @Override
    public DocumentSignatureStatusDTO getSignatureStatus(Long bdtId) {
        Bdt bdt = bdtRepo.findById(bdtId)
                .orElseThrow(() -> new EntityNotFoundException("BDT not found with id: " + bdtId));

        List<Worker> requiredWorkers = workerSelectionService.getWorkersByChantier(bdt.getChantier().getId());

        List<DocumentSignatureStatusDTO.SignatureInfoDTO> signatures = bdt.getSignatures().stream()
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
                    boolean hasSigned = bdt.getSignatures().stream()
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
                bdtId,
                "BDT",
                requiredSignatures,
                currentSignatures,
                isFullySigned,
                signatures,
                missingSignatures
        );
    }

    @Override
    public boolean isDocumentFullySigned(Long bdtId) {
        DocumentSignatureStatusDTO status = getSignatureStatus(bdtId);
        return status.isFullySigned();
    }

    private void updateDocumentStatusAfterSignature(Bdt bdt) {
        boolean isFullySigned = isDocumentFullySigned(bdt.getId());

        if (isFullySigned) {
            bdt.setStatus(DocumentStatus.SIGNED);
            bdt.setActionType(ActionType.NONE);

            // Update chantier status if needed
            updateChantierStatusAfterDocumentSigned(bdt);
        } else {
            bdt.setStatus(DocumentStatus.NEEDS_SIGNATURES);
            bdt.setActionType(ActionType.SIGN);
        }
    }

    private void updateChantierStatusAfterDocumentSigned(Bdt bdt) {
        Chantier chantier = bdt.getChantier();

        // Check if this enables the chantier to be ACTIVE
        // A chantier can be ACTIVE if it has signed BDT for today
        if (bdt.getDate().equals(LocalDate.now())) {
            chantier.setStatus(ChantierStatus.ACTIVE);
            chantierService.save(chantier);
        }
    }
}
