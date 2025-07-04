package com.danone.pdpbackend.Utils.mappers;


import com.danone.pdpbackend.Services.*;
import com.danone.pdpbackend.entities.*;
import com.danone.pdpbackend.entities.dto.ChantierDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class ChantierMapper {
    @Autowired
    protected EntrepriseService entrepriseService;
    @Autowired protected LocalisationService localisationService;
    @Autowired protected UserService userService;
    @Autowired protected BdtService bdtService;
    @Autowired protected PdpService pdpService;
    @Autowired protected WorkerService workerService;
    @Autowired protected WorkerSelectionService workerSelectionService;

    @Mapping(target = "entrepriseUtilisatrice", expression = "java(mapEntreprise(dto.getEntrepriseUtilisatrice()))")
    @Mapping(target = "entrepriseExterieurs", expression = "java(mapEntreprises(dto.getEntrepriseExterieurs()))")
    @Mapping(target = "localisation", expression = "java(mapLocalisation(dto.getLocalisation()))")
    @Mapping(target = "donneurDOrdre", expression = "java(mapUser(dto.getDonneurDOrdre()))")
    @Mapping(target = "bdts", expression = "java(mapBdts(dto.getBdts()))")
    @Mapping(target = "pdps", expression = "java(mapPdps(dto.getPdps()))")
    @Mapping(target = "workerSelections", expression = "java(mapWorkerSelections(dto.getWorkerSelections()))")
    public abstract Chantier toEntity(ChantierDTO dto);

    @Mapping(target = "entrepriseUtilisatrice", source = "entrepriseUtilisatrice.id")
    @Mapping(target = "entrepriseExterieurs", expression = "java(toIdList(chantier.getEntrepriseExterieurs()))")
    @Mapping(target = "localisation", source = "localisation.id")
    @Mapping(target = "donneurDOrdre", source = "donneurDOrdre.id")
    @Mapping(target = "bdts", expression = "java(toBdtIds(chantier.getBdts()))")
    @Mapping(target = "pdps", expression = "java(toPdpIds(chantier.getPdps()))")
    @Mapping(target = "workerSelections", expression = "java(toWorkerSelectionIds(chantier.getWorkerSelections()))")
    public abstract ChantierDTO toDTO(Chantier chantier);



    @Mapping(target = "id" , ignore = true)
    public abstract void updateEntity(ChantierDTO dto, @MappingTarget Chantier chantier);

    @Mapping(target = "id", ignore = true)
    public abstract void updateDTO(Chantier chantier, @MappingTarget ChantierDTO dto);

    public abstract List<ChantierDTO> toDTOList(List<Chantier> chantiers);
    public abstract List<Chantier> toEntityList(List<ChantierDTO> dtos);

    protected Entreprise mapEntreprise(Long id) {
        return id != null ? entrepriseService.getById(id) : null;
    }

    protected List<Entreprise> mapEntreprises(List<Long> ids) {
        return ids != null ? entrepriseService.getByIds(ids) : new ArrayList<>();
    }

    protected Localisation mapLocalisation(Long id) {
        return id != null ? localisationService.getById(id) : null;
    }

    protected User mapUser(Long id) {
        return id != null ? userService.getUserById(id) : null;
    }

    protected List<Bdt> mapBdts(List<Long> ids) {
        return ids != null ? bdtService.getByIds(ids) : List.of();
    }

    protected List<Pdp> mapPdps(List<Long> ids) {
        return ids != null ? pdpService.getByIds(ids) : List.of();
    }

    protected List<WorkerChantierSelection> mapWorkerSelections(List<Long> ids) {
        return ids != null ? workerSelectionService.getWorkerSelectionsByIds(ids) : List.of();
    }

    protected List<Worker> mapWorkers(List<Long> ids) {
        return ids != null ? workerService.getWorkersByIds(ids) : List.of();
    }

    protected List<Long> toIdList(List<Entreprise> list) {
        return list != null ? list.stream().map(Entreprise::getId).collect(Collectors.toList()) : List.of();
    }

    protected List<Long> toBdtIds(List<Bdt> list) {
        return list != null ? list.stream().map(Bdt::getId).collect(Collectors.toList()) : List.of();
    }

    protected List<Long> toPdpIds(List<Pdp> list) {
        return list != null ? list.stream().map(Pdp::getId).collect(Collectors.toList()) : List.of();
    }

    protected List<Long> toWorkerSelectionIds(List<WorkerChantierSelection> list) {
        return list != null ? list.stream().map(WorkerChantierSelection::getId).collect(Collectors.toList()) : List.of();
    }

    protected List<Long> toWorkerIds(List<Worker> list) {
        return list != null ? list.stream().map(Worker::getId).collect(Collectors.toList()) : List.of();
    }

    protected Long map(Entreprise entreprise) {
        return entreprise != null ? entreprise.getId() : null;
    }

    protected Long map(User user) { return user != null ? user.getId() : null; }
    protected Long map(Localisation loc) { return loc != null ? loc.getId() : null; }

}

