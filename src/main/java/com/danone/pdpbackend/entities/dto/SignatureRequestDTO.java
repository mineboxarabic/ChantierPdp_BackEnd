package com.danone.pdpbackend.entities.dto;

import lombok.Data;

@Data
public class SignatureRequestDTO {
    private Long workerId;
    private Long documentId;
    private Long userId; // Added to track which user is performing the signing
    private String name;
    private String lastName;
    private String signatureImage; // Base64 encoded image
}
