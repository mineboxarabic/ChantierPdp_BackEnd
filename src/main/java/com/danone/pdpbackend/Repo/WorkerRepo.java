package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.entities.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Collection;
import java.util.List;

@org.springframework.stereotype.Repository
public interface WorkerRepo extends Repository<Worker, Long> {
    //CRUD

    Worker save(Worker worker);

    void deleteById(Long id);

    Worker findById(Long id);

    Worker findByNom(String nom);

    List<Worker> findAllBy();

    //Find all by a chantier id
    @Query("SELECT w FROM Worker w JOIN w.chantiers c WHERE c.id = :chantierId")
    List<Worker> findAllByChantierId(Long chantierId);

    List<Worker> findWorkersByIdIn(Collection<Long> ids);


    boolean existsById(Long id);
}
