package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.Repo.DocumentRepo;
import com.danone.pdpbackend.Repo.WorkerRepo;
import com.danone.pdpbackend.Repo.DocumentSignatureRepository;
import com.danone.pdpbackend.Utils.Image.ImageModel;
import com.danone.pdpbackend.entities.Document;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.DocumentSignature;
import com.danone.pdpbackend.entities.dto.SignatureRequestDTO;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SignatureService {

    private final DocumentRepo documentRepository;
    private final WorkerRepo workerRepository;
    private final DocumentSignatureRepository documentSignatureRepository;

    public void signDocument(SignatureRequestDTO signatureRequest) {
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
            signatureImageBytes = Base64.decodeBase64(signatureRequest.getSignatureImage());
        } catch (IllegalStateException e) {
            // Rethrow as IllegalArgumentException to be handled by the controller
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
        signature.setActive(true);

        // Persist the signature
        document.getSignatures().add(signature);
        documentRepository.save(document);
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
}
