package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.BdtService;
import com.danone.pdpbackend.Utils.ApiResponse;

import com.danone.pdpbackend.Utils.mappers.BdtMapper;
import com.danone.pdpbackend.entities.Bdt;
import com.danone.pdpbackend.entities.dto.BdtDTO;
import com.danone.pdpbackend.entities.dto.SignatureRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/bdt")
public class BdtController {

    private final BdtService bdtService;
    private final BdtMapper bdtMapper;

    public BdtController(BdtService bdtService, BdtMapper bdtMapper) {
        this.bdtService = bdtService;
        this.bdtMapper = bdtMapper;
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<BdtDTO>>> getAllBDT() {
        return ResponseEntity.ok(new ApiResponse<>(bdtMapper.toDTOList(bdtService.getAll()), "BDTs fetched successfully"));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<BdtDTO>> createBDT(@RequestBody BdtDTO bdt) {
        Bdt createdBdt = bdtService.create(bdtMapper.toEntity(bdt));
        return ResponseEntity.ok(new ApiResponse<>(bdtMapper.toDTO(createdBdt), "BDT created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BdtDTO>> getBDT(@PathVariable Long id) {
    Bdt bdt = bdtService.getById(id);
        if (bdt == null) {
            return ResponseEntity.status(404).body(new ApiResponse<>(null, "BDT not found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(bdtMapper.toDTO(bdt), "BDT fetched successfully"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<BdtDTO>> updateBDT(@PathVariable Long id, @RequestBody BdtDTO bdt) {
        Bdt existingBdt = bdtService.getById(id);
        Bdt updatedBdt = bdtService.update(id, bdtMapper.updateEntityFromDTO(existingBdt, bdt));
        return ResponseEntity.ok(new ApiResponse<>(bdtMapper.toDTO(updatedBdt), "BDT updated successfully"));
    }

    @DeleteMapping("/{id}")
    public void deleteBDT(@PathVariable Long id) {
        bdtService.delete(id);
    }

    @GetMapping("/chantier/{chantierId}/date/{date}")
    public ResponseEntity<ApiResponse<BdtDTO>> getBDTByChantierAndDate(@PathVariable Long chantierId, @PathVariable String date) {
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, "Invalid date format"));
        }
        Optional<Bdt> bdt = bdtService.findByChantierIdAndDate(chantierId, parsedDate);
        if (bdt.isEmpty()) {
            return ResponseEntity.status(404).body(new ApiResponse<>(null, "BDT not found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(bdtMapper.toDTO(bdt.get()), "BDTs fetched successfully"));
    }

    @GetMapping("/chantier/{chantierId}")
    public ResponseEntity<ApiResponse<List<BdtDTO>>> getBDTByChantier(@PathVariable Long chantierId) {
        List<Bdt> bdts = bdtService.findByChantierId(chantierId);
        if (bdts.isEmpty()) {
            return ResponseEntity.status(404).body(new ApiResponse<>(null, "No BDTs found for this chantier"));
        }
        return ResponseEntity.ok(new ApiResponse<>(bdtMapper.toDTOList(bdts), "BDTs fetched successfully"));
    }


}
