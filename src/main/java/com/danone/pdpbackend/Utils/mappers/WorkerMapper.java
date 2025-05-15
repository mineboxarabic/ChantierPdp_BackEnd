package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.Services.ChantierService;
import com.danone.pdpbackend.Services.EntrepriseService;
import com.danone.pdpbackend.Services.PdpService;
import com.danone.pdpbackend.entities.Chantier;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.WorkerDTO;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class WorkerMapper implements Mapper<WorkerDTO, Worker> {

    @Autowired
    @Lazy
    private final EntrepriseService entrepriseService;
    @Lazy
    private final PdpService pdpService;
    @Lazy
    private final ChantierService chantierService;


    @Override
    public void setDTOFields(WorkerDTO workerDTO, Worker worker) {
        if (workerDTO == null || worker == null) {
            return;
        }
        workerDTO.setId(worker.getId());
        workerDTO.setNom(worker.getNom());
        workerDTO.setPrenom(worker.getPrenom());
        //Entreprise
        if (worker.getEntreprise() != null) {
            workerDTO.setEntreprise(worker.getEntreprise().getId());
        }

        //PDP
        if (worker.getPdps() != null) {
            List<Long> pdpIds = worker.getPdps().stream()
                    .map(pdp -> pdp.getId())
                    .toList();
            workerDTO.setPdps(pdpIds);
        }

        //Chantiers
        if (worker.getChantiers() != null) {
            List<Long> chantierIds = worker.getChantiers().stream()
                    .map(chantier -> chantier.getId())
                    .toList();
            workerDTO.setChantiers(chantierIds);
        }
    }

    @Override
    public void setEntityFields(WorkerDTO workerDTO, Worker worker) {
        if (workerDTO == null || worker == null) {
            return;
        }
        worker.setId(workerDTO.getId());
        worker.setNom(workerDTO.getNom());
        worker.setPrenom(workerDTO.getPrenom());
        //Entreprise
        if (workerDTO.getEntreprise() != null) {
          worker.setEntreprise(entrepriseService.getById(workerDTO.getEntreprise()));

        }

        //PDP
        if (workerDTO.getPdps() != null) {
           // worker.setPdps(pdpServiceImpl.getByIds(wokerDTO.getPdps()));
            List<Pdp> pdps = pdpService.getByIds(workerDTO.getPdps());
            worker.getPdps().clear();
            worker.getPdps().addAll(pdps);
        }

        //Chantiers
        if (workerDTO.getChantiers() != null) {
           // worker.setChantiers(chantierServiceImpl.getByIds(wokerDTO.getChantiers()));
            List<Chantier> chantiers = chantierService.getByIds(workerDTO.getChantiers());
            worker.getChantiers().clear();
            worker.getChantiers().addAll(chantiers);
        }
    }

    @Override
    public Worker toEntity(WorkerDTO workerDTO) {

        if (workerDTO == null) {
            return null;
        }
        Worker worker = new Worker();
        setEntityFields(workerDTO, worker);
        return worker;
    }

    @Override
    public WorkerDTO toDTO(Worker worker) {
        if (worker == null) {
            return null;
        }
        WorkerDTO workerDTO = new WorkerDTO();
        setDTOFields(workerDTO, worker);
        return workerDTO;
    }

    @Override
    public List<Worker> toEntityList(List<WorkerDTO> workerDTOS) {
        return workerDTOS.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkerDTO> toDTOList(List<Worker> workers) {
        if (workers == null) {
            // Return an empty list if the input is null
            return new ArrayList<>();
        }

        return workers.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());    }

    @Override
    public Worker updateEntityFromDTO(Worker worker, WorkerDTO workerDTO) {
        if (workerDTO == null) {
            return worker;
        }
        setEntityFields(workerDTO, worker);

        return worker;
    }

    @Override
    public WorkerDTO updateDTOFromEntity(WorkerDTO workerDTO, Worker worker) {
        if (worker == null) return workerDTO;
        setDTOFields(workerDTO, worker);
        return workerDTO;
    }
}
