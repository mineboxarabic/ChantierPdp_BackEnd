package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.DispositifService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.entities.dto.DispositifDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dispositif")
public class DispositifController {
    @Autowired
    private DispositifService dispositifService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DispositifDTO>>> getAllDispositifs() {
        return ResponseEntity.ok(new ApiResponse<>(dispositifService.getAllDispositifs(), "Dispositifs fetched"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DispositifDTO>> getDispositifById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(dispositifService.getDispositifById(id), "Dispositif fetched"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DispositifDTO>> createDispositif(@RequestBody DispositifDTO dispositifDTO) {
        return ResponseEntity.ok(new ApiResponse<>(dispositifService.createDispositif(dispositifDTO), "Dispositif created"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<DispositifDTO>> updateDispositif(@PathVariable Long id, @RequestBody DispositifDTO dispositifDetailsDTO) {
        try {
            return ResponseEntity.ok(new ApiResponse<>(dispositifService.updateDispositif(id, dispositifDetailsDTO), "Dispositif updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteDispositif(@PathVariable Long id) {

        if (!dispositifService.deleteDispositif(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Dispositif deleted"));
    }


    //Get Dispositifs from a list of ids
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<DispositifDTO>>> getDispositifsByIds(@RequestBody List<Long> ids) {
        List<DispositifDTO> dispositifs = dispositifService.getDispositifsByIds(ids);
        return ResponseEntity.ok(new ApiResponse<>(dispositifs, "Dispositifs fetched"));
    }
}