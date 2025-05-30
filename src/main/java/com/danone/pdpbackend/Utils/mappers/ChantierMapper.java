package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.Services.*;
import com.danone.pdpbackend.entities.*;
import com.danone.pdpbackend.entities.Bdt;
import com.danone.pdpbackend.entities.dto.ChantierDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ChantierMapper implements Mapper<ChantierDTO, Chantier> {
    //From DTO to Entity

    @Autowired
    private final ChantierService chantierService;
    @Autowired
    private EntrepriseService entrepriseService;
    @Autowired
    private LocalisationService localisationService;
    @Autowired
    private UserService userService;
    @Autowired
    private BdtService bdtService;
    @Autowired
    @Lazy
    private PdpService pdpService;
    @Autowired
    private WorkerSelectionService workerSelectionService;
    @Autowired
    private WorkerService workerService;

    public ChantierMapper(ChantierService chantierService) {
        this.chantierService = chantierService;
    }

    public static <T, ID> List<T> mergeEntityCollection(
            List<T> existingCollection,
            List<ID> requestedIds,
            Function<T, ID> idExtractor,
            Function<List<ID>, List<T>> entityFetcher) {

        if (requestedIds == null) {
            // If the requested IDs are null, don't modify the collection
            return existingCollection;
        }

        // Convert the requested IDs to a set for efficient lookups
        Set<ID> requestedIdSet = new HashSet<>(requestedIds);

        // Initialize the collection if it's null
        if (existingCollection == null) {
            existingCollection = new ArrayList<>();
        }

        // Remove items that are no longer in the requested list
        existingCollection.removeIf(entity ->
                entity != null && !requestedIdSet.contains(idExtractor.apply(entity)));

        // Get existing IDs
        Set<ID> existingIds = existingCollection.stream()
                .filter(Objects::nonNull)
                .map(idExtractor)
                .collect(Collectors.toSet());

        // Add only new items
        Set<ID> idsToAdd = requestedIds.stream()
                .filter(id -> !existingIds.contains(id))
                .collect(Collectors.toSet());

        if (!idsToAdd.isEmpty()) {
            List<T> newEntities = entityFetcher.apply(new ArrayList<>(idsToAdd));
            if (newEntities != null) {
                existingCollection.addAll(newEntities);
            }
        }

        return existingCollection;
    }

    @Override
    public void setDTOFields(ChantierDTO chantierDTO, Chantier chantier) {
        chantierDTO.setId(chantier.getId());
        chantierDTO.setNom(chantier.getNom());
        chantierDTO.setOperation(chantier.getOperation());
        chantierDTO.setDateDebut(chantier.getDateDebut());
        chantierDTO.setDateFin(chantier.getDateFin());
        chantierDTO.setNbHeurs(chantier.getNbHeurs());
        chantierDTO.setIsAnnuelle(chantier.getIsAnnuelle());
        chantierDTO.setEffectifMaxiSurChantier(chantier.getEffectifMaxiSurChantier());
        chantierDTO.setNombreInterimaires(chantier.getNombreInterimaires());
        chantierDTO.setStatus(chantier.getStatus());
        chantierDTO.setTravauxDangereux(chantier.getTravauxDangereux());
        chantierDTO.setStatus(chantier.getStatus());
        // Handle collection with null check and filtering
        if (chantier.getEntrepriseExterieurs() != null) {
            chantierDTO.setEntrepriseExterieurs(chantier.getEntrepriseExterieurs().stream()
                    .filter(Objects::nonNull)
                    .map(Entreprise::getId)
                    .collect(Collectors.toList()));
        } else {
            chantierDTO.setEntrepriseExterieurs(Collections.emptyList());
        }

        // Handle references with null checks
        if (chantier.getEntrepriseUtilisatrice() != null) {
            chantierDTO.setEntrepriseUtilisatrice(chantier.getEntrepriseUtilisatrice().getId());
        }

        if (chantier.getLocalisation() != null) {
            chantierDTO.setLocalisation(chantier.getLocalisation().getId());
        }

        if (chantier.getDonneurDOrdre() != null) {
            chantierDTO.setDonneurDOrdre(chantier.getDonneurDOrdre().getId());
        }

        // Handle collections with null checks and filtering
        if (chantier.getBdts() != null) {
            chantierDTO.setBdts(chantier.getBdts().stream()
                    .filter(Objects::nonNull)
                    .map(Bdt::getId)
                    .collect(Collectors.toList()));
        } else {
            chantierDTO.setBdts(Collections.emptyList());
        }

        if (chantier.getPdps() != null) {
            chantierDTO.setPdps(chantier.getPdps().stream()
                    .filter(Objects::nonNull)
                    .map(Pdp::getId)
                    .collect(Collectors.toList()));
        } else {
            chantierDTO.setPdps(Collections.emptyList());
        }

        if (chantier.getWorkerSelections() != null) {
            chantierDTO.setWorkerSelections(chantier.getWorkerSelections().stream()
                    .filter(Objects::nonNull)
                    .map(WorkerChantierSelection::getId)
                    .collect(Collectors.toList()));
        } else {
            chantierDTO.setWorkerSelections(Collections.emptyList());
        }

        if (chantier.getWorkers() != null) {
            chantierDTO.setWorkers(chantier.getWorkers().stream()
                    .filter(Objects::nonNull)
                    .map(Worker::getId)
                    .collect(Collectors.toList()));
        } else {
            chantierDTO.setWorkers(Collections.emptyList());
        }


    }

    @Override
    public void setEntityFields(ChantierDTO chantierDTO, Chantier chantier) {

        chantier.setId(chantierDTO.getId());
        chantier.setNom(chantierDTO.getNom());
        chantier.setOperation(chantierDTO.getOperation());
        chantier.setDateDebut(chantierDTO.getDateDebut());
        chantier.setDateFin(chantierDTO.getDateFin());
        chantier.setNbHeurs(chantierDTO.getNbHeurs());
        chantier.setIsAnnuelle(chantierDTO.getIsAnnuelle());
        chantier.setEffectifMaxiSurChantier(chantierDTO.getEffectifMaxiSurChantier());
        chantier.setNombreInterimaires(chantierDTO.getNombreInterimaires());
        chantier.setStatus(chantierDTO.getStatus());
        chantier.setTravauxDangereux(chantierDTO.getTravauxDangereux());
        chantier.setStatus(chantierDTO.getStatus());
        // Handle collection with null check
        if (chantierDTO.getEntrepriseExterieurs() != null) {
            chantier.setEntrepriseExterieurs(entrepriseService.getByIds(chantierDTO.getEntrepriseExterieurs()));
        } else {
            chantier.setEntrepriseExterieurs(Collections.emptyList());
        }

        // Handle references with null checks
        if (chantierDTO.getEntrepriseUtilisatrice() != null) {
            chantier.setEntrepriseUtilisatrice(entrepriseService.getById(chantierDTO.getEntrepriseUtilisatrice()));
        }

        if (chantierDTO.getLocalisation() != null) {
            chantier.setLocalisation(localisationService.getById(chantierDTO.getLocalisation()));
        }

        if (chantierDTO.getDonneurDOrdre() != null) {
            chantier.setDonneurDOrdre(userService.getUserById(chantierDTO.getDonneurDOrdre()));
        }

        // Handle collections with null checks
        //In case of create we get the them by id
        //In case of update we get them from chantier
        if (chantierDTO.getBdts() != null) {
            List<Bdt> bdts = bdtService.getByIds(chantierDTO.getBdts());
            chantier.setBdts(bdts);
        } else {
            chantier.setBdts(new ArrayList<>());
        }

        if (chantierDTO.getPdps() != null) {
            chantier.setPdps(pdpService.getByIds(chantierDTO.getPdps()));
        } else {
            chantier.setPdps(new ArrayList<>());
        }

        if (chantierDTO.getWorkerSelections() != null) {
            chantier.setWorkerSelections(workerSelectionService.getWorkerSelectionsByIds(chantierDTO.getWorkerSelections()));
        } else {
            chantier.setWorkerSelections(new ArrayList<>());
        }

        if (chantierDTO.getWorkers() != null) {
            chantier.setWorkers(workerService.getWorkersByIds(chantierDTO.getWorkers()));
        } else {
            chantier.setWorkers(new ArrayList<>());
        }

    }

    public Chantier toEntity(ChantierDTO chantierDTO) {
        if (chantierDTO == null) {
            return null;
        }
        Chantier chantier = new Chantier();
        setEntityFields(chantierDTO, chantier);
        return chantier;
    }

    //From Entity to DTO
    public ChantierDTO toDTO(Chantier chantier) {
        if (chantier == null) {
            return null;
        }

        ChantierDTO chantierDTO = new ChantierDTO();
        setDTOFields(chantierDTO, chantier);
        return chantierDTO;
    }

    @Override
    public List<Chantier> toEntityList(List<ChantierDTO> chantierDTOS) {
        return chantierDTOS.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChantierDTO> toDTOList(List<Chantier> chantiers) {
        return chantiers.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Chantier updateEntityFromDTO(Chantier chantier, ChantierDTO chantierDTO) {
        if(chantierDTO == null) {
            return chantier;
        }
        setEntityFields(chantierDTO, chantier);
        // Handle collection with null check

        return chantier;

    }

    @Override
    public ChantierDTO updateDTOFromEntity(ChantierDTO chantierDTO, Chantier chantier) {
        if(chantier == null) {
            return chantierDTO;
        }
        setDTOFields(chantierDTO, chantier);

        return chantierDTO;
    }


}