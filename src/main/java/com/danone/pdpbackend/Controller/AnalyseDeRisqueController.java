package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.AnalyseDeRisqueService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.dto.AnalyseDeRisqueDTO;
import com.danone.pdpbackend.entities.AnalyseDeRisque;
import com.danone.pdpbackend.entities.ObjectAnsweredEntreprises;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/analyseDeRisque")
public class AnalyseDeRisqueController {

    @Autowired
    private AnalyseDeRisqueService analyseDeRisqueService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AnalyseDeRisque>>> getAllAnalyseDeRisques() {
        return ResponseEntity.ok(new ApiResponse<>(analyseDeRisqueService.getAllAnalyseDeRisques(), "AnalyseDeRisques fetched"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnalyseDeRisque>> getAnalyseDeRisqueById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(analyseDeRisqueService.getAnalyseDeRisqueById(id), "AnalyseDeRisque fetched"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AnalyseDeRisque>> createAnalyseDeRisque(@RequestBody AnalyseDeRisqueDTO analyseDeRisqueDTO) {
        return ResponseEntity.ok(new ApiResponse<>(analyseDeRisqueService.createAnalyseDeRisque(analyseDeRisqueDTO), "AnalyseDeRisque created"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AnalyseDeRisque>> updateAnalyseDeRisque(@PathVariable Long id, @RequestBody AnalyseDeRisque analyseDeRisqueDetails) {
        try {
            return ResponseEntity.ok(new ApiResponse<>(analyseDeRisqueService.updateAnalyseDeRisque(id, analyseDeRisqueDetails), "AnalyseDeRisque updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteAnalyseDeRisque(@PathVariable Long id) {
        if (!analyseDeRisqueService.deleteAnalyseDeRisque(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "AnalyseDeRisque deleted"));
    }


    //        return fetch(`api/analyseDeRisque/${analyseId}/risque/${risqueId}`, "POST", null, [
    @PostMapping("/{analyseId}/risque/{risqueId}")
    public ResponseEntity<ApiResponse<ObjectAnsweredEntreprises>> addRisqueToAnalyse(@PathVariable Long analyseId, @PathVariable Long risqueId) {

        return ResponseEntity.ok(new ApiResponse<>(analyseDeRisqueService.addRisqueToAnalyse(analyseId, risqueId), "Risque added to Analyse"));
    }

}