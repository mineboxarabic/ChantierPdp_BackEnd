package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.WorkerRepo;
import com.danone.pdpbackend.Services.WorkerService;
import com.danone.pdpbackend.entities.Entreprise;
import com.danone.pdpbackend.entities.Worker;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;


@Service
public class WorkerServiceImpl implements WorkerService {


    private final WorkerRepo workerRepo;
    private final EntrepriseServiceImpl entrepriseServiceImpl;

    public WorkerServiceImpl(WorkerRepo workerRepo, EntrepriseServiceImpl entrepriseServiceImpl) {
        this.workerRepo = workerRepo;
        this.entrepriseServiceImpl = entrepriseServiceImpl;
    }

    @Override
    @Transactional
    public Worker create(Worker worker) {
        if (worker.getEntreprise() != null && worker.getEntreprise().getId() != null) {
            // Fetch the managed Entreprise instance within this transaction
            Entreprise entreprise = entrepriseServiceImpl.getById(worker.getEntreprise().getId());
            if (entreprise != null) {
                if (!entreprise.getWorkers().contains(worker)) {
                    entreprise.getWorkers().add(worker);
                }
                worker.setEntreprise(entreprise);
            } else {
                System.err.println("WARN: Entreprise with ID " + worker.getEntreprise().getId() + " not found while creating worker.");
                worker.setEntreprise(null); // Or handle as appropriate
            }
        }



        return workerRepo.save(worker);
    }

    @Override
    public List<Worker> getAll() {
        return workerRepo.findAllBy();
    }

    @Override
    public Worker getById(Long id) {
        return workerRepo.findById(id);
    }

    @Override
    public Worker findByUsername(String nom) {
        return workerRepo.findByNom(nom);
    }


    @Override
    public Boolean delete(Long id) {
        workerRepo.deleteById(id);
        return true;
    }

    @Override
    public List<Worker> getByIds(List<Long> ids) {
        return workerRepo.findWorkersByIdIn(ids);
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

    @Override
    public List<Worker> findByIds(List<Long> signatures) {
        return workerRepo.findWorkersByIdIn(signatures);
    }
}
