package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.AuditSecuService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.AuditType;
import com.danone.pdpbackend.Utils.mappers.AuditSecuMapper;
import com.danone.pdpbackend.entities.AuditSecu;
import com.danone.pdpbackend.entities.dto.AuditSecuDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditsecu")
@RequiredArgsConstructor
public class AuditSecuController {
    private final AuditSecuService auditSecuService;
    private final AuditSecuMapper auditSecuMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditSecuDTO>>> getAllAuditSecus() {
        List<AuditSecu> auditSecus = auditSecuService.getAllAuditSecus();
        List<AuditSecuDTO> auditSecuDTOs = auditSecus.stream()
                .map(auditSecuMapper::toDTO)
                .toList();
        return ResponseEntity.ok(new ApiResponse<>(auditSecuDTOs, "AuditSecus fetched"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditSecuDTO>> getAuditSecuById(@PathVariable Long id) {
        AuditSecu auditSecu = auditSecuService.getAuditSecuById(id);
        AuditSecuDTO auditSecuDTO = auditSecuMapper.toDTO(auditSecu);
        return ResponseEntity.ok(new ApiResponse<>(auditSecuDTO, "AuditSecu fetched"));
    }

    @GetMapping("/type/{typeOfAudit}")
    public ResponseEntity<ApiResponse<List<AuditSecuDTO>>> getAuditSecusByType(@PathVariable String typeOfAudit) {
        try {
            // Convert string to enum, handling backward compatibility
            AuditType auditType = AuditType.valueOf(typeOfAudit.toUpperCase());
            List<AuditSecu> auditSecus = auditSecuService.getAuditSecusByType(auditType);
            List<AuditSecuDTO> auditSecuDTOs = auditSecus.stream()
                    .map(auditSecuMapper::toDTO)
                    .toList();
            return ResponseEntity.ok(new ApiResponse<>(auditSecuDTOs, "AuditSecus fetched by type"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, "Invalid audit type: " + typeOfAudit));
        }
    }



    @PostMapping
    public ResponseEntity<ApiResponse<AuditSecuDTO>> createAuditSecu(@RequestBody AuditSecuDTO auditSecuDTO) {
        AuditSecu auditSecu = auditSecuMapper.toEntity(auditSecuDTO);
        AuditSecu createdAuditSecu = auditSecuService.createAuditSecu(auditSecu);
        AuditSecuDTO createdAuditSecuDTO = auditSecuMapper.toDTO(createdAuditSecu);
        return ResponseEntity.ok(new ApiResponse<>(createdAuditSecuDTO, "AuditSecu created"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditSecuDTO>> updateAuditSecu(@PathVariable Long id, @RequestBody AuditSecuDTO auditSecuDetails) {
        try {
            AuditSecu auditSecuEntity = auditSecuMapper.toEntity(auditSecuDetails);
            AuditSecu updatedAuditSecu = auditSecuService.updateAuditSecu(id, auditSecuEntity);
            AuditSecuDTO updatedAuditSecuDTO = auditSecuMapper.toDTO(updatedAuditSecu);
            return ResponseEntity.ok(new ApiResponse<>(updatedAuditSecuDTO, "AuditSecu updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteAuditSecu(@PathVariable Long id) {
        Boolean result = auditSecuService.deleteAuditSecu(id);
        if (!result.booleanValue()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "AuditSecu deleted"));
    }
}