package com.danone.pdpbackend.Controller;


import com.danone.pdpbackend.Services.DocumentSignatureService;
import com.danone.pdpbackend.Services.WorkerService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.mappers.WorkerMapper;
import com.danone.pdpbackend.entities.dto.SignatureRequestDTO;
import com.danone.pdpbackend.entities.dto.WorkerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/worker")
@Slf4j
public class WorkerController {
    private final WorkerService workerService;
    private final WorkerMapper workerMapper;
    private final DocumentSignatureService documentSignatureService;

    public WorkerController(WorkerService workerService, WorkerMapper workerMapper, DocumentSignatureService documentSignatureService) {
        this.workerService = workerService;
        this.workerMapper = workerMapper;
        this.documentSignatureService = documentSignatureService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkerDTO>>> getAllWorkers() {
        if(workerService.getAll().isEmpty()) {
            return ResponseEntity.ok(new ApiResponse<>(List.of(), "No workers found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(workerMapper.toDTOList(workerService.getAll()), "Workers fetched"));
    }

    @PostMapping("/")
    public ResponseEntity<ApiResponse<WorkerDTO>> createWorker(@RequestBody WorkerDTO worker) {
        if(!workerService.filters(worker)) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, "Worker already exists"));
        }
        return ResponseEntity.ok(new ApiResponse<>(workerMapper.toDTO(workerService.create(workerMapper.toEntity(worker))), "Worker created"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkerDTO>> updateWorker(@PathVariable Long id, @RequestBody WorkerDTO workerDetails) {
        try {
            if(!workerService.filters(workerDetails)){
                return ResponseEntity.badRequest().body(new ApiResponse<>(null, "Error in the fields"));
            }
            if (!workerService.exists(id)) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(new ApiResponse<>(workerMapper.toDTO(workerService.update(id, workerMapper.toEntity(workerDetails))), "Worker updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteWorker(@PathVariable Long id) {

        if(!workerService.exists(id)) {
            return ResponseEntity.notFound().build();
        }
        workerService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Worker deleted"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkerDTO>> getWorkerById(@PathVariable Long id) {
        if(!workerService.exists(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ApiResponse<>(workerMapper.toDTO(workerService.getById(id)), "Worker fetched"));
    }

/*    @PostMapping("/{workerId}/sign")
    public ResponseEntity<ApiResponse<String>> signDocument(@PathVariable Long workerId,
            @RequestBody SignatureRequestDTO signatureRequest) {
        try {
            signatureRequest.setWorkerId(workerId);
            documentSignatureService.signDocument(signatureRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(null,"Document signed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null,e.getMessage()));
        }
    }*/

  /*  @DeleteMapping("/{workerId}/unsign/{signatureId}")
    public ResponseEntity<ApiResponse<String>> unsignDocument(
            @PathVariable Long workerId,
            @PathVariable Long signatureId) {
        try {
            documentSignatureService.unSignDocument(workerId, signatureId);
            return ResponseEntity.ok(new ApiResponse<>(null,"Document unsigned successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null,e.getMessage()));
        }
    }*/
}
