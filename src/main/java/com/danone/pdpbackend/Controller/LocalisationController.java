package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.LocalisationService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.entities.Localisation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/localisation")
public class LocalisationController {

    private final LocalisationService localisationService;

    public LocalisationController(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Localisation>>> getAllLocalisations() {
        return ResponseEntity.ok(new ApiResponse<>(localisationService.getAllLocalisations(), "Localisations fetched"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Localisation>> getLocalisationById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(localisationService.getLocalisationById(id), "Localisation fetched"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Localisation>> createLocalisation(@RequestBody Localisation localisation) {
        return ResponseEntity.ok(new ApiResponse<>(localisationService.createLocalisation(localisation), "Localisation created"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Localisation>> updateLocalisation(@PathVariable Long id, @RequestBody Localisation localisationDetails) {
        try {
            return ResponseEntity.ok(new ApiResponse<>(localisationService.updateLocalisation(id, localisationDetails), "Localisation updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteLocalisation(@PathVariable Long id) {
        if (!localisationService.deleteLocalisation(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Localisation deleted"));
    }
}
