package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Repo.BDTRepo;
import com.danone.pdpbackend.Repo.RisqueRepo;
import com.danone.pdpbackend.Repo.AuditSecuRepo;
import com.danone.pdpbackend.Services.BDTService;
import com.danone.pdpbackend.entities.BDT.BDT;
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

    public BDTServiceImpl(BDTRepo bdtRepo, RisqueRepo risqueRepo, AuditSecuRepo auditSecuRepo) {
        this.bdtRepo = bdtRepo;
        this.risqueRepo = risqueRepo;
        this.auditSecuRepo = auditSecuRepo;
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
    public Risque addRisqueToBDT(Long bdtId, Long risqueId) {
        BDT bdt = getBDT(bdtId);
        Risque risque = risqueRepo.findRisqueById(risqueId);
        bdt.getRisques().add(risque);
        bdtRepo.save(bdt);
        return risque;
    }

    @Override
    public AuditSecu addAuditToBDT(Long bdtId, Long auditId) {
        BDT bdt = getBDT(bdtId);
        AuditSecu audit = auditSecuRepo.findById(auditId).orElseThrow(() -> new IllegalArgumentException("AuditSecu not found"));
        bdt.getAuditSecu().add(audit);
        bdtRepo.save(bdt);
        return audit;
    }
}