package com.danone.pdpbackend.Repo;

import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.entities.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@org.springframework.stereotype.Repository
public interface DocumentRepo extends JpaRepository<Document, Long> {

    Document save(Document document);
    void delete(Document document);
    List<Document> findAll();
    void deleteById(Long id);
    List<Document> findDocumentsByIdIn(Collection<Long> ids);
    boolean existsById(Long id);
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
