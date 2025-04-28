package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.Repo.PdpRepo;
import com.danone.pdpbackend.entities.ObjectAnswered;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.dto.ObjectAnsweredDTO;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class ObjectAnsweredMapper implements Mapper<ObjectAnsweredDTO, ObjectAnswered>{
    private final PdpRepo pdpRepo;

    public ObjectAnsweredMapper(PdpRepo pdpRepo) {
        this.pdpRepo = pdpRepo;
    }

    @Override
    public void setDTOFields(ObjectAnsweredDTO objectAnsweredDTO, ObjectAnswered objectAnswered) {
        if(objectAnsweredDTO.getId() == null) {
            objectAnsweredDTO.setId(objectAnswered.getId());
        }
        //objectAnsweredDTO.setId(objectAnswered.getId());
        objectAnsweredDTO.setPdp(objectAnswered.getPdp().getId());
        objectAnsweredDTO.setObjectType(objectAnswered.getObjectType());
        objectAnsweredDTO.setObjectId(objectAnswered.getObjectId());
        objectAnsweredDTO.setAnswer(objectAnswered.getAnswer());
        objectAnsweredDTO.setEE(objectAnswered.getEe());
        objectAnsweredDTO.setEU(objectAnswered.getEu());
    }

    @Override
    public void setEntityFields(ObjectAnsweredDTO objectAnsweredDTO, ObjectAnswered objectAnswered) {
        objectAnswered.setId(objectAnsweredDTO.getId());

        if (objectAnsweredDTO.getPdp() != null && objectAnswered.getPdp() == null) {
            Pdp pdp = pdpRepo.findById(objectAnsweredDTO.getPdp())
                    .orElseThrow(() -> new IllegalArgumentException("PDP not found"));
            objectAnswered.setPdp(pdp);
        }

        objectAnswered.setObjectType(objectAnsweredDTO.getObjectType());
        objectAnswered.setObjectId(objectAnsweredDTO.getObjectId());
        objectAnswered.setAnswer(objectAnsweredDTO.getAnswer());
        objectAnswered.setEe(objectAnsweredDTO.getEE());
        objectAnswered.setEu(objectAnsweredDTO.getEU());
    }

    @Override
    public ObjectAnswered toEntity(ObjectAnsweredDTO objectAnsweredDTO) {
        ObjectAnswered objectAnswered = new ObjectAnswered();
        setEntityFields(objectAnsweredDTO, objectAnswered);
        return objectAnswered;
    }

    public ObjectAnswered toEntity(ObjectAnsweredDTO dto, Pdp pdp) {
        ObjectAnswered objectAnswered = new ObjectAnswered();
        setEntityFields(dto, objectAnswered);
        objectAnswered.setPdp(pdp);
        return objectAnswered;
    }

    @Override
    public ObjectAnsweredDTO toDTO(ObjectAnswered objectAnswered) {
        ObjectAnsweredDTO objectAnsweredDTO = new ObjectAnsweredDTO();
        setDTOFields(objectAnsweredDTO, objectAnswered);
        return objectAnsweredDTO;
    }

    @Override
    public List<ObjectAnswered> toEntityList(List<ObjectAnsweredDTO> objectAnsweredDTOS) {
        return objectAnsweredDTOS.stream()
                .map(this::toEntity)
                .toList();
    }


    public List<ObjectAnswered> toEntityList(List<ObjectAnsweredDTO> objectAnsweredDTOS, Pdp pdp) {
        return objectAnsweredDTOS.stream()
                .map(dto -> toEntity(dto, pdp))
                .toList();
    }

    @Override
    public List<ObjectAnsweredDTO> toDTOList(List<ObjectAnswered> objectAnswereds) {
        return objectAnswereds.stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public ObjectAnswered updateEntityFromDTO(ObjectAnswered objectAnswered, ObjectAnsweredDTO objectAnsweredDTO) {
        if (objectAnsweredDTO == null) {
            return objectAnswered;
        }
        setEntityFields(objectAnsweredDTO, objectAnswered);
        return objectAnswered;
    }

    @Override
    public ObjectAnsweredDTO updateDTOFromEntity(ObjectAnsweredDTO objectAnsweredDTO, ObjectAnswered objectAnswered) {
        if (objectAnswered == null) {
            return objectAnsweredDTO;
        }
        setDTOFields(objectAnsweredDTO, objectAnswered);
        return objectAnsweredDTO;
    }
}
