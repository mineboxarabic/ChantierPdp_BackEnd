package com.danone.pdpbackend.entities.dto;

import com.danone.pdpbackend.Utils.Image.ImageModel; // Ensure ImageModel is correctly imported
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@Data // Lombok annotation for getters, setters, toString, etc.
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSignatureDTO {

    private Long id;
    private Long documentId; // ID of the Document (Pdp, Bdt, etc.)
    private Long workerId; // ID of the Worker who signed
    private Long userId; // ID of the User who performed the signing action
    private String workerName; // Optional: For display purposes on the frontend
    private Date signatureDate;
    private ImageModel signatureVisual; // The visual signature data
    private String signerRole; // Optional: Role during signing (e.g., "ChargeDeTravail")
    private boolean active = true; // Status of the signature

    // Constructor without workerName if not always needed immediately
    public DocumentSignatureDTO(Long id, Long documentId, Long workerId, Date signatureDate, ImageModel signatureVisual, String signerRole, boolean active, Long userId) {
        this.id = id;
        this.documentId = documentId;
        this.workerId = workerId;
        this.signatureDate = signatureDate;
        this.signatureVisual = signatureVisual;
        this.signerRole = signerRole;
        this.active = active;
        this.userId = userId; // Initialize userId
    }
}