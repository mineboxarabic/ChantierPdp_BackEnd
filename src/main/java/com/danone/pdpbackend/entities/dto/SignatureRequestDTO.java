package com.danone.pdpbackend.entities.dto;

import lombok.Data;

import java.util.Date;

@Data
public class SignatureRequestDTO {
    private Long workerId;
    private Long documentId;
    private Long userId; // Added to track which user is performing the signing
    private String prenom;
    private String nom;
    private String signatureImage; // Base64 encoded image
    private Date signatureDate;
}
