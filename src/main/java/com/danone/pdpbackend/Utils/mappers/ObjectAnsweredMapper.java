package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.Repo.BDTRepo;
import com.danone.pdpbackend.Repo.DocumentRepo;
import com.danone.pdpbackend.Repo.PdpRepo;
import com.danone.pdpbackend.entities.Bdt;
import com.danone.pdpbackend.entities.Document;
import com.danone.pdpbackend.entities.ObjectAnswered;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.dto.ObjectAnsweredDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
@AllArgsConstructor
public class ObjectAnsweredMapper implements Mapper<ObjectAnsweredDTO, ObjectAnswered>{
    private final DocumentRepo documentRepo;


    @Override
    public void setDTOFields(ObjectAnsweredDTO objectAnsweredDTO, ObjectAnswered objectAnswered) {
        if(objectAnsweredDTO.getId() == null) {
            objectAnsweredDTO.setId(objectAnswered.getId());
        }
        //objectAnsweredDTO.setId(objectAnswered.getId());
        if(objectAnswered.getDocument() != null){
            objectAnsweredDTO.setDocument(objectAnswered.getDocument().getId());

        }

        objectAnsweredDTO.setObjectType(objectAnswered.getObjectType());
        objectAnsweredDTO.setObjectId(objectAnswered.getObjectId());
        objectAnsweredDTO.setAnswer(objectAnswered.getAnswer());
        objectAnsweredDTO.setEE(objectAnswered.getEe());
        objectAnsweredDTO.setEU(objectAnswered.getEu());
    }

    @Override
    public void setEntityFields(ObjectAnsweredDTO objectAnsweredDTO, ObjectAnswered objectAnswered) {
        objectAnswered.setId(objectAnsweredDTO.getId());

        if (objectAnsweredDTO.getDocument() != null && objectAnswered.getDocument() == null) {
            Document document = documentRepo.findById(objectAnsweredDTO.getDocument())
                    .orElseThrow(() -> new IllegalArgumentException("PDP not found"));
            objectAnswered.setDocument(document);
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

    public ObjectAnswered toEntity(ObjectAnsweredDTO dto, Document document) {
        ObjectAnswered objectAnswered = new ObjectAnswered();
        setEntityFields(dto, objectAnswered);
        objectAnswered.setDocument(document);
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


    public List<ObjectAnswered> toEntityList(List<ObjectAnsweredDTO> objectAnsweredDTOS, Document document) {
        return objectAnsweredDTOS.stream()
                .map(dto -> toEntity(dto, document))
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
