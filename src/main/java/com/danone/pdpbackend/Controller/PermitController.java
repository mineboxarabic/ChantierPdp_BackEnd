package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.PermitService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.entities.Permit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/permit")
public class PermitController {

    @Autowired
    private PermitService permitService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Permit>>> getAllPermits() {
        return ResponseEntity.ok(new ApiResponse<>(permitService.getAllPermits(), "Permits fetched"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Permit>> getPermitById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(permitService.getPermitById(id), "Permit fetched"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Permit>> createPermit(@RequestBody Permit permit) {
        return ResponseEntity.ok(new ApiResponse<>(permitService.createPermit(permit), "Permit created"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Permit>> updatePermit(@PathVariable Long id, @RequestBody Permit permitDetails) {
        try {
            return ResponseEntity.ok(new ApiResponse<>(permitService.updatePermit(id, permitDetails), "Permit updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public void deletePermit(@PathVariable Long id) {
        permitService.delete(id);
    }



    //Get from a list of ids
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<Permit>>> getPermitsByIds(@RequestBody List<Long> ids) {
        List<Permit> permits = permitService.getPermitsByIds(ids);
        return ResponseEntity.ok(new ApiResponse<>(permits, "Permit fetched"));
    }
}
