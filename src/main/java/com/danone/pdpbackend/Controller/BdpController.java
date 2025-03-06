package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.BDTService;
import com.danone.pdpbackend.Utils.ApiResponse;

import com.danone.pdpbackend.entities.BDT.BDT;
import com.danone.pdpbackend.entities.Risque;
import com.danone.pdpbackend.entities.AuditSecu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bdt")
public class BdpController {

    private final BDTService bdtService;

    public BdpController(BDTService bdtService) {
        this.bdtService = bdtService;
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<BDT>>> getAllBDT() {
        return ResponseEntity.ok(new ApiResponse<>(bdtService.getAllBDT(), "BDTs fetched successfully"));
    }

    @PostMapping("/")
    public ResponseEntity<ApiResponse<String>> createBDT(@RequestBody BDT bdt) {
        bdtService.createBDT(bdt);
        return ResponseEntity.ok(new ApiResponse<>(null, "BDT created successfully"));
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
    public ResponseEntity<ApiResponse<Risque>> addRisqueToBDT(@PathVariable Long bdtId, @PathVariable Long risqueId) {
        return ResponseEntity.ok(new ApiResponse<>(bdtService.addRisqueToBDT(bdtId, risqueId), "Risque added to BDT successfully"));
    }

    @PostMapping("/{bdtId}/audit/{auditId}")
    public ResponseEntity<ApiResponse<AuditSecu>> addAuditToBDT(@PathVariable Long bdtId, @PathVariable Long auditId) {
        return ResponseEntity.ok(new ApiResponse<>(bdtService.addAuditToBDT(bdtId, auditId), "Audit added to BDT successfully"));
    }
}
