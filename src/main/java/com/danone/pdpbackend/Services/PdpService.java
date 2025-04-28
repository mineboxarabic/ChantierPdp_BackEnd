package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.ObjectAnswered;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.PdpDTO;

import java.util.List;

public interface PdpService extends Service<Pdp>{
    Pdp update(Long id, Pdp pdp);

  //  Pdp updatePdp(Pdp updatedPdp, Long id);

    Pdp create(Pdp pdp);

    Long getLastId();

    List<Pdp> getRecent();

    List<Worker> findWorkersByPdp(Long pdpId);

    List<Pdp> getByIds(List<Long> pdps);

    List<ObjectAnswered> getObjectAnsweredByPdpId(Long pdpId, ObjectAnsweredObjects objectType);

    /*//ObjectAnswereds
    ObjectAnswered addObjectAnswered(Long pdpId,  ObjectAnswered objectAnswered, ObjectAnsweredObjects objectAnsweredObject);
    ObjectAnswered removeObjectAnswered(Long pdpId, Long id, ObjectAnsweredObjects objectAnsweredObject);
    List<ObjectAnswered> addMultipleObjectsToPdp(Long pdpId, List<ObjectAnswered> objectAnswereds, ObjectAnsweredObjects objectType);
*/
    Pdp saveOrUpdatePdp(PdpDTO dto);

  //  List<ObjectAnswered> removeMultipleObjectsFromPdp(Long pdpId, List<Long> ids, ObjectAnsweredObjects objectAnsweredObject);
}
