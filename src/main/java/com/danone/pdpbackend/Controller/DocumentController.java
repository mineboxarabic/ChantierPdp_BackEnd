package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.DocumentSignatureService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.entities.DocumentSignature;
import com.danone.pdpbackend.entities.dto.DocumentSignatureDTO;
import com.danone.pdpbackend.entities.dto.SignatureRequestDTO;
import com.danone.pdpbackend.entities.dto.SignatureResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentSignatureService documentSignatureService;

/*    @PostMapping("/{documentId}/sign")
    public ResponseEntity<ApiResponse<String>> signDocument(
            @PathVariable Long documentId,
            @RequestBody SignatureRequestDTO signatureRequest) {
        try {
            signatureRequest.setDocumentId(documentId);
            documentSignatureService.signDocument(signatureRequest);
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
*/

    @DeleteMapping("/worker/{workerId}/unsign/{signatureId}")
    public ResponseEntity<ApiResponse<String>> unsignDocument(
            @PathVariable Long signatureId,
            @PathVariable Long workerId) {
        try {
            documentSignatureService.unSignDocumentByWorker(workerId, signatureId);
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

    @PostMapping("/worker/sign")
    public ResponseEntity<ApiResponse<Long>> signDocumentByWorker(
            @RequestBody SignatureRequestDTO signatureRequest) 
            {
                
        try {
            Long signatureId = documentSignatureService.signDocumentByWorkerAndReturnId(signatureRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(signatureId,"Document signed by worker successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null,e.getMessage()));
        } catch (Exception e) {
            log.error("Error signing document by worker: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null,"Internal server error"));
        }
    }

    @PostMapping("/user/sign")
    public ResponseEntity<ApiResponse<Long>> signDocumentByUser(
            @RequestBody SignatureRequestDTO signatureRequest) {
        try {
            Long signatureId = documentSignatureService.signDocumentByUser(signatureRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(signatureId,"Document signed by user successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null,e.getMessage()));
        } catch (Exception e) {
            log.error("Error signing document by user: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null,"Internal server error"));
        }
    }

    @DeleteMapping("/user/{userId}/unsign/{signatureId}")
    public ResponseEntity<ApiResponse<String>> unsignDocumentByUser(
            @PathVariable Long signatureId,
            @PathVariable Long userId) {
        try {
            documentSignatureService.unSignDocumentByUser(userId, signatureId);
            return ResponseEntity.ok(new ApiResponse<>(null,"Document unsigned by user successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null,e.getMessage()));
        } catch (Exception e) {
            log.error("Error unsigning document by user: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null,"Internal server error"));
        }
    }


    @GetMapping("/{documentId}/signatures")
    public ResponseEntity<ApiResponse<List<SignatureResponseDTO>>> getSignaturesByDocumentId(@PathVariable Long documentId) {
        try {
            List<SignatureResponseDTO> signatures = documentSignatureService.getSignaturesByDocumentId(documentId);
            return ResponseEntity.ok(new ApiResponse<>(signatures, "Signatures fetched successfully"));
        } catch (Exception e) {
            log.error("Error fetching signatures: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Internal server error"));
        }
    }

}
