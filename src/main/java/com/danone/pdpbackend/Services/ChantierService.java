package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.Utils.ChantierStatus;
import com.danone.pdpbackend.entities.*;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ChantierService extends Service<Chantier> {

    Long getLastId();

    List<Chantier> getRecent();


    void addPdpToChantier(Chantier chantier, Pdp pdpId);

    <T extends Document> void addDocumentToChantier(Chantier chantier, T document);

    void addBdtToChantier(Chantier chantier, Bdt bdt);




    /**
     * Determines if a Chantier requires a PDP based on its duration or danger level.
     * @param chantierId The ID of the Chantier.
     * @return true if a PDP is required, false otherwise.
     */
    boolean requiresPdp(Long chantierId);

    /**
     * Calculates the current status of the Chantier based on dates, required documents (PDP/BDT), etc.
     * Does NOT save the status.
     * @param chantierId The ID of the Chantier.
     * @return The calculated ChantierStatus.
     */
    ChantierStatus calculateChantierStatus(Long chantierId);

    /**
     * Calculates the current status and updates the Chantier entity in the database if changed.
     * @param chantierId The ID of the Chantier.
     * @return The updated Chantier entity.
     */
    Chantier updateAndSaveChantierStatus(Long chantierId);

    /**
     * Retrieves various statistics for a given Chantier.
     * (Consider creating a dedicated Stats DTO instead of a raw Map for better structure).
     * @param chantierId The ID of the Chantier.
     * @return A map containing statistics (e.g., PDP counts by status, worker count).
     */
    Map<String, Object> getChantierStats(Long chantierId);

    /**
     * Sets the status of a chantier manually (e.g., for Start, Suspend, Cancel actions).
     * Performs validation based on current state if necessary.
     * @param chantierId The ID of the Chantier.
     * @param newStatus The new status to set.
     * @return The updated Chantier entity.
     * @throws IllegalStateException if the status transition is invalid.
     */
    Chantier setChantierStatusManually(Long chantierId, ChantierStatus newStatus);

    User getDonneurDOrdreForChantier(Long chantierId);


    //Optional<User> getDnneurDOrdreByChantierId(Long pdpId);
}
