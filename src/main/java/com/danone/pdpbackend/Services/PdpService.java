package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.Utils.Image.ImageModel;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.ObjectAnswered;
import com.danone.pdpbackend.entities.dto.PdpDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface PdpService extends CommonDocumentServiceInterface<Pdp> {
    Pdp update(Long id, Pdp pdp);

  //  Pdp updatePdp(Pdp updatedPdp, Long id);

    Pdp create(Pdp pdp);

    Long getLastId();

    List<Pdp> getRecent();


    List<Pdp> getByIds(List<Long> pdps);

    List<ObjectAnswered> getObjectAnsweredsByPdpId(Long pdpId, ObjectAnsweredObjects objectType);



    Pdp saveOrUpdatePdp(PdpDTO dto);




}
