package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.ChantierService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.mappers.ChantierMapper;
import com.danone.pdpbackend.entities.Chantier;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.ChantierDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chantier")
public class ChantierController {

    private final ChantierService chantierService;
    private final ChantierMapper chantierMapper;

    public ChantierController(ChantierService chantierService, ChantierMapper chantierMapper1) {
        this.chantierService = chantierService;
        this.chantierMapper = chantierMapper1;
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ChantierDTO>>> getAllChantiers() {
        return ResponseEntity.ok(new ApiResponse<>(chantierService.getAll().stream().map(chantierMapper::toDTO).toList()
                , "Chantiers fetched successfully"));
    }

    @PostMapping("/")
    public ResponseEntity<ApiResponse<ChantierDTO>> saveChantier(@RequestBody ChantierDTO chantierDTO) {
        Chantier createdChantier = chantierService.create(chantierMapper.toEntity(chantierDTO));
        return ResponseEntity.ok(new ApiResponse<>(chantierMapper.toDTO(createdChantier), "Chantier saved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChantierDTO>> getChantier(@PathVariable Long id) {
        Chantier chantier = chantierService.getById(id);
        if (chantier == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(null, "Chantier not found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(chantierMapper.toDTO(chantier), "Chantier fetched successfully"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ChantierDTO>> updateChantier(@PathVariable Long id, @RequestBody ChantierDTO chantierDTO) {
        log.info("Updating chantier with id: {}", id);
        Chantier existingChantier = chantierService.getById(id);
        Chantier updatedChantier = chantierService.update( id,chantierMapper.updateEntityFromDTO(existingChantier, chantierDTO));
        return ResponseEntity.ok(new ApiResponse<>(chantierMapper.toDTO(updatedChantier), "Chantier updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteChantier(@PathVariable Long id) {
        if (!chantierService.delete(id)) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, "Chantier not found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(null, "Chantier deleted successfully"));
    }

    @GetMapping("/last")
    public ResponseEntity<ApiResponse<Long>> getLastId() {
        return ResponseEntity.ok(new ApiResponse<>(chantierService.getLastId(), "Last id fetched successfully"));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<ChantierDTO>>> getRecent() {
        return ResponseEntity.ok(new ApiResponse<>(chantierService.getRecent().stream().map(chantierMapper::toDTO).toList(), "Recent chantiers fetched successfully"));
    }

    //        return fetch(`api/chantier/${chantierId}/workers`, 'GET', null, [
    @GetMapping("/{chantierId}/workers")
    public ResponseEntity<ApiResponse<List<Worker>>> getWorkersByChantier(@PathVariable Long chantierId) {
        return ResponseEntity.ok(new ApiResponse<>(chantierService.getWorkersByChantier(chantierId), "Workers fetched successfully"));
    }


    @GetMapping("/{id}/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChantierStats(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(chantierService.getChantierStats(id), "Chantier stats fetched successfully"));
    }

    @PostMapping("/{id}/update-status")
    public ResponseEntity<ApiResponse<ChantierDTO>> updateChantierStatus(@PathVariable Long id, @RequestBody String newStatus) {
        Chantier updatedChantier = chantierService.updateAndSaveChantierStatus(id);
        return ResponseEntity.ok(new ApiResponse<>(chantierMapper.toDTO(updatedChantier), "Chantier status updated successfully"));
    }


    @GetMapping("/{id}/requires-pdp")
    public ResponseEntity<ApiResponse<Boolean>> requiresPdp(@PathVariable Long id) {
        boolean requiresPdp = chantierService.requiresPdp(id);
        return ResponseEntity.ok(new ApiResponse<>(requiresPdp, "PDP requirement status fetched successfully"));
    }
}
