package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.entities.BDT.BDT;
import com.danone.pdpbackend.entities.ObjectAnswered;

import java.util.List;

public interface BDTService {
    List<BDT> getAllBDT();

    BDT getBDTById(Long id);

    List<BDT> getBDTsByIds(List<Long> id);

    BDT createBDT(BDT bdt);
    BDT updateBDT(Long id, BDT bdt);
    boolean deleteBDT(Long id);
    ObjectAnswered removeObjectAnswered(Long permitId, Long id, ObjectAnsweredObjects objectAnsweredObject);
    ObjectAnswered addObjectAnswered(Long pdpId, Long id, ObjectAnsweredObjects objectAnsweredObject);

}