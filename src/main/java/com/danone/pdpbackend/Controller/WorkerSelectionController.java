package com.danone.pdpbackend.Controller;


import com.danone.pdpbackend.Services.WorkerSelectionService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.mappers.ChantierMapper;
import com.danone.pdpbackend.Utils.mappers.WorkerChantierSelectionMapper;
import com.danone.pdpbackend.Utils.mappers.WorkerMapper;
import com.danone.pdpbackend.entities.Chantier;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.WorkerChantierSelection;
import com.danone.pdpbackend.entities.dto.ChantierDTO;
import com.danone.pdpbackend.entities.dto.WorkerDTO;
import com.danone.pdpbackend.entities.dto.WorkerChantierSelectionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/worker-selection")
public class WorkerSelectionController {

    private static final Logger log = LoggerFactory.getLogger(WorkerSelectionController.class);
    @Autowired
    private WorkerSelectionService selectionService;
    @Autowired
    private WorkerChantierSelectionMapper workerChantierSelectionMapper;
    @Autowired
    private WorkerMapper workerMapper;
    @Autowired
    private ChantierMapper chantierMapper;

    @PostMapping("/select")
    public ResponseEntity<ApiResponse<WorkerChantierSelectionDTO>> selectWorker(@RequestBody Map<String, Object> request) {

        Long workerId = Long.valueOf(request.get("workerId").toString());
        Long chantierId = Long.valueOf(request.get("chantierId").toString());
        String note = request.get("note") != null ? request.get("note").toString() : null;

        WorkerChantierSelectionDTO selection = workerChantierSelectionMapper.toDTO(selectionService.selectWorkerForChantier(workerId, chantierId, note));

        return ResponseEntity.ok(new ApiResponse<>(selection, "Worker selected successfully"));
    }

    @PostMapping("/deselect")
    public ResponseEntity<ApiResponse<?>> deselectWorker(@RequestBody Map<String, Object> request) {
        Long workerId = Long.valueOf(request.get("workerId").toString());
        Long chantierId = Long.valueOf(request.get("chantierId").toString());

        selectionService.deselectWorkerFromChantier(workerId, chantierId);
        return ResponseEntity.ok(new ApiResponse<>(null, "deslected"));
    }

    @GetMapping("/chantier/{chantierId}/workers")
    public ResponseEntity<ApiResponse<List<WorkerDTO>>> getWorkersForChantier(@PathVariable Long chantierId) {
        List<Worker> workers = selectionService.getWorkersForChantier(chantierId);
        log.info("Workers for chantier {}: {}", chantierId, workers);
        return ResponseEntity.ok(new ApiResponse<>(workerMapper.toDTOList(workers), "Workers fetched"));
    }

    @GetMapping("/worker/{workerId}/chantiers")
    public ResponseEntity<ApiResponse<List<ChantierDTO>>> getChantiersForWorker(@PathVariable Long workerId) {
        List<Chantier> chantiers = selectionService.getChantiersForWorker(workerId);
        return ResponseEntity.ok(new ApiResponse<>(chantierMapper.toDTOList(chantiers), "Chantiers fetched"));
    }

    @GetMapping("/chantier/{chantierId}/selections")
    public ResponseEntity<ApiResponse<List<WorkerChantierSelectionDTO>>> getSelectionsForChantier(@PathVariable Long chantierId) {
        List<WorkerChantierSelection> chantiers = selectionService.getSelectionsForChantier(chantierId);
        return ResponseEntity.ok(new ApiResponse<>(workerChantierSelectionMapper.toDTOList(chantiers), "Chantiers fetched"));
    }
}