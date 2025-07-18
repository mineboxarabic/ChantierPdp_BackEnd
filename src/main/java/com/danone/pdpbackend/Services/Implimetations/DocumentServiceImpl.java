package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.DocumentRepo;
import com.danone.pdpbackend.Repo.ObjectAnswerRepo;
import com.danone.pdpbackend.Services.*;
import com.danone.pdpbackend.Utils.*;
import com.danone.pdpbackend.config.SecurityConfiguration;
import com.danone.pdpbackend.entities.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService{
    private static final Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);
    private final DocumentRepo documentRepo;
    private final ChantierService chantierService;
    private final ObjectAnswerRepo objectAnswerRepo;
    private final RisqueService risqueService;
    private final WorkerSelectionService workerSelectionService;
    private final NotificationService notificationService;
    private final SecurityConfiguration securityConfiguration;
    private final ActivityLogService activityLogService;
    private final PermitService permitService;
    private final DocumentSignatureService documentSignatureService;

    public DocumentServiceImpl(DocumentRepo documentRepo, ChantierService chantierService, ObjectAnswerRepo objectAnswerRepo,
                               RisqueService risqueService, WorkerSelectionService workerSelectionService, NotificationService notificationService,
                               SecurityConfiguration securityConfiguration, ActivityLogService activityLogService, PermitService permitService, DocumentSignatureService documentSignatureService) {
        this.documentRepo = documentRepo;
        this.chantierService = chantierService;
        this.objectAnswerRepo = objectAnswerRepo;
        this.risqueService = risqueService;
        this.workerSelectionService = workerSelectionService;
        this.notificationService = notificationService;
        this.securityConfiguration = securityConfiguration;
        this.activityLogService = activityLogService;
        this.permitService = permitService;
        this.documentSignatureService = documentSignatureService;
    }

    @Override
    public List<Document> getAll() {
        return documentRepo.findAll();
    }

    @Override
    public Document getById(Long id) {
        return documentRepo.findById(id).orElse(null);
    }

    @Override
    public Document create(Document entity) {
        // Set donneurDOrdre from chantier if not already set
        if (entity.getChantier() != null && entity.getDonneurDOrdre() == null) {
            entity.setDonneurDOrdre(entity.getChantier().getDonneurDOrdre());
        }
        
        Document document = documentRepo.save(entity);

        if(document.getChantier() != null) {
            chantierService.addDocumentToChantier(document.getChantier(), document);
            try {
                chantierService.updateAndSaveChantierStatus(document.getChantier().getId());
            } catch (Exception e) {
                log.error("Failed to update chantier status after DOCUMENT {} creation", document.getId(), e);
            }
        }

        activityLogService.logActivity(
                "document.created",
                document.getId(),
                "Chantier",
                "Document created",
                Map.of()
        );

        return document;
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
    public Document update(Long id, Document entityDetails) {
        if (entityDetails.getId() != null && !entityDetails.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID and PDP ID must match");
        }

        if(entityDetails.getChantier() != null){
            try {
                chantierService.updateAndSaveChantierStatus(entityDetails.getChantier().getId());
            } catch (Exception e) {
                log.error("Failed to update chantier status after DOCUMENT {} creation", entityDetails.getId(), e);
            }
        }
        Document document = documentRepo.findById(id).orElse(null);

        assert document != null;
        activityLogService.logActivity(
                "document.created",
                document.getId(),
                "Chantier",
                "Document created",
                Map.of()
        );
        return document;
    }

    private Boolean existsById(Long id) {
        return documentRepo.existsById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if(!documentRepo.existsById(id)){
            throw new EntityNotFoundException("Document with id " + id + " not found");
        }
        documentRepo.deleteById(id);
    }

    @Override
    public List<Document> getByIds(List<Long> ids) {
        return documentRepo.findDocumentsByIdIn(ids);
    }

    @Override
    @Transactional
    public Document addSignature(Long documentId, DocumentSignature documentSignature) {
        Document document = documentRepo.findById(documentId).orElse(null);
        if (document != null) {
            document.getSignatures().add(documentSignature);
            return documentRepo.save(document);
        }
        return null;
    }

    public Document addSignature(Document document, DocumentSignature documentSignature) {
        if (document != null) {
            document.getSignatures().add(documentSignature);
            return documentRepo.save(document);
        }
        return null;
    }


    @Override
    public Document calculateDocumentState(Long documentId) {
        Document document = documentRepo.findById(documentId).orElse(null);
        if (document == null) {
            throw new IllegalArgumentException("Document not found");
        }
        return calculateDocumentState(document);
    }

    @Override
    public Document calculateDocumentState(Document document) {
        ChantierStatus chantierStatus = null;
        if (document == null) {
            throw new IllegalArgumentException("Document not found");
        }

        // 1. Check if the document has a chantier first
        if (document.getChantier() != null) {
            try {
                chantierStatus = chantierService.getById(document.getChantier().getId()).getStatus();
                if (chantierStatus == ChantierStatus.COMPLETED) {
                    document.setStatus(DocumentStatus.COMPLETED);
                    document.setActionType(ActionType.NONE);
                    return document;
                } else if (chantierStatus == ChantierStatus.CANCELED) {
                    document.setStatus(DocumentStatus.CANCELED);
                    document.setActionType(ActionType.NONE);
                    return document;
                }
            } catch (Exception e) {
                log.warn("Could not determine status for Chantier {} linked to Document {}", document.getChantier(), document.getId(), e);
            }
        } else {
            log.warn("Document {} has no associated chantier ID.", document.getId());
            document.setStatus(DocumentStatus.DRAFT);
            document.setActionType(ActionType.NONE);
            return document;
        }

        // 2. Check Signatures - Always check donneur d'ordre, plus workers if any are assigned
        List<Worker> assignedWorkers = List.of();
        if (document.getChantier() != null) {
            try {
                assignedWorkers = workerSelectionService.getWorkersForChantier(document.getChantier().getId());
            } catch (Exception e) {
                log.warn("Could not get assigned workers for Chantier {} linked to PDP {}", document.getChantier(), document.getId(), e);
                document.setStatus(DocumentStatus.NEEDS_ACTION);
                document.setActionType(ActionType.SIGHNATURES_MISSING);

                 String message = String.format("Signature manquante pour le document %d du chantier '%s'.", document.getId(), document.getChantier().getNom());

                 User currentUser = securityConfiguration.getCurrentUser();

                 notificationService.createNotification(currentUser, NotificationType.DOCUMENT_SIGNATURE_MISSING, message, document.getId(), document instanceof Pdp ? "Pdp" : "Bdt", "/documents/" + (document instanceof Pdp ? "pdp/" : "bdt/") + document.getId(), "Document Signature Missing");
                return document;
            }
        }

        // Check worker signatures if workers are assigned
        boolean allWorkersSigned = true;
        if (!assignedWorkers.isEmpty()) {
            List<Worker> signedWorkers = documentSignatureService.getSignedWorkersByDocument(document.getId());

            Set<Long> assignedWorkerIds = assignedWorkers.stream().map(Worker::getId).collect(Collectors.toSet());
            Set<Long> signedWorkerIds = signedWorkers.stream().map(Worker::getId).collect(Collectors.toSet());

            allWorkersSigned = signedWorkerIds.containsAll(assignedWorkerIds);
        }
        
        // Always check if donneur d'ordre has signed (if one is assigned)
        boolean donneurDOrdreSigned = false;
        //User DonneurDOrdre = chantierService.getDonneurDOrdreForChantier(document.getChantier().getId());


        assert document.getChantier() != null;
        if (document.getChantier().getDonneurDOrdre() != null) {
            List<User> signedUsers = documentSignatureService.getSignedUsersByDocument(document.getId());
            donneurDOrdreSigned = signedUsers.stream()
                .anyMatch(user -> user.getId().equals(document.getChantier().getDonneurDOrdre().getId()));
        }
        
        if (!allWorkersSigned || !donneurDOrdreSigned) {
            document.setStatus(DocumentStatus.NEEDS_ACTION);
            document.setActionType(ActionType.SIGHNATURES_MISSING);
            return document;
        }


        // 3. Check Permits based on linked permits to the document itself
        boolean permitsNeeded = false;
        List<Risque> risquesWithPermitType = document.getRelations().stream()
                .filter(r -> r.getObjectType() == ObjectAnsweredObjects.RISQUE)
                .map(r -> {
                    try { return risqueService.getRisqueById(r.getObjectId()); }
                    catch (Exception e) {
                        log.warn("Could not find Risque {} for Document {}", r.getObjectId(), document.getId());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(risque -> risque.getTravaillePermit() != null && risque.getTravaillePermit() && risque.getPermitType() != null)
                .toList();

        if (!risquesWithPermitType.isEmpty()) {
            Set<PermiTypes> requiredPermitTypes = risquesWithPermitType.stream()
                    .map(risque -> {
                        try {
                            return risque.getPermitType();
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid permit type {} for Risque {}", risque.getPermitType(), risque.getId());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Set<PermiTypes> linkedPermitTypes = document.getRelations().stream()
                    .filter(r -> r.getObjectType() == ObjectAnsweredObjects.PERMIT && r.getAnswer() != null && r.getAnswer())
                    .map(r -> {
                        try { return permitService.getPermitById(r.getObjectId()).getType(); }
                        catch (Exception e) {
                            log.warn("Could not find Permit {} for Document {}", r.getObjectId(), document.getId());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            permitsNeeded = !requiredPermitTypes.stream().allMatch(linkedPermitTypes::contains);
        }

        if (permitsNeeded) {
            document.setStatus(DocumentStatus.NEEDS_ACTION);
            document.setActionType(ActionType.PERMIT_MISSING);
            return document;
        }

        // 4. If not Completed, Permit Needed, or Needs Signatures -> Ready
        document.setStatus(DocumentStatus.ACTIVE);
        document.setActionType(ActionType.NONE);
        return document;
    }

    @Override
    public Document updateDocumentStatus(Document document) {
        calculateDocumentState(document);
        documentRepo.save(document);
        if(document.getChantier() != null){
            try {
                chantierService.updateAndSaveChantierStatus(document.getChantier().getId());
            } catch (Exception e) {
                log.error("Failed to update chantier status after DOCUMENT {} status change", document.getId(), e);
            }
        }
        return document;
    }
}
