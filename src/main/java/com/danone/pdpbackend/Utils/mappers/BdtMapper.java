package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.Repo.SignatureRepo;
import com.danone.pdpbackend.Services.ChantierService;
import com.danone.pdpbackend.Services.EntrepriseService;
import com.danone.pdpbackend.Services.Implimetations.ChantierServiceImpl;
import com.danone.pdpbackend.Services.WorkerService;
import com.danone.pdpbackend.entities.BDT.Bdt;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.BdtDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class BdtMapper implements Mapper<BdtDTO, Bdt> {

    private final ObjectAnsweredMapper objectAnsweredMapper;
    private final ChantierService chantierService;
    private final EntrepriseService entrepriseService;
    private final WorkerService workerService;
    private final SignatureRepo signatureRepo;

    @Override
    public void setDTOFields(BdtDTO bdtDTO, Bdt bdt) {
        bdtDTO.setId(bdt.getId());
        bdtDTO.setNom(bdt.getNom());
        bdtDTO.setComplementOuRappels(bdt.getComplementOuRappels());

        //bdtDTO.setChantier(bdt.getChantier().getId());
        //bdtDTO.setEntrepriseExterieure(bdt.getEntrepriseExterieure().getId());
        //bdtDTO.setSignatureChargeDeTravail(bdt.getSignatureChargeDeTravail().getId());
        //bdtDTO.setSignatureDonneurDOrdre(bdt.getSignatureDonneurDOrdre().getId());

        if(bdt.getChantier() != null) {
            bdtDTO.setChantier(bdt.getChantier().getId());
        }
        if(bdt.getEntrepriseExterieure() != null) {
            bdtDTO.setEntrepriseExterieure(bdt.getEntrepriseExterieure().getId());
        }
        if(bdt.getSignatureChargeDeTravail() != null) {
            bdtDTO.setSignatureChargeDeTravail(bdt.getSignatureChargeDeTravail().getId());
        }
        if(bdt.getSignatureDonneurDOrdre() != null) {
            bdtDTO.setSignatureDonneurDOrdre(bdt.getSignatureDonneurDOrdre().getId());
        }



        bdtDTO.setStatus(bdt.getStatus());
        bdtDTO.setDate(bdt.getDate());


        //bdtDTO.setPermitRelations(bdt.getPermitRelations().stream().map(objectAnsweredMapper::toDTO).toList());

        if(bdt.getPermitRelations() != null) {
            bdtDTO.setPermitRelations(bdt.getPermitRelations().stream().map(objectAnsweredMapper::toDTO).toList());
        }

        if(bdt.getSignatures() != null) {
            bdtDTO.setSignatures(bdt.getSignatures().stream().map(Worker::getId).toList());
        }
        //bdtDTO.setSignatures(bdt.getSignatures().stream().map(Worker::getId).toList());

        if(bdt.getRelations() != null) {
            bdtDTO.setRelations(bdt.getRelations().stream().map(objectAnsweredMapper::toDTO).toList());
        }


    }

    @Override
    public void setEntityFields(BdtDTO bdtDTO, Bdt bdt) {
        if(bdtDTO.getId() == null) {
            bdt.setId(bdt.getId());
        } else {
            bdt.setId(bdtDTO.getId());
        }

        bdt.setNom(bdtDTO.getNom());
        bdt.setComplementOuRappels(bdtDTO.getComplementOuRappels());
        if(bdtDTO.getChantier() != null) {
            bdt.setChantier(chantierService.getById(bdtDTO.getChantier()));
        }

        if(bdtDTO.getEntrepriseExterieure() != null) {
            bdt.setEntrepriseExterieure(entrepriseService.getById(bdtDTO.getEntrepriseExterieure()));
        }
        //bdt.setEntrepriseExterieure(entrepriseService.getById(bdtDTO.getEntrepriseExterieure())); // TODO: Set entrepriseExterieure entity
        if(bdtDTO.getSignatureChargeDeTravail() != null) {
            bdt.setSignatureChargeDeTravail(signatureRepo.findById(bdtDTO.getSignatureChargeDeTravail()));
        }

        //bdt.setSignatureChargeDeTravail(signatureRepo.findById(bdtDTO.getSignatureChargeDeTravail())); // TODO: Set signatureChargeDeTravail entity
        if(bdtDTO.getSignatureDonneurDOrdre() != null) {
            bdt.setSignatureDonneurDOrdre(signatureRepo.findById(bdtDTO.getSignatureDonneurDOrdre()));
        }
        //bdt.setSignatureDonneurDOrdre(signatureRepo.findById(bdtDTO.getSignatureDonneurDOrdre())); // TODO: Set signatureDonneurDOrdre entity
        bdt.setStatus(bdtDTO.getStatus());
        bdt.setDate(bdtDTO.getDate());
        if(bdtDTO.getPermitRelations() != null) {
            bdt.setPermitRelations(bdtDTO.getPermitRelations().stream().map(objectAnsweredMapper::toEntity).toList());
        }
       // bdt.setPermitRelations(bdtDTO.getPermitRelations().stream().map(objectAnsweredMapper::toEntity).toList());
        if(bdtDTO.getSignatures() != null) {
            bdt.setSignatures(workerService.getWorkersByIds(bdtDTO.getSignatures()));
        }
        //bdt.setSignatures(workerService.getWorkersByIds(bdtDTO.getSignatures())); // TODO: Set signatures entity
        if(bdtDTO.getRelations() != null) {
            bdt.setRelations(bdtDTO.getRelations().stream().map(objectAnsweredMapper::toEntity).toList());
        }
        //bdt.setRelations(bdtDTO.getPermitRelations().stream().map(objectAnsweredMapper::toEntity).toList());
    }

    @Override
    public Bdt toEntity(BdtDTO bdtDTO) {
        if(bdtDTO == null) {
            return null;
        }

        Bdt bdt = new Bdt();
        setEntityFields(bdtDTO, bdt);
        return bdt;
    }

    @Override
    public BdtDTO toDTO(Bdt bdt) {
        if (bdt == null) {
            return null;
        }
       BdtDTO bdtDTO = new BdtDTO();
        setDTOFields(bdtDTO, bdt);
        return bdtDTO;
    }

    @Override
    public List<Bdt> toEntityList(List<BdtDTO> bdtDTOS) {
        return bdtDTOS.stream().map(this::toEntity).toList();
    }

    @Override
    public List<BdtDTO> toDTOList(List<Bdt> bdts) {
        return bdts.stream().map(this::toDTO).toList();
    }

    @Override
    public Bdt updateEntityFromDTO(Bdt bdt, BdtDTO bdtDTO) {
        if (bdt == null || bdtDTO == null) {
            return null;
        }
        setEntityFields(bdtDTO, bdt);


        return bdt;
    }

    @Override
    public BdtDTO updateDTOFromEntity(BdtDTO bdtDTO, Bdt bdt) {
        if (bdt == null || bdtDTO == null) {
            return null;
        }
        setDTOFields(bdtDTO, bdt);
        return bdtDTO;
    }
}
