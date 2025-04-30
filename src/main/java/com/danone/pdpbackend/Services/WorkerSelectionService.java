package com.danone.pdpbackend.Services;
import com.danone.pdpbackend.Repo.ChantierRepo;
import com.danone.pdpbackend.Repo.WorkerChantierSelectionRepo;
import com.danone.pdpbackend.Repo.WorkerRepo;
import com.danone.pdpbackend.entities.Chantier;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.WorkerChantierSelection;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkerSelectionService {

    @Autowired
    private WorkerChantierSelectionRepo selectionRepository;

    @Autowired
    private WorkerRepo workerRepository;

    @Autowired
    private ChantierRepo chantierRepository;

    /**
     * Select a worker for a chantier
     */
    @Transactional
    public WorkerChantierSelection selectWorkerForChantier(Long workerId, Long chantierId, String note) {
        Worker worker = workerRepository.findById(workerId);

        Chantier chantier = chantierRepository.findById(chantierId)
                .orElseThrow(() -> new RuntimeException("Chantier not found with id: " + chantierId));

        // Check if a selection already exists
        Optional<WorkerChantierSelection> existingSelection = selectionRepository.findByWorkerAndChantier(worker, chantier);

        if (existingSelection.isPresent()) {
            // Update the existing selection
            WorkerChantierSelection selection = existingSelection.get();
            selection.setIsSelected(true);
            selection.setSelectionDate(new Date());
            if (note != null) {
                selection.setSelectionNote(note);
            }
            return selectionRepository.save(selection);
        } else {
            // Create a new selectionk
            WorkerChantierSelection selection = new WorkerChantierSelection();
            selection.setWorker(worker);
            selection.setChantier(chantier);
            selection.setIsSelected(true);
            selection.setSelectionDate(new Date());
            selection.setSelectionNote(note);
            return selectionRepository.save(selection);
        }
    }

    /**
     * Deselect a worker from a chantier
     */
    @Transactional
    public void deselectWorkerFromChantier(Long workerId, Long chantierId) {
        Optional<WorkerChantierSelection> existingSelection = selectionRepository.findByWorkerAndChantier(workerRepository.findById(workerId), chantierRepository.findById(chantierId).orElseThrow());

        if (existingSelection.isPresent()) {
            WorkerChantierSelection selection = existingSelection.get();
            selection.setIsSelected(false);
            selectionRepository.save(selection);
        }else{
            throw new RuntimeException("Selection not found for worker ID: " + workerId + " and chantier ID: " + chantierId);
        }
    }

    /**
     * Get all workers selected for a chantier
     */
    public List<Worker> getWorkersForChantier(Long chantierId) {
        // Fetch the Chantier entity first
        Chantier chantier = chantierRepository.findById(chantierId)
                .orElseThrow(() -> new RuntimeException("Chantier not found with id: " + chantierId));

        // Query the selection repository for active selections for this chantier
        // Ensure you have a method like this in WorkerChantierSelectionRepo
        List<WorkerChantierSelection> selections = selectionRepository.findByChantierAndIsSelectedTrue(chantier);

        // Map the selections to Worker entities
        return selections.stream()
                .map(WorkerChantierSelection::getWorker) // Get the worker from each selection
                .filter(Objects::nonNull) // Filter out any selections with null workers (shouldn't happen ideally)
                .distinct() // Avoid potential duplicates
                .collect(Collectors.toList());    }

    /**
     * Get all chantiers a worker is selected for
     */
    public List<Chantier> getChantiersForWorker(Long workerId) {
        Worker worker = workerRepository.findById(workerId);
        // Query the selection repository for active selections for this worker
        List<WorkerChantierSelection> selections = selectionRepository.findByWorkerAndIsSelectedTrue(worker);

        // Map the selections to Chantier entities
        return selections.stream()
                .map(WorkerChantierSelection::getChantier) // Get the chantier from each selection
                .filter(Objects::nonNull) // Filter out any selections with null chantiers (shouldn't happen ideally)
                .distinct() // Avoid potential duplicates
                .collect(Collectors.toList());
    }

    public List<WorkerChantierSelection> getWorkerSelectionsByIds(List<Long> workerSelections) {
        return selectionRepository.findWorkerChantierSelectionsByIdIn(workerSelections);
    }

    public List<WorkerChantierSelection> getSelectionsForChantier(Long chantierId) {
        return selectionRepository.findWorkerChantierSelectionsByChantier_Id(chantierId);

    }
}
