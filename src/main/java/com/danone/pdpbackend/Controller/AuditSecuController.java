package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.AuditSecuService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.entities.AuditSecu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditsecu")
public class AuditSecuController {
    @Autowired
    private AuditSecuService auditSecuService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditSecu>>> getAllAuditSecus() {
        return ResponseEntity.ok(new ApiResponse<>(auditSecuService.getAllAuditSecus(), "AuditSecus fetched"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditSecu>> getAuditSecuById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(auditSecuService.getAuditSecuById(id), "AuditSecu fetched"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AuditSecu>> createAuditSecu(@RequestBody AuditSecu auditSecu) {
        return ResponseEntity.ok(new ApiResponse<>(auditSecuService.createAuditSecu(auditSecu), "AuditSecu created"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditSecu>> updateAuditSecu(@PathVariable Long id, @RequestBody AuditSecu auditSecuDetails) {
        try {
            return ResponseEntity.ok(new ApiResponse<>(auditSecuService.updateAuditSecu(id, auditSecuDetails), "AuditSecu updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteAuditSecu(@PathVariable Long id) {
        if (!auditSecuService.deleteAuditSecu(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "AuditSecu deleted"));
    }
}