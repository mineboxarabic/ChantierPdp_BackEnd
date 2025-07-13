package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Repo.DocumentRepo;
import com.danone.pdpbackend.Repo.DocumentSignatureRepository;
import com.danone.pdpbackend.Repo.WorkerRepo;
import com.danone.pdpbackend.entities.Document;
import com.danone.pdpbackend.entities.DocumentSignature;
import com.danone.pdpbackend.entities.Worker;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
@SpringBootTest
@Transactional
public class DocumentSignatureRepositoryIntegrationTest {

    @Autowired
    private DocumentSignatureRepository documentSignatureRepository;

    @Autowired
    private DocumentRepo documentRepository;

    @Autowired
    private WorkerRepo workerRepository;

    @Test
    void testFindWorkersByDocumentId() {
        // Create test data
        Document document = new Document();
        document = documentRepository.save(document);

        Worker worker1 = new Worker();
        worker1.setPrenom("Worker 1");
        worker1 = workerRepository.save(worker1);

        Worker worker2 = new Worker();
        worker2.setPrenom("Worker 2");
        worker2 = workerRepository.save(worker2);

        DocumentSignature signature1 = new DocumentSignature();
        signature1.setDocument(document);
        signature1.setWorker(worker1);
        documentSignatureRepository.save(signature1);

        DocumentSignature signature2 = new DocumentSignature();
        signature2.setDocument(document);
        signature2.setWorker(worker2);
        documentSignatureRepository.save(signature2);

        // Test the query
        List<Worker> workers = documentSignatureRepository.findWorkersByDocumentId(document.getId());

        // Assertions
        assertThat(workers).isNotNull();
        assertThat(workers.size()).isEqualTo(2);
        assertThat(workers.stream().anyMatch(w -> w.getPrenom().equals("Worker 1"))).isTrue();
        assertThat(workers.stream().anyMatch(w -> w.getPrenom().equals("Worker 2"))).isTrue();
    }
}