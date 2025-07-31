package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.*;
import com.danone.pdpbackend.Services.*;
import com.danone.pdpbackend.entities.*;
import com.danone.pdpbackend.entities.Bdt;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

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

    public boolean bdtExistsInChantier(Long chantierId) {
        return bdtRepo.existsByChantierId(chantierId);
    }

    public Bdt getByChantierId(Long chantierId) {
        List<Bdt> bdtList = bdtRepo.findBDTByChantierId(chantierId);
        if (bdtList.isEmpty()) {
            return null;
        } else {
            return bdtList.get(0);
        }
    }

    @Override
    public Bdt create(Bdt bdt) {
   //     calculateDocumentState(bdt);

        if(bdt.getChantier() != null && bdtExistsInChantier(bdt.getChantier().getId())) {
            //Copy signatures from existing BDT
            Bdt existingBdt = getByChantierId(bdt.getChantier().getId());
            if (existingBdt != null && existingBdt.getSignatures() != null && !existingBdt.getSignatures().isEmpty()) {
                log.info("Copying {} signatures from existing BDT for chantier ID: {}",
                        existingBdt.getSignatures().size(), bdt.getChantier().getId());

                for (DocumentSignature signature : existingBdt.getSignatures()) {
                    // Only copy active signatures
                    if (signature.isActive()) {
                        DocumentSignature newSignature = new DocumentSignature();
                        newSignature.setWorker(signature.getWorker());
                        newSignature.setDocument(bdt);
                        newSignature.setSignatureDate(signature.getSignatureDate());
                        newSignature.setUser(signature.getUser());
                        newSignature.setNom(signature.getNom());
                        newSignature.setPrenom(signature.getPrenom());
                        newSignature.setActive(signature.isActive());
                        newSignature.setSignatureVisual(signature.getSignatureVisual()); // Copy the visual signature

                        bdt.getSignatures().add(newSignature);

                        log.debug("Copied signature for worker: {} {}",
                                signature.getPrenom(), signature.getNom());
                    }
                }

                log.info("Successfully copied {} active signatures to new BDT",
                        bdt.getSignatures().size());
            } else {
                log.warn("No existing BDT found or no signatures available for chantier ID: {}",
                        bdt.getChantier().getId());
            }
        } else {
            log.info("No existing BDT found for chantier or chantier ID: {}",
                    bdt.getChantier() != null ? bdt.getChantier().getId() : "null");
        }

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
        return bdtRepo.findBDTByChantierId(chantierId);
    }




}
