package com.danone.pdpbackend.Controller;


import com.danone.pdpbackend.Services.WorkerSelectionService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.entities.Chantier;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.WorkerChantierSelection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/worker-selection")
public class WorkerSelectionController {

    @Autowired
    private WorkerSelectionService selectionService;

    @PostMapping("/select")
    public ResponseEntity<ApiResponse<WorkerChantierSelection>> selectWorker(
            @RequestBody Map<String, Object> request) {

        Long workerId = Long.valueOf(request.get("workerId").toString());
        Long chantierId = Long.valueOf(request.get("chantierId").toString());
        String note = request.get("note") != null ? request.get("note").toString() : null;

        WorkerChantierSelection selection = selectionService.selectWorkerForChantier(workerId, chantierId, note);

        return ResponseEntity.ok(new ApiResponse<>(selection, "Worker selected successfully"));
    }

    @PostMapping("/deselect")
    public ResponseEntity<ApiResponse<?>> deselectWorker(@RequestBody Map<String, Object> request) {
        Long workerId = Long.valueOf(request.get("workerId").toString());
        Long chantierId = Long.valueOf(request.get("chantierId").toString());

        selectionService.deselectWorkerFromChantier(workerId, chantierId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/chantier/{chantierId}/workers")
    public ResponseEntity<ApiResponse<List<Worker>>> getWorkersForChantier(@PathVariable Long chantierId) {
        List<Worker> workers = selectionService.getWorkersForChantier(chantierId);
        return ResponseEntity.ok(new ApiResponse<>(workers, "Workers fetched"));
    }

    @GetMapping("/worker/{workerId}/chantiers")
    public ResponseEntity<ApiResponse<List<Chantier>>> getChantiersForWorker(@PathVariable Long workerId) {
        List<Chantier> chantiers = selectionService.getChantiersForWorker(workerId);
        return ResponseEntity.ok(new ApiResponse<>(chantiers, "Chantiers fetched"));
    }

    @GetMapping("/chantier/{chantierId}/selections")
    public ResponseEntity<ApiResponse<List<WorkerChantierSelection>>> getSelectionsForChantier(@PathVariable Long chantierId) {
        List<WorkerChantierSelection> chantiers = selectionService.getSelectionsForChantier(chantierId);
        return ResponseEntity.ok(new ApiResponse<>(chantiers, "Chantiers fetched"));
    }
}