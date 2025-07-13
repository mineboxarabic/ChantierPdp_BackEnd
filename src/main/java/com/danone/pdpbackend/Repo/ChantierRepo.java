package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.Utils.ChantierStatus;
import com.danone.pdpbackend.entities.Chantier;
import com.danone.pdpbackend.entities.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface ChantierRepo extends Repository<Chantier, Integer> {

    Chantier save(Chantier chantier);

    void delete(Chantier chantier);

    List<Chantier> findAll();

    Optional<Chantier> findById(Long id);

    @Query("SELECT MAX(id) FROM Chantier")
    int findMaxId();

    void deleteById(Long id);

    List<Chantier> getChantierById(Long id);

    List<Chantier> findAllByDonneurDOrdre(User user);

    List<Chantier> findChantiersByIdIn(List<Long> ids);

    List<Chantier> findByNom(String nom);


    boolean existsById(Long id);
    // New method for active chantiers in a given month
    @Query("SELECT COUNT(c.id) FROM Chantier c WHERE " +
            "c.dateDebut <= :monthEnd AND " +                         // Started on or before the end of the month
            "(c.dateFin IS NULL OR c.dateFin >= :monthStart) AND " +  // Ends on or after the start of the month, or is ongoing
            "c.status NOT IN :terminalStatuses")                     // Not already in a terminal state before relevant period
    Long countChantiersActiveDuringMonth(
            @Param("monthStart") Date monthStart,
            @Param("monthEnd") Date monthEnd,
            @Param("terminalStatuses") List<ChantierStatus> terminalStatuses
    );

    // If you want to count chantiers currently in specific "active-like" statuses
    Long countByStatusIn(List<ChantierStatus> statuses);

    void deleteAll();

    //Query to find recent chantiers sorted by date
    @Query("SELECT c FROM Chantier c ORDER BY c.createdAt DESC")
    List<Chantier> findRecent();




    @Query("SELECT c FROM Chantier c WHERE :documentId MEMBER OF c.pdps OR :documentId MEMBER OF c.bdts")
    Optional<Chantier> findChantierByDocumentId(Long documentId);
}
