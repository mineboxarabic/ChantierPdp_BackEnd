package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.Services.ChantierService;
import com.danone.pdpbackend.Services.EntrepriseService;
import com.danone.pdpbackend.Services.Implimetations.ChantierServiceImpl;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.dto.PdpDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor // Use Lombok for constructor injection
public class PdpMapper implements Mapper<PdpDTO, Pdp> {

    private final EntrepriseService entrepriseService;
    private final ObjectAnsweredMapper objectAnsweredMapper;
    private final DocumentSignatureMapper documentSignatureMapper;
    private final ChantierService chantierService;

    // --- Entity to DTO ---

    @Override
    public PdpDTO toDTO(Pdp pdp) {
        if (pdp == null) return null;
        PdpDTO pdpDTO = new PdpDTO();
        setDTOFields(pdpDTO, pdp);
        return pdpDTO;
    }

    @Override
    public void setDTOFields(PdpDTO pdpDTO, Pdp pdp) {
        if (pdpDTO == null || pdp == null) return;

        // Map common Document fields using the utility
        DocumentMappingUtils.mapEntityToDtoBase(pdp, pdpDTO, objectAnsweredMapper, documentSignatureMapper);

        // Map PDP Specific Fields
        if(pdp.getChantier() != null) {
            pdpDTO.setChantier(pdp.getChantier().getId());
        }
        //pdpDTO.setChantier(pdp.getChantier().getId());
        pdpDTO.setDateInspection(pdp.getDateInspection());
        pdpDTO.setIcpdate(pdp.getIcpdate());
        pdpDTO.setDatePrevenirCSSCT(pdp.getDatePrevenirCSSCT());
        pdpDTO.setDatePrev(pdp.getDatePrev());
        pdpDTO.setHorairesDetails(pdp.getHorairesDetails());
        pdpDTO.setHoraireDeTravail(pdp.getHoraireDeTravail());
        pdpDTO.setMisesEnDisposition(pdp.getMisesEnDisposition());
        if (pdp.getEntrepriseDInspection() != null) {
            pdpDTO.setEntrepriseDInspection(pdp.getEntrepriseDInspection().getId());
        } else {
            pdpDTO.setEntrepriseDInspection(null);
        }
    }

    // --- DTO to Entity ---

    @Override
    public Pdp toEntity(PdpDTO pdpDTO) {
        if (pdpDTO == null) return null;
        Pdp pdp = new Pdp();
        setEntityFields(pdpDTO, pdp);
        return pdp;
    }

    @Override
    public void setEntityFields(PdpDTO pdpDTO, Pdp pdp) {
        if (pdpDTO == null || pdp == null) return;

        // Map common Document fields using the utility
        DocumentMappingUtils.mapDtoToEntityBase(pdpDTO, pdp, entrepriseService, objectAnsweredMapper, documentSignatureMapper);

        // Map PDP Specific Fields
       // pdp.setChantier(pdpDTO.getChantier());

        if(pdpDTO.getChantier() != null) {
            pdp.setChantier(chantierService.getById(pdpDTO.getChantier()));
        } else {
            pdp.setChantier(null);
        }
        pdp.setDateInspection(pdpDTO.getDateInspection());
        pdp.setIcpdate(pdpDTO.getIcpdate());
        pdp.setDatePrevenirCSSCT(pdpDTO.getDatePrevenirCSSCT());
        pdp.setDatePrev(pdpDTO.getDatePrev());
        pdp.setHorairesDetails(pdpDTO.getHorairesDetails());
        pdp.setHoraireDeTravail(pdpDTO.getHoraireDeTravail()); // Embeddable copy
        pdp.setMisesEnDisposition(pdpDTO.getMisesEnDisposition()); // Embeddable copy

        if (pdpDTO.getEntrepriseDInspection() != null) {
            if (pdp.getEntrepriseDInspection() == null || !pdp.getEntrepriseDInspection().getId().equals(pdpDTO.getEntrepriseDInspection())) {
                pdp.setEntrepriseDInspection(entrepriseService.getById(pdpDTO.getEntrepriseDInspection()));
            }
        } else {
            pdp.setEntrepriseDInspection(null);
        }
    }

    // --- List and Update Methods ---

    @Override
    public List<Pdp> toEntityList(List<PdpDTO> pdpDTOS) {
        if (pdpDTOS == null) return Collections.emptyList();
        return pdpDTOS.stream().filter(Objects::nonNull).map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<PdpDTO> toDTOList(List<Pdp> pdps) {
        if (pdps == null) return Collections.emptyList();
        return pdps.stream().filter(Objects::nonNull).map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Pdp updateEntityFromDTO(Pdp pdp, PdpDTO pdpDTO) {
        setEntityFields(pdpDTO, pdp);
        return pdp;
    }

    @Override
    public PdpDTO updateDTOFromEntity(PdpDTO pdpDTO, Pdp pdp) {
        setDTOFields(pdpDTO, pdp);
        return pdpDTO;
    }
}