package com.danone.pdpbackend.Controller;


import com.danone.pdpbackend.Services.WorkerService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.mappers.WorkerMapper;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.WorkerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/worker")
@Slf4j
public class WorkerController {
    private final WorkerService workerService;
    private final WorkerMapper workerMapper;

    public WorkerController(WorkerService workerService, WorkerMapper workerMapper) {
        this.workerService = workerService;
        this.workerMapper = workerMapper;
    }
    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkerDTO>>> getAllWorkers() {
        //return new ApiResponse<>(risqueService.getAllRisques(), "Risques fetched");
        return ResponseEntity.ok(new ApiResponse<>(workerMapper.toDTOList(workerService.getAll()), "Workers fetched"));
    }

    @PostMapping("/")
    public ResponseEntity<ApiResponse<WorkerDTO>> createWorker(@RequestBody WorkerDTO worker) {
        return ResponseEntity.ok(new ApiResponse<>(workerMapper.toDTO(workerService.create(workerMapper.toEntity(worker))), "Worker created"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkerDTO>> updateWorker(@PathVariable Long id, @RequestBody WorkerDTO workerDetails) {
        try {
            return ResponseEntity.ok(new ApiResponse<>(workerMapper.toDTO(workerService.update(id, workerMapper.toEntity(workerDetails))), "Worker updated"));
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
    public ResponseEntity<ApiResponse<WorkerDTO>> getWorkerById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(workerMapper.toDTO(workerService.getById(id)), "Worker fetched"));
    }



}
