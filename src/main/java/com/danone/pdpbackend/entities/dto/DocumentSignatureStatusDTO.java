package com.danone.pdpbackend.entities.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentSignatureStatusDTO {
    private Long documentId;
    private String documentType;
    private int requiredSignatures;
    private int currentSignatures;
    private boolean isFullySigned;
    private List<SignatureInfoDTO> signatures;
    private List<WorkerSignatureStatusDTO> missingSignatures;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SignatureInfoDTO {
        private Long signatureId;
        private Long workerId;
        private String workerName;
        private String signatureDate;
        private boolean active;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorkerSignatureStatusDTO {
        private Long workerId;
        private String workerName;
        private String role;
        private boolean hasSigned;
    }
}
