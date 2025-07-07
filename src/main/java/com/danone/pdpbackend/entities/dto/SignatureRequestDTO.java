package com.danone.pdpbackend.entities.dto;

import lombok.Data;

@Data
public class SignatureRequestDTO {
    private Long workerId;
    private Long documentId;
    private String name;
    private String lastName;
    private String signatureImage; // Base64 encoded image
}
