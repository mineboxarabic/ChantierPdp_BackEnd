package com.danone.pdpbackend.Utils.mappers;


import com.danone.pdpbackend.Services.BdtService;
import com.danone.pdpbackend.Services.PdpService;
import com.danone.pdpbackend.Services.WorkerService;
import com.danone.pdpbackend.Utils.BidirectionalRelationshipUtil;
import com.danone.pdpbackend.entities.Bdt;
import com.danone.pdpbackend.entities.Entreprise;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.EntrepriseDTO;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EntrepriseMapper implements Mapper<EntrepriseDTO, Entreprise> {
    private final PdpService pdpService;
    private final BdtService bdtService;
    private final WorkerService workerService;
    public EntrepriseMapper(@Lazy PdpService pdpService, BdtService bdtService, WorkerService workerService) {
        this.pdpService = pdpService;
        this.bdtService = bdtService;
        this.workerService = workerService;
    }

    @Override
    public void setDTOFields(EntrepriseDTO entrepriseDTO, Entreprise entreprise) {
        if (entrepriseDTO == null || entreprise == null) {
            return;
        }
        entrepriseDTO.setId(entreprise.getId());
        entrepriseDTO.setNom(entreprise.getNom());
        entrepriseDTO.setDescription(entreprise.getDescription());
        entrepriseDTO.setNumTel(entreprise.getNumTel());
        entrepriseDTO.setRaisonSociale(entreprise.getRaisonSociale());
        entrepriseDTO.setAddress(entreprise.getAddress());
        entrepriseDTO.setImage(entreprise.getImage());
        entrepriseDTO.setMedecinDuTravailleEE(entreprise.getMedecinDuTravailleEE());
        entrepriseDTO.setType(entreprise.getType());

        //PDP
        if (entreprise.getPdps() != null) {
            entrepriseDTO.setPdps(entreprise.getPdps().stream().map(Pdp::getId).toList());
        } else {
            entrepriseDTO.setPdps(List.of());
        }

        //BDT
        if (entreprise.getBdts() != null) {
            entrepriseDTO.setBdts(entreprise.getBdts().stream().map(Bdt::getId).toList());
        } else {
            entrepriseDTO.setBdts(List.of());
        }

        //Worker
        if (entreprise.getWorkers() != null) {
            entrepriseDTO.setWorkers(entreprise.getWorkers().stream().map(Worker::getId).toList());
        } else {
            entrepriseDTO.setWorkers(List.of());
        }
    }


    @Override
    public void setEntityFields(EntrepriseDTO entrepriseDTO, Entreprise entreprise) {
        if (entrepriseDTO == null || entreprise == null) {
            return;
        }
        entreprise.setId(entrepriseDTO.getId());
        entreprise.setNom(entrepriseDTO.getNom());
        entreprise.setDescription(entrepriseDTO.getDescription());
        entreprise.setNumTel(entrepriseDTO.getNumTel());
        entreprise.setRaisonSociale(entrepriseDTO.getRaisonSociale());
        entreprise.setAddress(entrepriseDTO.getAddress());
        entreprise.setImage(entrepriseDTO.getImage());
        entreprise.setMedecinDuTravailleEE(entrepriseDTO.getMedecinDuTravailleEE());
        entreprise.setType(entrepriseDTO.getType());

        // Update Workers bidirectional relationship
        if (entrepriseDTO.getWorkers() != null) {
            BidirectionalRelationshipUtil.updateBidirectionalRelationship(
                    entreprise,                                      // Parent entity
                    entreprise.getWorkers(),                         // Collection in parent
                    entrepriseDTO.getWorkers(),                      // New child IDs
                    workerService::getWorkersByIds,                  // Service to get children by IDs
                    Worker::getId,                                   // Function to get ID from child
                    Worker::setEntreprise                            // Method to set parent in child
            );
        }

        // Similarly for PDPs if needed
        if (entrepriseDTO.getPdps() != null) {
            BidirectionalRelationshipUtil.updateBidirectionalRelationship(
                    entreprise,
                    entreprise.getPdps(),

                    entrepriseDTO.getPdps(),
                    pdpService::getByIds,

                    Pdp::getId,
                    Pdp::setEntrepriseExterieure  // Assuming this setter exists
            );
        }

        // And for BDTs if needed
        if (entrepriseDTO.getBdts() != null) {
            BidirectionalRelationshipUtil.updateBidirectionalRelationship(
                    entreprise,
                    entreprise.getBdts(),
                    entrepriseDTO.getBdts(),
                    bdtService::getByIds,
                    Bdt::getId,
                    Bdt::setEntrepriseExterieure  // Assuming this setter exists
            );
        }
    }


    @Override
    public Entreprise toEntity(EntrepriseDTO entrepriseDTO) {
        if (entrepriseDTO == null) {
            return null;
        }

        Entreprise entreprise = new Entreprise();

        setEntityFields(entrepriseDTO, entreprise);

        //PDP
        if (entrepriseDTO.getPdps() != null) {
            List<Pdp> pdps = pdpService.getByIds(entrepriseDTO.getPdps());
            entreprise.getPdps().clear();
            entreprise.getPdps().addAll(pdps);
        }

        //BDT
        if (entrepriseDTO.getBdts() != null) {
         //   entreprise.setBdts(bdtService.getBDTsByIds(entrepriseDTO.getBdts()));
            List<Bdt> bdts = bdtService.getByIds(entrepriseDTO.getBdts());
            entreprise.getBdts().clear();
            entreprise.getBdts().addAll(bdts);
        }

        // Handle collection with null check
        if (entrepriseDTO.getWorkers() != null) {
        //    entreprise.setWorkers(workerService.getWorkersByIds(entrepriseDTO.getWorkers()));
            List<Worker> workers = workerService.getWorkersByIds(entrepriseDTO.getWorkers());
            entreprise.getWorkers().clear();
            entreprise.getWorkers().addAll(workers);
        }
        return entreprise;
    }

    @Override
    public EntrepriseDTO toDTO(Entreprise entreprise) {
        if (entreprise == null) {
            return null;
        }

        EntrepriseDTO entrepriseDTO = new EntrepriseDTO();

        setDTOFields(entrepriseDTO, entreprise);

        //PDP
        if (entreprise.getPdps() != null) {
            entrepriseDTO.setPdps(entreprise.getPdps().stream().map(Pdp::getId).toList());
        } else {
            entrepriseDTO.setPdps(List.of());
        }

        //BDT
        if (entreprise.getBdts() != null) {
            entrepriseDTO.setBdts(entreprise.getBdts().stream().map(Bdt::getId).toList());
        } else {
            entrepriseDTO.setBdts(List.of());
        }

        //Worker
        if (entreprise.getWorkers() != null) {
            entrepriseDTO.setWorkers(entreprise.getWorkers().stream().map(Worker::getId).toList());
        } else {
            entrepriseDTO.setWorkers(List.of());
        }

        return entrepriseDTO;
    }

    @Override
    public List<Entreprise> toEntityList(List<EntrepriseDTO> entrepriseDTOS) {
        return entrepriseDTOS.stream()
                .map(this::toEntity)
                .toList();

    }

    @Override
    public List<EntrepriseDTO> toDTOList(List<Entreprise> entreprises) {
        return entreprises.stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public Entreprise updateEntityFromDTO(Entreprise entreprise, EntrepriseDTO entrepriseDTO) {
        if (entrepriseDTO == null) {
            return entreprise;
        }

        setEntityFields(entrepriseDTO, entreprise);

        return entreprise;
    }

    @Override
    public EntrepriseDTO updateDTOFromEntity(EntrepriseDTO entrepriseDTO, Entreprise entreprise) {
        if (entreprise == null) {
            return entrepriseDTO;
        }

        setDTOFields(entrepriseDTO, entreprise);



        return entrepriseDTO;
    }

}
