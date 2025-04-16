package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.Services.EntrepriseService;
import com.danone.pdpbackend.Services.WorkerService;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.PdpDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class PdpMapper {

    private final EntrepriseService entrepriseService;
    private final WorkerService workerService;

    public PdpMapper(EntrepriseService entrepriseService, WorkerService workerService) {
        this.entrepriseService = entrepriseService;
        this.workerService = workerService;
    }

    /**
     * Converts a Pdp entity to a PdpDTO.
     *
     * @param pdp The Pdp entity to convert
     * @return The resulting PdpDTO
     */
    public PdpDTO toDto(Pdp pdp) {
        if (pdp == null) {
            return null;
        }

        PdpDTO pdpDTO = new PdpDTO();

        // Map basic properties
        pdpDTO.setId(pdp.getId());
        pdpDTO.setChantier(pdp.getChantier());
        pdpDTO.setDateInspection(pdp.getDateInspection());
        pdpDTO.setIcpdate(pdp.getIcpdate());
        pdpDTO.setDatePrevenirCSSCT(pdp.getDatePrevenirCSSCT());
        pdpDTO.setDatePrev(pdp.getDatePrev());
        pdpDTO.setHorairesDetails(pdp.getHorairesDetails());

        // Map embedded objects
        pdpDTO.setHoraireDeTravail(pdp.getHoraireDeTravail());
        pdpDTO.setMisesEnDisposition(pdp.getMisesEnDisposition());

        // Map lists
        pdpDTO.setRisques(pdp.getRisques());
        pdpDTO.setDispositifs(pdp.getDispositifs());
        pdpDTO.setPermits(pdp.getPermits());
        pdpDTO.setAnalyseDeRisques(pdp.getAnalyseDeRisques());

        // Map complex object references to their IDs
        if (pdp.getEntrepriseExterieure() != null) {
            pdpDTO.setEntrepriseExterieure(pdp.getEntrepriseExterieure().getId());
        }

        if (pdp.getEntrepriseDInspection() != null) {
            pdpDTO.setEntrepriseDInspection(pdp.getEntrepriseDInspection().getId());
        }

        // Map Worker list to ID list
        if (pdp.getSignatures() != null) {
            List<Long> signatureIds = pdp.getSignatures().stream()
                    .map(Worker::getId)
                    .collect(Collectors.toList());
            pdpDTO.setSignatures(signatureIds);
        }

        return pdpDTO;
    }

    /**
     * Converts a PdpDTO to a Pdp entity.
     * Note: This doesn't set complex objects that need to be retrieved from repositories.
     *
     * @param pdpDTO The PdpDTO to convert
     * @return A partially populated Pdp entity
     */
    public Pdp toEntity(PdpDTO pdpDTO) {
        if (pdpDTO == null) {
            return null;
        }

        Pdp pdp = new Pdp();

        // Map basic properties
        pdp.setId(pdpDTO.getId());
        pdp.setChantier(pdpDTO.getChantier());
        pdp.setDateInspection(pdpDTO.getDateInspection());
        pdp.setIcpdate(pdpDTO.getIcpdate());
        pdp.setDatePrevenirCSSCT(pdpDTO.getDatePrevenirCSSCT());
        pdp.setDatePrev(pdpDTO.getDatePrev());
        pdp.setHorairesDetails(pdpDTO.getHorairesDetails());

        // Map embedded objects
        pdp.setHoraireDeTravail(pdpDTO.getHoraireDeTravail());
        pdp.setMisesEnDisposition(pdpDTO.getMisesEnDisposition());

        // Map lists
        pdp.setRisques(pdpDTO.getRisques());
        pdp.setDispositifs(pdpDTO.getDispositifs());
        pdp.setPermits(pdpDTO.getPermits());
        pdp.setAnalyseDeRisques(pdpDTO.getAnalyseDeRisques());


        if(pdpDTO.getEntrepriseExterieure() != null) {
            pdp.setEntrepriseExterieure(entrepriseService.getEntrepriseById(pdpDTO.getEntrepriseExterieure()));
        }

        if(pdpDTO.getEntrepriseDInspection() != null) {
            pdp.setEntrepriseDInspection(entrepriseService.getEntrepriseById(pdpDTO.getEntrepriseDInspection()));
        }

        if (pdpDTO.getSignatures() != null) {
            List<Worker> signatures =workerService.getWorkersByIds(pdpDTO.getSignatures());
            pdp.setSignatures(signatures);
        }




        return pdp;
    }
}