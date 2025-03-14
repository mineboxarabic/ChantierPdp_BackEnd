package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.BDTRepo;
import com.danone.pdpbackend.Repo.ObjectAnswerRepo;
import com.danone.pdpbackend.Repo.RisqueRepo;
import com.danone.pdpbackend.Repo.AuditSecuRepo;
import com.danone.pdpbackend.Services.BDTService;
import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.entities.BDT.BDT;
import com.danone.pdpbackend.entities.ObjectAnswered;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.Risque;
import com.danone.pdpbackend.entities.AuditSecu;
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

    public BDTServiceImpl(BDTRepo bdtRepo, RisqueRepo risqueRepo, AuditSecuRepo auditSecuRepo, ObjectAnswerRepo objectAnswerRepo) {
        this.bdtRepo = bdtRepo;
        this.risqueRepo = risqueRepo;
        this.auditSecuRepo = auditSecuRepo;
        this.objectAnswerRepo = objectAnswerRepo;
    }

    @Override
    public List<BDT> getAllBDT() {
        return bdtRepo.findAll();
    }

    @Override
    public BDT getBDT(Long id) {
        return bdtRepo.findById(id).orElse(null);
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
        BDT bdt = getBDT(bdtId);
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
        BDT bdt = getBDT(bdtId);
        ObjectAnswered objectAnswered = objectAnswerRepo.findById(id);

        if(objectAnsweredObject == ObjectAnsweredObjects.RISQUE)
        {
            bdt.getRisques().add(objectAnswered);
        }
        else if(objectAnsweredObject == ObjectAnsweredObjects.AUDIT)
        {
            bdt.getAuditSecu().add(objectAnswered);
        }

        objectAnswerRepo.delete(objectAnswered);
        bdtRepo.save(bdt);
        return objectAnswered;
    }
}