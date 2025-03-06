package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.DispositifService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.entities.Dispositif;
import com.danone.pdpbackend.entities.Dispositif;
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
    public ResponseEntity<ApiResponse<List<Dispositif>>> getAllDispositifs() {
        return ResponseEntity.ok(new ApiResponse<>(dispositifService.getAllDispositifs(), "Rispositifs fetched"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Dispositif>> getDispositifById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(dispositifService.getDispositifById(id), "Dispositif fetched"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Dispositif>> createDispositif(@RequestBody Dispositif dispositif) {
        return ResponseEntity.ok(new ApiResponse<>(dispositifService.createDispositif(dispositif), "Dispositif created"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Dispositif>> updateDispositif(@PathVariable Long id, @RequestBody Dispositif dispositifDetails) {
        try {
            return ResponseEntity.ok(new ApiResponse<>(dispositifService.updateDispositif(id, dispositifDetails), "Dispositif updated"));
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

}
