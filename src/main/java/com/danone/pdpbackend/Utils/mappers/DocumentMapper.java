package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.entities.Document;
import com.danone.pdpbackend.entities.dto.DocumentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {ObjectAnsweredMapper.class, DocumentSignatureMapper.class})
public interface DocumentMapper {

    DocumentMapper INSTANCE = Mappers.getMapper(DocumentMapper.class);

    @Mapping(source = "chantier.id", target = "chantier")
    @Mapping(source = "entrepriseExterieure.id", target = "entrepriseExterieure")
    @Mapping(source = "donneurDOrdre.id", target = "donneurDOrdre")
    DocumentDTO toDTO(Document document);
}
