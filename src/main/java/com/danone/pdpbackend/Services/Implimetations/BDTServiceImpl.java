package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.*;
import com.danone.pdpbackend.Services.BDTService;
import com.danone.pdpbackend.Services.DispositifService;
import com.danone.pdpbackend.Services.RisqueService;
import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.entities.BDT.BDT;
import com.danone.pdpbackend.entities.ObjectAnswered;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class BDTServiceImpl implements BDTService {
    private final BDTRepo bdtRepo;
    private final RisqueRepo risqueRepo;
    private final AuditSecuRepo auditSecuRepo;
    private final ObjectAnswerRepo objectAnswerRepo;
    private final RisqueService risqueService;
    private final DispositifService dispositifService;
    private final PermitRepo permitRepo;

    public BDTServiceImpl(BDTRepo bdtRepo, RisqueRepo risqueRepo, AuditSecuRepo auditSecuRepo, ObjectAnswerRepo objectAnswerRepo, RisqueService risqueService, DispositifService dispositifService, PermitRepo permitRepo) {
        this.bdtRepo = bdtRepo;
        this.risqueRepo = risqueRepo;
        this.auditSecuRepo = auditSecuRepo;
        this.objectAnswerRepo = objectAnswerRepo;
        this.risqueService = risqueService;
        this.dispositifService = dispositifService;
        this.permitRepo = permitRepo;
    }

    @Override
    public List<BDT> getAllBDT() {
        return bdtRepo.findAll();
    }

    @Override
    public BDT getBDTById(Long id) {
        return bdtRepo.findById(id).orElse(null);
    }

    @Override
    public List<BDT> getBDTsByIds(List<Long> id) {
        return bdtRepo.findBDTsByIdIn(id);
    }

    @Override
    public BDT createBDT(BDT bdt) {
        return bdtRepo.save(bdt);
    }

    @Override
    public BDT updateBDT(Long id, BDT bdt) {
        if (!bdtRepo.existsById(id)) {
            throw new IllegalArgumentException("BDT with id " + id + " not found");
        }
        bdt.setId(id);
        return bdtRepo.save(bdt);
    }

    @Override
    public boolean deleteBDT(Long id) {
        if (!bdtRepo.existsById(id)) {
            return false;
        }
        bdtRepo.deleteById(id);
        return true;
    }


    @Override
    public ObjectAnswered removeObjectAnswered(Long bdtId, Long id, ObjectAnsweredObjects objectAnsweredObject) {
        BDT bdt = getBDTById(bdtId);
        ObjectAnswered objectAnswered = objectAnswerRepo.findById(id);

        if(objectAnsweredObject == ObjectAnsweredObjects.RISQUE){
            bdt.getRisques().remove(objectAnswered);
        } else if(objectAnsweredObject == ObjectAnsweredObjects.AUDIT){
            bdt.getAuditSecu().remove(objectAnswered);
        }
        objectAnswerRepo.delete(objectAnswered);
        bdtRepo.save(bdt);
        return objectAnswered;
    }

    @Override
    public ObjectAnswered addObjectAnswered(Long bdtId, Long id, ObjectAnsweredObjects objectAnsweredObject) {
       BDT bdt = getBDTById(bdtId);
        ObjectAnswered objectAnswered = new ObjectAnswered();
        objectAnswered.setAnswer(false);
        objectAnswered = objectAnswerRepo.save(objectAnswered);

        if(objectAnsweredObject == ObjectAnsweredObjects.RISQUE){
            bdt.getRisques().add(objectAnswered);
        } else if(objectAnsweredObject == ObjectAnsweredObjects.AUDIT){
            bdt.getAuditSecu().add(objectAnswered);
        }
        bdtRepo.save(bdt);
        return objectAnswered;
    }
}