package com.danone.pdpbackend.Services.Implimetations;


import com.danone.pdpbackend.Repo.*;
import com.danone.pdpbackend.Services.DispositifService;
import com.danone.pdpbackend.Services.EntrepriseService;
import com.danone.pdpbackend.Services.PdpService;

import com.danone.pdpbackend.Services.RisqueService;
import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.dto.PdpUpdateDTO;
import com.danone.pdpbackend.entities.ObjectAnsweredEntreprises;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.ObjectAnswered;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PdpServiceImpl implements PdpService {
    private final AnalyseDeRisqueRepo analyseDeRisqueRepo;
    private final PermitRepo permitRepo;
    PdpRepo pdpRepo;
    EntrepriseService entrepriseService;
    private final ObjectAnswerRepo objectAnswerRepo;
    private final RisqueService risqueService;
    private final DispositifService dispositifService;
    private final ObjectAnswerEntreprisesRepo objectAnswerEntreprisesRepo;

    public PdpServiceImpl(PdpRepo pdpRepo, EntrepriseService entrepriseService, ObjectAnswerRepo objectAnswerRepo, RisqueService risqueService, DispositifService dispositifService, ObjectAnswerEntreprisesRepo objectAnswerEntreprisesRepo, AnalyseDeRisqueRepo analyseDeRisqueRepo, PermitRepo permitRepo) {
        this.pdpRepo = pdpRepo;
        this.entrepriseService = entrepriseService;
        this.objectAnswerRepo = objectAnswerRepo;
        this.risqueService = risqueService;
        this.dispositifService = dispositifService;
        this.objectAnswerEntreprisesRepo = objectAnswerEntreprisesRepo;
        this.analyseDeRisqueRepo = analyseDeRisqueRepo;
        this.permitRepo = permitRepo;
    }

    public List<Pdp> getAllPdp() {
        return pdpRepo.findAll();
    }

/*    @Override
    public Pdp updatePdp(Pdp updatedPdp, Long id) {
        Optional<Pdp> existingPdpOpt = pdpRepo.findById(id);

        if (existingPdpOpt.isEmpty()) {
            throw new IllegalArgumentException("Pdp with id " + id + " not found");
        }

        Pdp existingPdp = existingPdpOpt.get();

        //pdpMapper.updatePdpFromDto(updatedPdp, existingPdp);

        // Update fields only if they are non-null
        if (updatedPdp.getOperation() != null) existingPdp.setOperation(updatedPdp.getOperation());
        if (updatedPdp.getLieuintervention() != null) existingPdp.setLieuintervention(updatedPdp.getLieuintervention());
        if (updatedPdp.getDatedebuttravaux() != null) existingPdp.setDatedebuttravaux(updatedPdp.getDatedebuttravaux());
        if (updatedPdp.getDatefintravaux() != null) existingPdp.setDatefintravaux(updatedPdp.getDatefintravaux());
        if (updatedPdp.getEffectifmaxisurchantier() != null) existingPdp.setEffectifmaxisurchantier(updatedPdp.getEffectifmaxisurchantier());
        if (updatedPdp.getNombreinterimaires() != null) existingPdp.setNombreinterimaires(updatedPdp.getNombreinterimaires());
        if (updatedPdp.getHoraireDeTravail() != null) existingPdp.setHoraireDeTravail(updatedPdp.getHoraireDeTravail());
        if (updatedPdp.getHorairesdetail() != null) existingPdp.setHorairesdetail(updatedPdp.getHorairesdetail());
        if (updatedPdp.getIcpdate() != null) existingPdp.setIcpdate(updatedPdp.getIcpdate());
        if (updatedPdp.getEntrepriseexterieure() != null) existingPdp.setEntrepriseexterieure(updatedPdp.getEntrepriseexterieure());

        if (updatedPdp.getEntrepriseDInspection() != null) existingPdp.setEntrepriseDInspection(updatedPdp.getEntrepriseDInspection());
        if(updatedPdp.getDateInspection() != null) existingPdp.setDateInspection(updatedPdp.getDateInspection());

        if(updatedPdp.getEntrepriseutilisatrice() != null) existingPdp.setEntrepriseutilisatrice(entrepriseService.updateEntreprise(updatedPdp.getEntrepriseutilisatrice(), updatedPdp.getEntrepriseutilisatrice().getId()));


        if (updatedPdp.getMisesEnDisposition() != null) existingPdp.setMisesEnDisposition(updatedPdp.getMisesEnDisposition());
        if (updatedPdp.getMedecintravaileu() != null) existingPdp.setMedecintravaileu(updatedPdp.getMedecintravaileu());
        if (updatedPdp.getMedecintravailee() != null) existingPdp.setMedecintravailee(updatedPdp.getMedecintravailee());
        if (updatedPdp.getDateprevenircssct() != null) existingPdp.setDateprevenircssct(updatedPdp.getDateprevenircssct());
        if (updatedPdp.getDateprev() != null) existingPdp.setDateprev(updatedPdp.getDateprev());
        if (updatedPdp.getLocation() != null) existingPdp.setLocation(updatedPdp.getLocation());



        pdpRepo.save(existingPdp);
        return existingPdp;
    }*/


    @Override
    public Pdp updatePdp(Pdp updatedPdp, Long id) {
        Pdp existingPdp = pdpRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pdp with id " + id + " not found"));

        //Get the disposifs that are in existing pdp and not in updated pdp


        //updateEntrepriseUtilisatrice(updatedPdp, existingPdp);
        updateObjectAnsweredList(updatedPdp.getRisques(),existingPdp.getRisques(), objectAnswerRepo);
        updateObjectAnsweredList(updatedPdp.getDispositifs(),existingPdp.getDispositifs(), objectAnswerRepo);
        updateAnalyseDeRisques(updatedPdp);
        updateObjectAnsweredList(updatedPdp.getPermits(),existingPdp.getPermits(), objectAnswerRepo);

        updateSimpleFields(updatedPdp, existingPdp);

        pdpRepo.save(existingPdp);
        return existingPdp;
    }


    private void updateObjectAnsweredList(List<ObjectAnswered> updatedList,List<ObjectAnswered> existingList, ObjectAnswerRepo repo) {


        //We have three cases either we add new objects of objectAnswered or we update the existing ones, or we delete the ones that are not in the updated list

        //First we add the new objects
/*        for (ObjectAnswered item : updatedList) {
            if (item.getId() == null) {
                item = repo.save(item);
                existingList.add(item);
            }
        }*/

        //Then we update the existing ones
        for (ObjectAnswered item : updatedList) {
            if (item.getId() != null) {
                ObjectAnswered existingItem = repo.findById(item.getId());
                existingItem.setAnswer(item.getAnswer());
                repo.save(existingItem);
            }
        }

        //Finally we delete the ones that are not in the updated list
/*
        for (ObjectAnswered item : existingList) {
            if (!updatedList.contains(item)) {
                repo.delete(item);
            }
        }
*/


    }

    private void updateAnalyseDeRisques(Pdp updatedPdp) {
        if (updatedPdp.getAnalyseDeRisques() != null) {
            for (ObjectAnsweredEntreprises item : updatedPdp.getAnalyseDeRisques()) {
                ObjectAnsweredEntreprises existingItem = objectAnswerEntreprisesRepo.findById(item.getId());
                existingItem.setEE(item.getEE());
                existingItem.setEU(item.getEU());
                objectAnswerEntreprisesRepo.save(existingItem);
            }
        }
    }

    private void updateSimpleFields(Pdp updatedPdp, Pdp existingPdp) {
        for (Field field : Pdp.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object newValue = field.get(updatedPdp);
                if (newValue != null && !isSpecialField(field.getName())) {
                    field.set(existingPdp, newValue);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error updating field: " + field.getName(), e);
            }
        }
    }

    private boolean isSpecialField(String fieldName) {
        return List.of("entrepriseutilisatrice", "risques", "dispositifs", "analyseDeRisques", "permits").contains(fieldName);
    }



    @Override
    public Pdp createPdp(PdpUpdateDTO pdp) {
        Pdp pdpEntity = new Pdp();

        //Put all the fields from pdp into pdpEntity with for loop
        //log.info("Creating pdp with id " + (pdpRepo.findMaxId() + 1));
       // pdpEntity.setId((long) (pdpRepo.findMaxId() + 1));
 /*       pdpEntity.setOperation(pdp.getOperation());
        pdpEntity.setLieuintervention(pdp.getLieuintervention());
        pdpEntity.setDatedebuttravaux(pdp.getDatedebuttravaux());
        pdpEntity.setDatefintravaux(pdp.getDatefintravaux());
        pdpEntity.setEffectifmaxisurchantier(pdp.getEffectifmaxisurchantier());
        pdpEntity.setNombreinterimaires(pdp.getNombreinterimaires());
*/
        pdpEntity.setHoraireDeTravail(pdp.getHoraireDeTravail());

       // pdpEntity.setHorairesdetail(pdp.getHorairesdetail());
        pdpEntity.setIcpdate(pdp.getIcpdate());
       // pdpEntity.setEntrepriseexterieure(pdp.getEntrepriseexterieure());
        //pdpEntity.setEntrepriseutilisatrice(entreprise);
       // pdpEntity.setMedecintravaileu(pdp.getMedecintravaileu());
      //  pdpEntity.setMedecintravailee(pdp.getMedecintravailee());
        pdpEntity.setDatePrevenirCSSCT(pdp.getDateprevenircssct());
        pdpEntity.setDatePrev(pdp.getDateprev());
      //  pdpEntity.setLocation(pdp.getLocation());

        pdpEntity.setDateInspection(pdp.getDateInspection());
        pdpEntity.setEntrepriseDInspection(pdp.getEntrepriseDInspection());
     //   pdpEntity.setEntrepriseutilisatrice(pdp.getEntrepriseetutilisatrise());
     //   pdpEntity.setEntrepriseexterieure(pdp.getEntrepriseexterieure());

        return pdpRepo.save(pdpEntity);
    }

    @Override
    public Pdp getPdp(Long id) {
        Optional<Pdp> pdpOpt = pdpRepo.findById(id);

        if (pdpOpt.isEmpty()) {
            throw new IllegalArgumentException("Pdp with id " + id + " not found");
        }

        return pdpOpt.get();
    }

    @Override
    public boolean deletePdp(Long id) {
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
    public List<Pdp> getRecent() {
        List<Pdp> pdps = pdpRepo.findAll();
        if (pdps.size() <= 10) {
            return pdps;
        } else {
            return pdps.subList(pdps.size() - 10, pdps.size());
        }


    }

    public ObjectAnswered saveNewObjectAnswer(Long pdpId, Long risqueId, String type) {
        ObjectAnswered objectAnswered = new ObjectAnswered();
        objectAnswered.setAnswer(false);

        if("Risque".equals(type)){
            objectAnswered.setRisque(risqueService.getRisqueById(risqueId));
        } else if ("Dispositif".equals(type)){
            objectAnswered.setDispositif(dispositifService.getDispositifById(risqueId));
        }
        else if ("Permit".equals(type)){
            objectAnswered.setPermit(permitRepo.findPermitById(risqueId));
        }



        objectAnswered = objectAnswerRepo.save(objectAnswered);

        return objectAnswered;
    }

    @Override
    public ObjectAnswered addRisqueToPdp(Long pdpId, Long risqueId) {
        Pdp pdp = getPdp(pdpId);
        ObjectAnswered objectAnswered = saveNewObjectAnswer(pdpId, risqueId, "Risque");
        objectAnswered = objectAnswerRepo.save(objectAnswered);
        pdp.getRisques().add(objectAnswered);
        pdpRepo.save(pdp);
        return objectAnswered;
    }

    @Override
    public ObjectAnswered addDispositifToPdp(Long pdpId, Long dispositifId) {
        Pdp pdp = getPdp(pdpId);
        ObjectAnswered objectAnswered = saveNewObjectAnswer(pdpId, dispositifId,"Dispositif");
        objectAnswered = objectAnswerRepo.save(objectAnswered);
        //pdp.getDispositifs().add(objectAnswered);
        pdpRepo.save(pdp);
        return objectAnswered;
    }

    @Override
    public ObjectAnsweredEntreprises addAnalyseToPdp(Long pdpId, Long analyseId) {
        Pdp pdp = getPdp(pdpId);

        ObjectAnsweredEntreprises objectAnsweredEntreprises = new ObjectAnsweredEntreprises();
        objectAnsweredEntreprises.setEU(false);
        objectAnsweredEntreprises.setEE(false);
        objectAnsweredEntreprises.setAnalyseDeRisque(analyseDeRisqueRepo.findAnalyseDeRisqueById(analyseId));

        objectAnsweredEntreprises = objectAnswerEntreprisesRepo.save(objectAnsweredEntreprises);

        pdp.getAnalyseDeRisques().add(objectAnsweredEntreprises);
        pdpRepo.save(pdp);
        return objectAnsweredEntreprises;
    }

    @Override
    public ObjectAnswered addPermitToPdp(Long pdpId, Long permitId) {

        Pdp pdp = getPdp(pdpId);

        ObjectAnswered objectAnswered = saveNewObjectAnswer(pdpId, permitId, "Permit");
        objectAnswered = objectAnswerRepo.save(objectAnswered);
        pdp.getPermits().add(objectAnswered);
        pdpRepo.save(pdp);
        return objectAnswered;
    }

    @Override
    public ObjectAnswered removePermitFromPdp(Long pdpId, Long permitId) {
        Pdp pdp = getPdp(pdpId);
        ObjectAnswered objectAnswered = objectAnswerRepo.findById(permitId);
        pdp.getPermits().remove(objectAnswered);
        pdpRepo.save(pdp);
        objectAnswerRepo.delete(objectAnswered);
        return objectAnswered;
    }

    @Override
    public ObjectAnswered removeObjectAnswered(Long pdpId, Long id, ObjectAnsweredObjects objectAnsweredObject) {

        Pdp pdp = getPdp(pdpId);
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
    public ObjectAnswered addObjectAnswered(Long pdpId, Long id, ObjectAnsweredObjects objectAnsweredObject) {
        Pdp pdp = getPdp(pdpId);
        ObjectAnswered objectAnswered = new ObjectAnswered();
        objectAnswered.setAnswer(false);

        if(objectAnsweredObject == ObjectAnsweredObjects.RISQUE){
            objectAnswered.setRisque(risqueService.getRisqueById(id));
            pdp.getRisques().add(objectAnswered);
        } else if(objectAnsweredObject == ObjectAnsweredObjects.DISPOSITIF){
            objectAnswered.setDispositif(dispositifService.getDispositifById(id));
            pdp.getDispositifs().add(objectAnswered);
        } else if(objectAnsweredObject == ObjectAnsweredObjects.PERMIT){
            objectAnswered.setPermit(permitRepo.findPermitById(id));
            pdp.getPermits().add(objectAnswered);
        }

        objectAnswered = objectAnswerRepo.save(objectAnswered);
        pdpRepo.save(pdp);
        return objectAnswered;
    }

    @Override
    public ObjectAnsweredEntreprises removeAnalyse(Long pdpId, Long analyseId) {
        Pdp pdp = getPdp(pdpId);
        ObjectAnsweredEntreprises objectAnsweredEntreprises = objectAnswerEntreprisesRepo.findById(analyseId);


        pdp.getAnalyseDeRisques().remove(objectAnsweredEntreprises);
        pdpRepo.save(pdp);

        objectAnswerEntreprisesRepo.deleteById(analyseId);

        return objectAnsweredEntreprises;
    }


}