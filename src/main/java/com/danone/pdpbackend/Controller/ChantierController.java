package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.ChantierService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.entities.Chantier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chantier")
public class ChantierController {

    private final ChantierService chantierService;

    public ChantierController(ChantierService chantierService) {
        this.chantierService = chantierService;
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Chantier>>> getAllChantiers() {
        return ResponseEntity.ok(new ApiResponse<>(chantierService.getAllChantiers(), "Chantiers fetched successfully"));
    }

    @PostMapping("/")
    public ResponseEntity<ApiResponse<Chantier>> saveChantier(@RequestBody Chantier chantier) {
        Chantier createdChantier = chantierService.createChantier(chantier);
        return ResponseEntity.ok(new ApiResponse<>(createdChantier, "Chantier saved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Chantier>> getChantier(@PathVariable Long id) {
        Chantier chantier = chantierService.getChantier(id);
        if (chantier == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(null, "Chantier not found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(chantier, "Chantier fetched successfully"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Chantier>> updateChantier(@PathVariable Long id, @RequestBody Chantier chantier) {
        log.info("Updating chantier with id: {}", id);
        Chantier updatedChantier = chantierService.updateChantier(chantier, id);
        return ResponseEntity.ok(new ApiResponse<>(updatedChantier, "Chantier updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteChantier(@PathVariable Long id) {
        if (!chantierService.deleteChantier(id)) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, "Chantier not found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(null, "Chantier deleted successfully"));
    }

    @GetMapping("/last")
    public ResponseEntity<ApiResponse<Long>> getLastId() {
        return ResponseEntity.ok(new ApiResponse<>(chantierService.getLastId(), "Last id fetched successfully"));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<Chantier>>> getRecent() {
        return ResponseEntity.ok(new ApiResponse<>(chantierService.getRecent(), "Recent chantiers fetched successfully"));
    }
}
