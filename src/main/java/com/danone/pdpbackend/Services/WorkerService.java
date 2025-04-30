package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.Worker;

import java.util.List;

public interface WorkerService extends Service<Worker>{
    Worker findByUsername(String username);
    List<Worker> getWorkersByIds(List<Long> workers);

    List<Worker> findByIds(List<Long> signatures);
}
