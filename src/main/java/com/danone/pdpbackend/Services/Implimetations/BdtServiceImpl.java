package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.*;
import com.danone.pdpbackend.Services.*;
import com.danone.pdpbackend.Utils.ActionType;
import com.danone.pdpbackend.Utils.ChantierStatus;
import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.entities.*;
import com.danone.pdpbackend.entities.Bdt;
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
        List<Bdt> list =  bdtRepo.findBDTByChantierId(chantierId);
        if (list.isEmpty()) {
            return List.of();
        } else {
            return list;
        }
    }


}