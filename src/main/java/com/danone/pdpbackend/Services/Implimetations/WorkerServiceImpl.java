package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.WorkerRepo;
import com.danone.pdpbackend.Services.WorkerService;
import com.danone.pdpbackend.entities.Worker;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;


@Service
public class WorkerServiceImpl implements WorkerService {


    private final WorkerRepo workerRepo;

    public WorkerServiceImpl(WorkerRepo workerRepo) {
        this.workerRepo = workerRepo;
    }

    @Override
    public Worker save(Worker worker) {
        return workerRepo.save(worker);
    }

    @Override
    public List<Worker> findAll() {
        return workerRepo.findAllBy();
    }

    @Override
    public Worker findById(Long id) {
        return workerRepo.findById(id);
    }

    @Override
    public Worker findByUsername(String nom) {
        return workerRepo.findByNom(nom);
    }


    @Override
    public void delete(Long id) {
        workerRepo.deleteById(id);
    }

    @Override
    public Worker update(Long id, Worker workerDetails) {
        Worker worker = workerRepo.findById(id);
        if (worker == null) {
            return null;
        }

        for(Field field : workerDetails.getClass().getDeclaredFields()){
            field.setAccessible(true);
            try {
                Object value = field.get(workerDetails);
                if(value != null){
                    field.set(worker, value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return workerRepo.save(worker);
    }

    @Override
    public List<Worker> getWorkersByIds(List<Long> workers) {

        return workerRepo.findWorkersByIdIn(workers);
    }
}
