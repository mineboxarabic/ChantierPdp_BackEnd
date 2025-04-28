package com.danone.pdpbackend.Services.Implimetations;


import com.danone.pdpbackend.Repo.*;
import com.danone.pdpbackend.Services.*;

import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.Utils.mappers.PdpMapper;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.ObjectAnswered;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.PdpDTO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PdpServiceImpl implements PdpService {
    private final AnalyseDeRisqueRepo analyseDeRisqueRepo;
    private final PermitRepo permitRepo;
    private final ChantierRepo chantierRepo;
    private final ChantierService chantierService;
    private final PdpMapper pdpMapper;
    PdpRepo pdpRepo;
    EntrepriseService entrepriseService;
    private final ObjectAnswerRepo objectAnswerRepo;
    private final RisqueService risqueService;
    private final DispositifService dispositifService;
    private final ObjectAnswerEntreprisesRepo objectAnswerEntreprisesRepo;

    public PdpServiceImpl(PdpRepo pdpRepo, EntrepriseService entrepriseService, ObjectAnswerRepo objectAnswerRepo, RisqueService risqueService, DispositifService dispositifService, ObjectAnswerEntreprisesRepo objectAnswerEntreprisesRepo, AnalyseDeRisqueRepo analyseDeRisqueRepo, PermitRepo permitRepo, ChantierRepo chantierRepo, ChantierService chantierService, PdpMapper pdpMapper) {
        this.pdpRepo = pdpRepo;
        this.entrepriseService = entrepriseService;
        this.objectAnswerRepo = objectAnswerRepo;
        this.risqueService = risqueService;
        this.dispositifService = dispositifService;
        this.objectAnswerEntreprisesRepo = objectAnswerEntreprisesRepo;
        this.analyseDeRisqueRepo = analyseDeRisqueRepo;
        this.permitRepo = permitRepo;
        this.chantierRepo = chantierRepo;
        this.chantierService = chantierService;
        this.pdpMapper = pdpMapper;
    }

    public List<Pdp> getAll() {
        return pdpRepo.findAll();
    }

    private List<ObjectAnswered> mergeObjectAnswered(List<ObjectAnswered> incoming, List<ObjectAnswered> existing, Long pdpId) {
        List<ObjectAnswered> result = new ArrayList<>();


        for (ObjectAnswered obj : incoming) {
            if (obj.getId() == null) {
                // New object to add
                result.add(objectAnswerRepo.save(obj));
            }
            else {
                // Existing object to update or delete
                ObjectAnswered existingObj = objectAnswerRepo.findById(obj.getId());
                if (existingObj != null) {
                    if (obj.getAnswer() == null) {
                        // Delete object if answer is null
                        objectAnswerRepo.delete(existingObj);
                        // Don't add to result
                    } else {
                        // Update object
                        existingObj.setAnswer(obj.getAnswer());
                        existingObj.setEe(obj.getEe());
                        existingObj.setEu(obj.getEu());
                        existingObj.setObjectType(obj.getObjectType());
                        result.add(objectAnswerRepo.save(existingObj));
                    }
                }
            }
        }

        return result;
    }
    @Override
    @Transactional
    public Pdp update(Long id, Pdp updatedPdp) {
        if (updatedPdp.getId() != null && !updatedPdp.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID and PDP ID must match");
        }

        Pdp existingPdp = getById(id);

        // ********* RISQUES *********
        existingPdp.setRelations(mergeObjectAnswered(updatedPdp.getRelations(), existingPdp.getRelations(), id));

        // Update other fields if needed
        // For example, if chantier has changed
        if (updatedPdp.getChantier() != null) {
            chantierService.addPdpToChantier(updatedPdp.getChantier(), existingPdp);
        }

        return pdpRepo.save(existingPdp);
    }
    @Override
    public Pdp create(Pdp pdp) {
        Pdp pdp1 = pdpRepo.save(pdp);

        if(pdp.getChantier() != null)
        {
         chantierService.addPdpToChantier(pdp.getChantier(), pdp);
        }
        pdpRepo.save(pdp1);

        return pdp1;
    }

    @Override
    public Pdp getById(Long id) {
        Optional<Pdp> pdpOpt = pdpRepo.findById(id);

        if (pdpOpt.isEmpty()) {
            throw new IllegalArgumentException("Pdp with id " + id + " not found");
        }

        return pdpOpt.get();
    }

    @Override
    public Boolean delete(Long id) {
        Optional<Pdp> pdpOpt = pdpRepo.findById(id);

        if (pdpOpt.isEmpty()) {
            return false;
        }

        pdpRepo.deleteById(id);
        return true;
    }

    @Override
    public Long getLastId() {
        return (long) pdpRepo.findMaxId();
    }
    @Override
    public List<Pdp> getByIds(List<Long> pdps) {
        return pdpRepo.findPdpsByIdIn(pdps);
    }
    @Override
    public List<Pdp> getRecent() {
        List<Pdp> pdps = pdpRepo.findAll();
        if (pdps.size() <= 10) {
            return pdps;
        } else {
            return pdps.subList(pdps.size() - 10, pdps.size());
        }


    }

    @Override
    public List<Worker> findWorkersByPdp(Long pdpId) {
        return pdpRepo.findById(pdpId).get().getSignatures();
    }

    @Override
    public List<ObjectAnswered> getObjectAnsweredByPdpId(Long pdpId, ObjectAnsweredObjects objectType) {
        Pdp pdp = getById(pdpId);
        return pdp.getRelations();
    }

    /*

    @Override
    @Transactional
    public ObjectAnswered addObjectAnswered(Long pdpId,  ObjectAnswered objectAnswered, ObjectAnsweredObjects objectAnsweredObject) {
        Pdp pdp = getById(pdpId);
        if(objectAnsweredObject == ObjectAnsweredObjects.RISQUE){
            pdp.getRisques().add(objectAnswered);
        } else if(objectAnsweredObject == ObjectAnsweredObjects.DISPOSITIF){
            pdp.getDispositifs().add(objectAnswered);
        } else if(objectAnsweredObject == ObjectAnsweredObjects.PERMIT){
            pdp.getPermits().add(objectAnswered);
        }

        objectAnswered = objectAnswerRepo.save(objectAnswered);
        pdpRepo.save(pdp);
        return objectAnswered;
    }

    @Override
    @Transactional
    public ObjectAnswered removeObjectAnswered(Long pdpId, Long id, ObjectAnsweredObjects objectAnsweredObject) {
        // First remove it from the Pdp
        // Then remove it from the DB
        Pdp pdp = getById(pdpId);
        ObjectAnswered objectAnswered = objectAnswerRepo.findById(id);

        if(objectAnsweredObject == ObjectAnsweredObjects.RISQUE){
            pdp.getRisques().remove(objectAnswered);
        } else if(objectAnsweredObject == ObjectAnsweredObjects.DISPOSITIF){
            pdp.getDispositifs().remove(objectAnswered);
        } else if(objectAnsweredObject == ObjectAnsweredObjects.PERMIT){
            pdp.getPermits().remove(objectAnswered);
        }

        objectAnswerRepo.delete(objectAnswered);
        pdpRepo.save(pdp);
        return objectAnswered;

    }



    @Override
    @Transactional
    public List<ObjectAnswered> addMultipleObjectsToPdp(Long pdpId, List<ObjectAnswered> objectAnswereds, ObjectAnsweredObjects objectType) {
        Pdp pdp = getById(pdpId);

        for(ObjectAnswered objectAnswered : objectAnswereds){
            if(objectType == ObjectAnsweredObjects.RISQUE){
                pdp.getRisques().add(objectAnswered);
            } else if(objectType == ObjectAnsweredObjects.DISPOSITIF){
                pdp.getDispositifs().add(objectAnswered);
            } else if(objectType == ObjectAnsweredObjects.PERMIT){
                pdp.getPermits().add(objectAnswered);
            }
        }

        pdpRepo.save(pdp);
        return objectAnswereds;
    }
*/

    @Override
    @Transactional
    public Pdp saveOrUpdatePdp(PdpDTO dto) {
        Pdp pdp;

        if (dto.getId() != null) {
            pdp = getById(dto.getId());
            if (pdp == null) {
                throw new RuntimeException("Pdp not found with ID: " + dto.getId());
            }

            pdpMapper.updateEntityFromDTO(pdp, dto);
        } else {
            pdp = pdpMapper.toEntity(dto);
        }

        return pdpRepo.save(pdp);
    }

  /*  @Override
    public List<ObjectAnswered> removeMultipleObjectsFromPdp(Long pdpId, List<Long> ids, ObjectAnsweredObjects objectAnsweredObject) {
        // First remove it from the Pdp
        // Then remove it from the DB
        Pdp pdp = getById(pdpId);
        List<ObjectAnswered> objectAnswereds = objectAnswerRepo.findObjectAnsweredByIdIn(ids);

        List<ObjectAnswered> deletedAnswereds = new ArrayList<>();
        for(ObjectAnswered objectAnswered : objectAnswereds){
            if(objectAnsweredObject == ObjectAnsweredObjects.RISQUE){
                pdp.getRisques().remove(objectAnswered);
            } else if(objectAnsweredObject == ObjectAnsweredObjects.DISPOSITIF){
                pdp.getDispositifs().remove(objectAnswered);
            } else if(objectAnsweredObject == ObjectAnsweredObjects.PERMIT){
                pdp.getPermits().remove(objectAnswered);
            }
            deletedAnswereds.add(objectAnswered);
            objectAnswerRepo.delete(objectAnswered);
        }
        pdpRepo.save(pdp);

        return deletedAnswereds;
    }

*/
}