package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.entities.BDT.BDT;
import com.danone.pdpbackend.entities.Risque;
import com.danone.pdpbackend.entities.AuditSecu;

import java.util.List;

public interface BDTService {
    List<BDT> getAllBDT();
    BDT getBDT(Long id);
    BDT createBDT(BDT bdt);
    BDT updateBDT(Long id, BDT bdt);
    boolean deleteBDT(Long id);
    Risque addRisqueToBDT(Long bdtId, Long risqueId);
    AuditSecu addAuditToBDT(Long bdtId, Long auditId);
}