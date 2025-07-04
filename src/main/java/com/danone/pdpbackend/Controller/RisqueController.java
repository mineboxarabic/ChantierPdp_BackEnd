package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.RisqueService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.entities.Risque;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risque")
public class RisqueController {
    @Autowired
    private RisqueService risqueService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Risque>>> getAllRisques() {
        return ResponseEntity.ok(new ApiResponse<>(risqueService.getAllRisques(), "Risques fetched"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Risque>> getRisqueById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(risqueService.getRisqueById(id), "Risque fetched"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Risque>> createRisque(@RequestBody Risque risque) {
        return ResponseEntity.ok(new ApiResponse<>(risqueService.createRisque(risque), "Risque created"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Risque>> updateRisque(@PathVariable Long id, @RequestBody Risque risqueDetails) {
        try {
            return ResponseEntity.ok(new ApiResponse<>(risqueService.updateRisque(id, risqueDetails), "Risque updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public void deleteRisque(@PathVariable Long id) {
        risqueService.deleteRisque(id);
    }

    //Get Risques from a list of ids
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<Risque>>> getRisquesByIds(@RequestBody List<Long> ids) {
        List<Risque> risques = risqueService.getRisquesByIds(ids);
        return ResponseEntity.ok(new ApiResponse<>(risques, "Risques fetched"));
    }




}
