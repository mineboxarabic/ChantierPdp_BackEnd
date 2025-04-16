package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.Worker;

import java.util.List;

public interface WorkerService {
    Worker save(Worker worker);
    List<Worker> findAll();
    Worker findById(Long id);
    Worker findByUsername(String username);
    void delete(Long id);

    Worker update(Long id, Worker workerDetails);

    List<Worker> getWorkersByIds(List<Long> workers);
}
