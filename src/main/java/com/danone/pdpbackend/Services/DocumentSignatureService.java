package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.Repo.*;
import com.danone.pdpbackend.Utils.ActionType;
import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.Utils.Image.ImageModel;
import com.danone.pdpbackend.entities.*;
import com.danone.pdpbackend.entities.dto.SignatureRequestDTO;
import com.danone.pdpbackend.entities.dto.SignatureResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentSignatureService {

    private final DocumentRepo documentRepository;
    private final WorkerRepo workerRepository;
    private final DocumentSignatureRepository documentSignatureRepository;
    private final UsersRepo usersRepo;
    private final ChantierRepo chantierRepo;

    public void signDocument(SignatureRequestDTO signatureRequest) {
        signDocumentAndReturnId(signatureRequest);
    }

    public Long signDocumentAndReturnId(SignatureRequestDTO signatureRequest) {
        // Validate worker existence
        Worker worker = workerRepository.findById(signatureRequest.getWorkerId());

        User user = usersRepo.findById(signatureRequest.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (worker == null) {
            throw new IllegalArgumentException("Worker not found");
        }

        // Validate document existence
        Optional<Document> documentOpt = documentRepository.findById(signatureRequest.getDocumentId());
        if (documentOpt.isEmpty()) {
            throw new IllegalArgumentException("Document not found");
        }

        Document document = documentOpt.get();

        // Decode and validate signature image
        byte[] signatureImageBytes;
        try {
            signatureImageBytes = Base64.getDecoder().decode(signatureRequest.getSignatureImage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 signature image format");
        }

        if (signatureImageBytes.length == 0) {
            throw new IllegalArgumentException("Invalid signature image");
        }

        // Create and save the DocumentSignature
        DocumentSignature signature = new DocumentSignature();
        signature.setWorker(worker);
        signature.setDocument(document);
        signature.setSignatureDate(new java.util.Date());
        signature.setSignatureVisual(new ImageModel(signatureImageBytes));
        signature.setUser(user);
        signature.setActive(true);

        // Save the signature to get the ID
        DocumentSignature savedSignature = documentSignatureRepository.save(signature);

        // Persist the signature
        document.getSignatures().add(savedSignature);

        // Update document status after signing
        updateDocumentStatus(document);

        documentRepository.save(document);

        return savedSignature.getId();
    }

    public void unSignDocument(Long workerId, Long signatureId) {
        Optional<DocumentSignature> signatureOpt = documentSignatureRepository.findById(signatureId);
        if (signatureOpt.isEmpty()) {
            throw new IllegalArgumentException("Signature not found");
        }

        DocumentSignature signature = signatureOpt.get();
        if (!signature.getWorker().getId().equals(workerId)) {
            throw new IllegalArgumentException("Unauthorized to unsign");
        }
        // Remove signature
        documentSignatureRepository.delete(signature);
    }

    public List<Worker> getSignedWorkersByDocument(Long documentId){
        if(documentId == null ){
            return List.of();
        }

        return documentSignatureRepository.findWorkersByDocumentId(documentId);
    }

    public void signDocumentByWorker(SignatureRequestDTO signatureRequest) {

        // Validate worker existence
        Worker worker = workerRepository.findById(signatureRequest.getWorkerId());
        if (worker == null) {
            throw new IllegalArgumentException("Worker not found");
        }

        // Validate document existence
        Optional<Document> documentOpt = documentRepository.findById(signatureRequest.getDocumentId());
        if (documentOpt.isEmpty()) {
            throw new IllegalArgumentException("Document not found");
        }
        Document document = documentOpt.get();

        // Decode and validate signature image
        byte[] signatureImageBytes;
        try {
            signatureImageBytes = Base64.getDecoder().decode(signatureRequest.getSignatureImage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 signature image format");
        }

        if (signatureImageBytes.length == 0) {
            throw new IllegalArgumentException("Invalid signature image");
        }

        // Create and save the DocumentSignature
        DocumentSignature signature = new DocumentSignature();
        signature.setWorker(worker);
        signature.setDocument(document);
        signature.setSignatureDate(new java.util.Date());
        signature.setSignatureVisual(new ImageModel(signatureImageBytes));
        signature.setUser(null);
        signature.setActive(true);

        // Persist the signature
        document.getSignatures().add(signature);

        // Update document status after signing
        updateDocumentStatus(document);

        documentRepository.save(document);
    }

    public Long signDocumentByUser(SignatureRequestDTO signatureRequest) {
        // For user-only signing, we need to work within the existing database constraints
        // The database schema requires both worker_id and doesn't have a separate user_id column
        // So we'll need to use the existing signDocument pattern
        
        // Validate document existence first (for proper error handling order)
        Optional<Document> documentOpt = documentRepository.findById(signatureRequest.getDocumentId());
        if (documentOpt.isEmpty()) {
            throw new IllegalArgumentException("Document not found");
        }
        
        // Validate user existence
        User user = usersRepo.findById(signatureRequest.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));




        // Decode and validate signature image
        byte[] signatureImageBytes;
        try {
            signatureImageBytes = Base64.getDecoder().decode(signatureRequest.getSignatureImage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 signature image format");
        }
        if (signatureImageBytes.length == 0) {
            throw new IllegalArgumentException("Invalid signature image");
        }
        // Create and save the DocumentSignature
        DocumentSignature signature = new DocumentSignature();
        signature.setWorker(null); // No worker for user signing
        signature.setDocument(documentOpt.get());
        signature.setSignatureDate(new java.util.Date());
        signature.setSignatureVisual(new ImageModel(signatureImageBytes));
        signature.setUser(user); // Set the user who signed
        signature.setActive(true);
        signature.setPrenom(signatureRequest.getPrenom());
        signature.setNom(signatureRequest.getNom());
        // Save signature and get the ID
        DocumentSignature savedSignature = documentSignatureRepository.save(signature);
        // Persist the signature
        Document document = documentOpt.get();
        document.getSignatures().add(savedSignature);
        // Update document status after signing
        updateDocumentStatus(document);
        documentRepository.save(document);
        return savedSignature.getId();
    }

    public void unSignDocumentByUser(Long userId, Long signatureId) {
        // Since the database schema requires worker_id and doesn't have separate user_id column,
        // we need to find the signature by ID and verify the user has permission to unsign it
        Optional<DocumentSignature> signatureOpt = documentSignatureRepository.findById(signatureId);
        if (signatureOpt.isEmpty()) {
            throw new IllegalArgumentException("Signature not found");
        }

        DocumentSignature signature = signatureOpt.get();
        
        // For user unsigning, we need to check if this signature was created by the user
        // Check if the user who created this signature matches the requesting user
        if (signature.getUser() == null || !signature.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to unsign");
        }
        
        // Remove signature
        documentSignatureRepository.delete(signature);
    }

    public Long signDocumentByWorkerAndReturnId(SignatureRequestDTO signatureRequest) {

        // Validate worker existence
        Worker worker = workerRepository.findById(signatureRequest.getWorkerId());
        if (worker == null) {
            throw new IllegalArgumentException("Worker not found");
        }

        // Validate document existence
        Optional<Document> documentOpt = documentRepository.findById(signatureRequest.getDocumentId());
        if (documentOpt.isEmpty()) {
            throw new IllegalArgumentException("Document not found");
        }
        Document document = documentOpt.get();

        // Decode and validate signature image
        byte[] signatureImageBytes;
        try {
            signatureImageBytes = Base64.getDecoder().decode(signatureRequest.getSignatureImage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 signature image format");
        }

        if (signatureImageBytes.length == 0) {
            throw new IllegalArgumentException("Invalid signature image");
        }

        // Create and save the DocumentSignature
        DocumentSignature signature = new DocumentSignature();
        signature.setWorker(worker);
        signature.setDocument(document);
        signature.setSignatureDate(new java.util.Date());
        signature.setSignatureVisual(new ImageModel(signatureImageBytes));
        signature.setUser(null);
        signature.setActive(true);
        signature.setPrenom(signatureRequest.getPrenom());
        signature.setNom(signatureRequest.getNom());

        // Save signature and get the ID
        DocumentSignature savedSignature = documentSignatureRepository.save(signature);

        // Persist the signature
        document.getSignatures().add(savedSignature);

        // Update document status after signing
        updateDocumentStatus(document);

        Document doc = documentRepository.save(document);
        
        return savedSignature.getId();
    }

    /**
     * Updates the document status based on current signatures and chantier conditions
     */
    private void updateDocumentStatus(Document document) {
        if (document.getChantier() == null) {
            document.setStatus(DocumentStatus.DRAFT);
            return;
        }

        // Check if chantier is canceled (assuming CANCELED is a status in ChantierStatus enum)
        if (document.getChantier().getStatus() != null &&
            document.getChantier().getStatus().name().equals("CANCELED")) {
            document.setStatus(DocumentStatus.CANCELED);
            return;
        }

        // Check if chantier is completed (assuming COMPLETED is a status in ChantierStatus enum)
        if (document.getChantier().getStatus() != null &&
            document.getChantier().getStatus().name().equals("COMPLETED")) {
            document.setStatus(DocumentStatus.COMPLETED);
            return;
        }

        // Get all workers assigned to this chantier through WorkerChantierSelection
        List<Worker> assignedWorkers = chantierRepo.findById(document.getChantier().getId())
                .map(chantier -> chantier.getWorkerSelections().stream()
                    .filter(selection -> selection.getIsSelected()) // Only consider selected workers
                    .map(selection -> selection.getWorker())
                    .toList())
                .orElse(List.of());

        // Get all workers who have signed this document
        List<Worker> signedWorkers = getSignedWorkersByDocument(document.getId());

        // Check if all assigned workers have signed using IDs for comparison
        boolean allWorkersSigned = !assignedWorkers.isEmpty() &&
                                  signedWorkers.size() >= assignedWorkers.size() &&
                                  assignedWorkers.stream().allMatch(assignedWorker ->
                                      signedWorkers.stream().anyMatch(signedWorker ->
                                          signedWorker.getId().equals(assignedWorker.getId())));

        if (!allWorkersSigned) {
            document.setStatus(DocumentStatus.NEEDS_ACTION);
            document.setActionType(ActionType.SIGHNATURES_MISSING);
            return;
        }

        // Check if any permits are needed (assuming relations represent permit requirements)
        // For now, simplified check - if there are no relations, no permits needed
        boolean permitsNeeded = document.getRelations() != null &&
                               !document.getRelations().isEmpty();

        if (permitsNeeded) {
            document.setStatus(DocumentStatus.NEEDS_ACTION);
            document.setActionType(ActionType.PERMIT_MISSING);
            return;
        }

        // If all conditions are met, document is ACTIVE
        document.setStatus(DocumentStatus.ACTIVE);
        document.setActionType(ActionType.NONE);
    }

    public List<SignatureResponseDTO> getSignaturesByDocumentId(Long documentId) {

        if (documentId == null) {
            return List.of();
        }
    List<DocumentSignature> signatures = documentSignatureRepository.findDocumentSignatureByDocumentId(documentId);
        List<SignatureResponseDTO> signatureResponseDTO = new ArrayList<>();
        for (DocumentSignature signature : signatures) {
            SignatureResponseDTO dto = new SignatureResponseDTO();
            dto.setId(signature.getId());
            dto.setDocumentId(signature.getDocument().getId());
            dto.setWorkerId(signature.getWorker() != null ? signature.getWorker().getId() : null);
            dto.setUserId(signature.getUser() != null ? signature.getUser().getId() : null);
            dto.setSignatureDate(signature.getSignatureDate());
            dto.setSignatureVisual(signature.getSignatureVisual());
            dto.setPrenom(signature.getPrenom());
            dto.setNom(signature.getNom());
            signatureResponseDTO.add(dto);
        }

        if (signatures.isEmpty()) {
            return List.of();
        }

        return signatureResponseDTO;
    }

    public void unSignDocumentByWorker(Long workerId, Long signatureId) {
        Optional<DocumentSignature> signatureOpt = documentSignatureRepository.findById(signatureId);
        if (signatureOpt.isEmpty()) {
            throw new IllegalArgumentException("Signature not found");
        }
        DocumentSignature signature = signatureOpt.get();
        if (signature.getWorker() == null || !signature.getWorker().getId().equals(workerId)) {
            throw new IllegalArgumentException("Unauthorized to unsign");
        }
        // Remove signature
        documentSignatureRepository.delete(signature);
    }

    public List<User> getSignedUsersByDocument(Long documentId) {
        if (documentId == null) {
            return List.of();
        }

        return documentSignatureRepository.findUsersByDocumentId(documentId);
    }
}
