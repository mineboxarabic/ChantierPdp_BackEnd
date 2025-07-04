package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.LocalisationService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.mappers.LocalisationMapper;
import com.danone.pdpbackend.entities.Localisation;
import com.danone.pdpbackend.entities.dto.LocalisationDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/localisation")
public class LocalisationController {

    private final LocalisationService localisationService;
    private final LocalisationMapper localisationMapper;

    public LocalisationController(LocalisationService localisationService, LocalisationMapper localisationMapper) {
        this.localisationService = localisationService;
        this.localisationMapper = localisationMapper;
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<LocalisationDTO>>> getAllLocalisations() {
        return ResponseEntity.ok(new ApiResponse<>(localisationMapper.toDTOList(localisationService.getAll()), "Localisations fetched"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LocalisationDTO>> getLocalisationById(@PathVariable Long id) {
        Localisation localisation = localisationService.getById(id);
        if (localisation == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(null, "Localisation not found"));
        }
        return ResponseEntity.ok(
                new ApiResponse<>(localisationMapper.toDTO(localisation), "Localisation fetched"));    }

    @PostMapping
    public ResponseEntity<ApiResponse<LocalisationDTO>> createLocalisation(@RequestBody LocalisationDTO localisation) {
        return ResponseEntity.ok(new ApiResponse<>(localisationMapper.toDTO(localisationService.create(localisationMapper.toEntity(localisation))), "Localisation created"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<LocalisationDTO>> updateLocalisation(@PathVariable Long id, @RequestBody LocalisationDTO localisationDetails) {
        try {

            Localisation existingLocalisation = localisationService.getById(id);
            if (existingLocalisation == null) {
                return ResponseEntity.notFound().build();
            }
            localisationMapper.updateEntity(localisationDetails, existingLocalisation);
            Localisation updatedLocalisation = localisationService.update(id, existingLocalisation);
            return ResponseEntity.ok(new ApiResponse<>(localisationMapper.toDTO(updatedLocalisation), "Localisation updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public void deleteLocalisation(@PathVariable Long id) {
    localisationService.delete(id);
    }
}
