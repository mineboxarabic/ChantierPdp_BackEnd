package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.entities.Document;
import com.danone.pdpbackend.entities.Pdp;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface DocumentRepo extends Repository<Document, Integer> {

 /*   Pdp save(Pdp pdp);

    void delete(Pdp pdp);

    List<Pdp>  findAll();


    Optional<Pdp> findById(Long id);

    void deleteById(Long id);

    List<Pdp> findPdpsByIdIn(Collection<Long> ids);

    Boolean existsById(Long id);

    List<Pdp> findByStatusInAndCreationDateBefore(List<DocumentStatus> ready, LocalDate oneYearAgo);*/

    Document save(Document document);
    void delete(Document document);
    List<Document> findAll();
    Optional<Document> findById(Long id);
    void deleteById(Long id);
    List<Document> findDocumentsByIdIn(Collection<Long> ids);
    Boolean existsById(Long id);
    List<Document> findByStatusInAndCreationDateBefore(List<DocumentStatus> ready, LocalDate oneYearAgo);

    List<Document> findDocumentsByIdIn(List<Long> ids);

    // New method for documents currently needing action
    Long countByStatus(DocumentStatus status);
     @Query("SELECT COUNT(d.id) FROM Document d WHERE d.status = :status AND d.lastUpdate >= :monthStart AND d.lastUpdate < :monthEnd")
     Long countDocumentsBecameStatusInMonth(@Param("status") DocumentStatus status, @Param("monthStart") java.time.LocalDate monthStart, @Param("monthEnd") java.time.LocalDate monthEnd);

    // New method for documents in a specific status
    Long countByStatusIn(List<DocumentStatus> statuses);

    void deleteAll();
}
