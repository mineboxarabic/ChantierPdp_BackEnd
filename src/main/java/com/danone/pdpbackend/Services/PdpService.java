package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.ObjectAnswered;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.PdpDTO;

import java.util.List;
import java.util.Map;

public interface PdpService extends DocumentService<Pdp>{
    Pdp update(Long id, Pdp pdp);

  //  Pdp updatePdp(Pdp updatedPdp, Long id);

    Pdp create(Pdp pdp);

    Long getLastId();

    List<Pdp> getRecent();


    List<Pdp> getByIds(List<Long> pdps);

    List<ObjectAnswered> getObjectAnsweredsByPdpId(Long pdpId, ObjectAnsweredObjects objectType);



    Pdp saveOrUpdatePdp(PdpDTO dto);


    // --- New Methods based on Plan ---

    /**
     * Calculates the current status of the Pdp based on expiry, permits, signatures, and Chantier status.
     * Does NOT save the status.
     * @param pdpId The ID of the Pdp.
     * @return The calculated DocumentStatus.
     */
    DocumentStatus calculatePdpStatus(Long pdpId);

    /**
     * Calculates the current status and updates the Pdp entity in the database if changed.
     * @param pdpId The ID of the Pdp.
     * @return The updated Pdp entity.
     */
    Pdp updateAndSavePdpStatus(Long pdpId);

    /**
     * Gets the count of all PDPs grouped by their current status.
     * @return A map where keys are DocumentStatus and values are counts.
     */
    Map<DocumentStatus, Long> getPdpStatusCounts();

    /**
     * Manually triggers the check for expired PDPs and initiates renewal if needed.
     * (Alternatively, this logic could be in a separate scheduled service).
     */
    void triggerPdpRenewalCheck();

}
