package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.BDTService;
import com.danone.pdpbackend.Utils.ApiResponse;

import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.entities.BDT.BDT;
import com.danone.pdpbackend.entities.ObjectAnswered;
import com.danone.pdpbackend.entities.Risque;
import com.danone.pdpbackend.entities.AuditSecu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bdt")
public class BdtController {

    private final BDTService bdtService;

    public BdtController(BDTService bdtService) {
        this.bdtService = bdtService;
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<BDT>>> getAllBDT() {
        return ResponseEntity.ok(new ApiResponse<>(bdtService.getAllBDT(), "BDTs fetched successfully"));
    }

    @PostMapping("/")
    public ResponseEntity<ApiResponse<BDT>> createBDT(@RequestBody BDT bdt) {
        BDT createdBdt = bdtService.createBDT(bdt);
        return ResponseEntity.ok(new ApiResponse<>(bdt, "BDT created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BDT>> getBDT(@PathVariable Long id) {
        BDT bdt = bdtService.getBDT(id);
        if (bdt == null) {
            return ResponseEntity.status(404).body(new ApiResponse<>(null, "BDT not found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(bdt, "BDT fetched successfully"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<BDT>> updateBDT(@PathVariable Long id, @RequestBody BDT bdt) {
        BDT updatedBDT = bdtService.updateBDT(id, bdt);
        return ResponseEntity.ok(new ApiResponse<>(updatedBDT, "BDT updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBDT(@PathVariable Long id) {
        if (!bdtService.deleteBDT(id)) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, "BDT not found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(null, "BDT deleted successfully"));
    }

    @PostMapping("/{bdtId}/risque/{risqueId}")
    public ResponseEntity<ApiResponse<ObjectAnswered>> addRisqueToBDT(@PathVariable Long bdtId, @PathVariable Long risqueId) {
        return ResponseEntity.ok(new ApiResponse<>(bdtService.addObjectAnswered(bdtId, risqueId, ObjectAnsweredObjects.RISQUE), "Risque added to BDT successfully"));
    }

    @PostMapping("/{bdtId}/audit/{auditId}")
    public ResponseEntity<ApiResponse<ObjectAnswered>> addAuditToBDT(@PathVariable Long bdtId, @PathVariable Long auditId) {
        return ResponseEntity.ok(new ApiResponse<>(bdtService.addObjectAnswered(bdtId, auditId,ObjectAnsweredObjects.AUDIT), "Audit added to BDT successfully"));
    }

    @DeleteMapping("/{bdtId}/risque/{risqueId}")
    public ResponseEntity<ApiResponse<ObjectAnswered>> removeRisqueFromBDT(@PathVariable Long bdtId, @PathVariable Long risqueId) {
        return ResponseEntity.ok(new ApiResponse<>(bdtService.removeObjectAnswered(bdtId, risqueId, ObjectAnsweredObjects.RISQUE), "Risque removed from BDT successfully"));
    }

    @DeleteMapping("/{bdtId}/audit/{auditId}")
    public ResponseEntity<ApiResponse<ObjectAnswered>> removeAuditFromBDT(@PathVariable Long bdtId, @PathVariable Long auditId) {
        return ResponseEntity.ok(new ApiResponse<>(bdtService.removeObjectAnswered(bdtId, auditId, ObjectAnsweredObjects.AUDIT), "Audit removed from BDT successfully"));
    }
}
