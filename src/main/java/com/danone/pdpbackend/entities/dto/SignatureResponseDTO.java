package com.danone.pdpbackend.entities.dto;

import com.danone.pdpbackend.Utils.Image.ImageModel;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

import java.util.Date;

@Data
public class SignatureResponseDTO {
    private Long id;
    private Long workerId;
    private Long documentId;
    private Long userId; // Added to track which user is performing the signing
    private String prenom;
    private String nom;
    private String signatureImage; // Base64 encoded image

    private Date signatureDate;
    private ImageModel signatureVisual;
}
