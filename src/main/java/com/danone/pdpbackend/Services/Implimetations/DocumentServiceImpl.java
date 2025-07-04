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
    public DocumentServiceImpl(DocumentRepo documentRepo, ChantierService chantierService, ObjectAnswerRepo objectAnswerRepo, RisqueService risqueService, WorkerSelectionService workerSelectionService, NotificationService notificationService, SecurityConfiguration securityConfiguration, ActivityLogService activityLogService) {
        this.documentRepo = documentRepo;
        this.chantierService = chantierService;
        this.objectAnswerRepo = objectAnswerRepo;
        this.risqueService = risqueService;
        this.workerSelectionService = workerSelectionService;
        this.notificationService = notificationService;
        this.securityConfiguration = securityConfiguration;
        this.activityLogService = activityLogService;
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
        //1.Check if the document has a chantier first
        if (document.getChantier() != null) {
            try {
                // Get the chantier from the db and check it's status, and apply it to the document
                chantierStatus = chantierService.getById(document.getChantier().getId()).getStatus();
                if (chantierStatus == ChantierStatus.COMPLETED) {
                    document.setStatus(DocumentStatus.COMPLETED);
                    document.setActionType(ActionType.NONE);
                    return document;
                }
                else if (chantierStatus == ChantierStatus.CANCELED){
                    document.setStatus(DocumentStatus.CANCELED);
                    document.setActionType(ActionType.NONE);
                    return document;

                }
            } catch (Exception e) {
                log.warn("Could not determine status for Chantier {} linked to Document {}", document.getChantier(), document.getId(), e);
            }
        }else{
            log.warn("Document {} has no associated chantier ID.", document.getId());
            document.setStatus(DocumentStatus.DRAFT);
            document.setActionType(ActionType.NONE);
            return document;

        }


        //TODO:Check Expiry for PDP not BDT




        // 2. Check Signatures - signatures has the priority over permits
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

        if (!assignedWorkers.isEmpty()) {
            List<Worker> signedWorkers = document.getSignatures().stream()
                    .filter(signature -> signature.getWorker() != null)
                    .map(DocumentSignature::getWorker).toList();

            Set<Long> assignedWorkerIds = assignedWorkers.stream().map(Worker::getId).collect(Collectors.toSet());
            Set<Long> signedWorkerIds = signedWorkers.stream().map(Worker::getId).collect(Collectors.toSet());

            boolean allSigned = assignedWorkerIds.stream().allMatch(signedWorkerIds::contains);
            if (!allSigned) {
                document.setStatus(DocumentStatus.NEEDS_ACTION);
                document.setActionType(ActionType.SIGHNATURES_MISSING);
                return document;
            }
        }


        // 3. Check Permits based on linked Risks
        boolean permitsNeeded = false;
        Boolean isThereNullPermits = document.getRelations().stream()
                .filter(r -> r.getObjectType() == ObjectAnsweredObjects.RISQUE)
                .map(r -> {
                    try { return risqueService.getRisqueById(r.getObjectId()); }
                    catch (Exception e) { log.warn("Could not find Risque {} for PDP {}", r.getObjectId(), document.getId()); return null; }
                })
                .filter(Objects::nonNull)
                .filter(risque -> risque.getTravaillePermit() != null && risque.getTravaillePermit())
                .map(Risque::getPermitId)
                .anyMatch(Objects::isNull);

        if (isThereNullPermits) {
            document.setStatus(DocumentStatus.NEEDS_ACTION);
            document.setActionType(ActionType.PERMIT_MISSING);
            return document;
        }

        List<Long> requiredPermitIds = document.getRelations().stream()
                .filter(r -> r.getObjectType() == ObjectAnsweredObjects.RISQUE)
                .map(r -> {
                    try { return risqueService.getRisqueById(r.getObjectId()); }
                    catch (Exception e) { log.warn("Could not find Risque {} for PDP {}", r.getObjectId(), document.getId()); return null; }
                })
                .filter(Objects::nonNull)
                .filter(risque -> risque.getTravaillePermit() != null && risque.getTravaillePermit())
                .map(Risque::getPermitId)
                .toList();

        if (!requiredPermitIds.isEmpty()) {
            Set<Long> linkedPermitObjectIds = document.getRelations().stream()
                    .filter(r -> r.getObjectType() == ObjectAnsweredObjects.PERMIT && r.getAnswer() != null && r.getAnswer()) // Check if linked and marked as addressed/valid
                    .map(ObjectAnswered::getObjectId)
                    .collect(Collectors.toSet());

            // Check if all *required* permits (based on risks) are linked and marked valid in the PDP relations
            permitsNeeded = !requiredPermitIds.stream().allMatch(linkedPermitObjectIds::contains);
            // This is a simplified check. You might need to fetch Permit entities and check their validity dates/status.
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
