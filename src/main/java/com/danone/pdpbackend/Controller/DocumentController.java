package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.SignatureService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.entities.dto.SignatureRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final SignatureService signatureService;

    @PostMapping("/{documentId}/sign")
    public ResponseEntity<ApiResponse<String>> signDocument(
            @PathVariable Long documentId,
            @RequestBody SignatureRequestDTO signatureRequest) {
        try {
            signatureRequest.setDocumentId(documentId);
            signatureService.signDocument(signatureRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(null, "Document signed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, e.getMessage()));
        } catch (Exception e) {
            log.error("Error signing document: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Internal server error"));
        }
    }

    @DeleteMapping("/{documentId}/unsign/{signatureId}")
    public ResponseEntity<ApiResponse<String>> unsignDocument(
            @PathVariable Long documentId,
            @PathVariable Long signatureId,
            @RequestParam Long workerId) {
        try {
            signatureService.unSignDocument(workerId, signatureId);
            return ResponseEntity.ok(new ApiResponse<>(null,"Document unsigned successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null,e.getMessage()));
        } catch (Exception e) {
            log.error("Error unsigning document: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null,"Internal server error"));
        }
    }

    @PostMapping("/{documentId}/sign/worker/{workerId}")
    public ResponseEntity<ApiResponse<String>> signDocumentByWorker(
            @PathVariable Long documentId,
            @PathVariable Long workerId,
            @RequestBody SignatureRequestDTO signatureRequest) {
        try {
            signatureRequest.setDocumentId(documentId);
            signatureRequest.setWorkerId(workerId);
            signatureService.signDocument(signatureRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(null,"Document signed by worker successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null,e.getMessage()));
        } catch (Exception e) {
            log.error("Error signing document by worker: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null,"Internal server error"));
        }
    }
}
