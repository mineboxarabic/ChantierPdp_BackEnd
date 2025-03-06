package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.dto.PdpUpdateDTO;
import com.danone.pdpbackend.entities.ObjectAnsweredEntreprises;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.ObjectAnswered;

import java.util.List;

public interface PdpService {


    List<Pdp> getAllPdp();

    Pdp updatePdp(Pdp pdp, Long id);

  //  Pdp updatePdp(Pdp updatedPdp, Long id);

    Pdp createPdp(PdpUpdateDTO pdp);

    Pdp getPdp(Long id);

    boolean deletePdp(Long id);

    Long getLastId();

    List<Pdp> getRecent();

    ObjectAnswered addRisqueToPdp(Long pdpId, Long risqueId);

    ObjectAnswered addDispositifToPdp(Long pdpId, Long dispositifId);

    ObjectAnsweredEntreprises addAnalyseToPdp(Long pdpId, Long analyseId);

    ObjectAnswered addPermitToPdp(Long pdpId, Long permitId);

    ObjectAnswered removePermitFromPdp(Long pdpId, Long permitId);

    ObjectAnswered removeObjectAnswered(Long permitId, Long id, ObjectAnsweredObjects objectAnsweredObject);
    ObjectAnswered addObjectAnswered(Long pdpId, Long id, ObjectAnsweredObjects objectAnsweredObject);

}
