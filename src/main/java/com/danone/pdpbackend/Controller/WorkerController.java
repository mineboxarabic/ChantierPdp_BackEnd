package com.danone.pdpbackend.Controller;


import com.danone.pdpbackend.Services.WorkerService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.entities.Worker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/worker")
@Slf4j
public class WorkerController {
    private final WorkerService workerService;
    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }
    @GetMapping
    public ResponseEntity<ApiResponse<List<Worker>>> getAllWorkers() {
        //return new ApiResponse<>(risqueService.getAllRisques(), "Risques fetched");
        return ResponseEntity.ok(new ApiResponse<>(workerService.getAll(), "Workers fetched"));
    }
    @PostMapping
    public ResponseEntity<ApiResponse<Worker>> createWorker(@RequestBody Worker worker) {
        return ResponseEntity.ok(new ApiResponse<>(workerService.create(worker), "Worker created"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Worker>> updateWorker(@PathVariable Long id, @RequestBody Worker workerDetails) {
        try {
            return ResponseEntity.ok(new ApiResponse<>(workerService.update(id, workerDetails), "Worker updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteWorker(@PathVariable Long id) {
        workerService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Worker deleted"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Worker>> getWorkerById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(workerService.getById(id), "Worker fetched"));
    }



}
