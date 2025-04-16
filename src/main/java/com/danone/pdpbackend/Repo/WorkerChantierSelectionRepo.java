package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.entities.Chantier;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.WorkerChantierSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkerChantierSelectionRepo extends org.springframework.data.repository.Repository<WorkerChantierSelection, Long> {

    List<WorkerChantierSelection> findByChantierAndIsSelectedTrue(Chantier chantier);

    List<WorkerChantierSelection> findByWorkerAndIsSelectedTrue(Worker worker);

    @Query("SELECT wcs FROM WorkerChantierSelection wcs WHERE wcs.worker = :worker AND wcs.chantier = :chantier")
    Optional<WorkerChantierSelection> findByWorkerAndChantier(Worker worker, Chantier chantier);

    @Query("SELECT wcs FROM WorkerChantierSelection wcs WHERE wcs.chantier.id = :chantierId AND wcs.isSelected = true")
    List<WorkerChantierSelection> findSelectedWorkersByChantier(Long chantierId);

    @Query("SELECT wcs FROM WorkerChantierSelection wcs WHERE wcs.worker.id = :workerId AND wcs.isSelected = true")
    List<WorkerChantierSelection> findSelectedChantiersByWorker(Long workerId);


    WorkerChantierSelection save(WorkerChantierSelection selection);


    List<WorkerChantierSelection> findWorkerChantierSelectionsByIdIn(Collection<Long> ids);

    List<WorkerChantierSelection> findWorkerChantierSelectionsByChantier_Id(Long chantierId);
}