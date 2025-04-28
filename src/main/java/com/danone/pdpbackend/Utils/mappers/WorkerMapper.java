package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.Services.EntrepriseService;
import com.danone.pdpbackend.Services.Implimetations.ChantierServiceImpl;
import com.danone.pdpbackend.Services.Implimetations.PdpServiceImpl;
import com.danone.pdpbackend.entities.Chantier;
import com.danone.pdpbackend.entities.Entreprise;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.WokerDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
@AllArgsConstructor
public class WorkerMapper implements Mapper<WokerDTO, Worker> {

    private final EntrepriseService entrepriseService;
    private final PdpServiceImpl pdpServiceImpl;
    private final ChantierServiceImpl chantierServiceImpl;


    /* private Long id;
    private String nom;
    private String prenom;
    private Entreprise entreprise;
    private List<Long> pdp;
    private List<Long> signatures;
    private List<Long> chantiers;
    private List<Long> chantierSelections;*/


    @Override
    public void setDTOFields(WokerDTO wokerDTO, Worker worker) {
        if (wokerDTO == null || worker == null) {
            return;
        }
        wokerDTO.setId(worker.getId());
        wokerDTO.setNom(worker.getNom());
        wokerDTO.setPrenom(worker.getPrenom());
        //Entreprise
        if (worker.getEntreprise() != null) {
            wokerDTO.setEntreprise(worker.getEntreprise().getId());
        }

        //PDP
        if (worker.getPdps() != null) {
            List<Long> pdpIds = worker.getPdps().stream()
                    .map(pdp -> pdp.getId())
                    .toList();
            wokerDTO.setPdps(pdpIds);
        }

        //Chantiers
        if (worker.getChantiers() != null) {
            List<Long> chantierIds = worker.getChantiers().stream()
                    .map(chantier -> chantier.getId())
                    .toList();
            wokerDTO.setChantiers(chantierIds);
        }
    }

    @Override
    public void setEntityFields(WokerDTO wokerDTO, Worker worker) {
        if (wokerDTO == null || worker == null) {
            return;
        }
        worker.setId(wokerDTO.getId());
        worker.setNom(wokerDTO.getNom());
        worker.setPrenom(wokerDTO.getPrenom());
        //Entreprise
        if (wokerDTO.getEntreprise() != null) {
          worker.setEntreprise(entrepriseService.getById(wokerDTO.getEntreprise()));

        }

        //PDP
        if (wokerDTO.getPdps() != null) {
           // worker.setPdps(pdpServiceImpl.getByIds(wokerDTO.getPdps()));
            List<Pdp> pdps = pdpServiceImpl.getByIds(wokerDTO.getPdps());
            worker.getPdps().clear();
            worker.getPdps().addAll(pdps);
        }

        //Chantiers
        if (wokerDTO.getChantiers() != null) {
           // worker.setChantiers(chantierServiceImpl.getByIds(wokerDTO.getChantiers()));
            List<Chantier> chantiers = chantierServiceImpl.getByIds(wokerDTO.getChantiers());
            worker.getChantiers().clear();
            worker.getChantiers().addAll(chantiers);
        }
    }

    @Override
    public Worker toEntity(WokerDTO wokerDTO) {

        if (wokerDTO == null) {
            return null;
        }
        Worker worker = new Worker();
        setEntityFields(wokerDTO, worker);
        return worker;
    }

    @Override
    public WokerDTO toDTO(Worker worker) {
        if (worker == null) {
            return null;
        }
        WokerDTO wokerDTO = new WokerDTO();
        setDTOFields(wokerDTO, worker);
        return wokerDTO;
    }

    @Override
    public List<Worker> toEntityList(List<WokerDTO> wokerDTOS) {
        return List.of();
    }

    @Override
    public List<WokerDTO> toDTOList(List<Worker> workers) {
        return List.of();
    }

    @Override
    public Worker updateEntityFromDTO(Worker worker, WokerDTO wokerDTO) {
        if (wokerDTO == null) {
            return worker;
        }
        setEntityFields(wokerDTO, worker);

        return worker;
    }

    @Override
    public WokerDTO updateDTOFromEntity(WokerDTO wokerDTO, Worker worker) {
        if (worker == null) return wokerDTO;
        setDTOFields(wokerDTO, worker);
        return wokerDTO;
    }
}
