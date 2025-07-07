package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Repo.DocumentRepo;
import com.danone.pdpbackend.Repo.DocumentSignatureRepository;
import com.danone.pdpbackend.Repo.WorkerRepo;
import com.danone.pdpbackend.Services.SignatureService;
import com.danone.pdpbackend.entities.Document;
import com.danone.pdpbackend.entities.DocumentSignature;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.SignatureRequestDTO;
import com.danone.pdpbackend.entities.Pdp;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
public class WorkerSignatureControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private WorkerRepo workerRepository;

    @Autowired
    private DocumentRepo documentRepository;

    @Autowired
    private DocumentSignatureRepository documentSignatureRepository;

    @Autowired
    private SignatureService signatureService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Worker testWorker;
    private Document testDocument;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();

        // Create test worker
        testWorker = new Worker();
        testWorker.setNom("Test");
        testWorker.setPrenom("Worker");
        testWorker = workerRepository.save(testWorker);

        // Create test document (assuming you have a concrete Document implementation)
        // You may need to adjust this based on your actual Document implementation
        testDocument = createTestDocument();
        testDocument = documentRepository.save(testDocument);
    }

    @Test
    @DisplayName("Worker can sign a document successfully")
    void testWorkerSignDocument() throws Exception {
        SignatureRequestDTO signatureRequest = createSignatureRequest();

        mockMvc.perform(post("/api/worker/{workerId}/sign", testWorker.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signatureRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Document signed successfully"));

        // Verify signature was created
        Optional<DocumentSignature> signature = documentSignatureRepository.findAll()
                .stream()
                .filter(s -> s.getWorker().getId().equals(testWorker.getId()) &&
                           s.getDocument().getId().equals(testDocument.getId()))
                .findFirst();

        assertTrue(signature.isPresent());
        assertEquals(testWorker.getId(), signature.get().getWorker().getId());
        assertEquals(testDocument.getId(), signature.get().getDocument().getId());
        assertTrue(signature.get().isActive());
    }

    @Test
    @DisplayName("Worker cannot sign document that doesn't exist")
    void testWorkerSignNonExistentDocument() throws Exception {
        SignatureRequestDTO signatureRequest = createSignatureRequest();
        signatureRequest.setDocumentId(999L); // Non-existent document ID

        mockMvc.perform(post("/api/worker/{workerId}/sign", testWorker.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signatureRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Document not found"));
    }

    @Test
    @DisplayName("Non-existent worker cannot sign document")
    void testNonExistentWorkerSignDocument() throws Exception {
        SignatureRequestDTO signatureRequest = createSignatureRequest();

        mockMvc.perform(post("/api/worker/{workerId}/sign", 999L) // Non-existent worker ID
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signatureRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Worker not found"));
    }

    @Test
    @DisplayName("Worker can unsign their own signature")
    void testWorkerUnsignDocument() throws Exception {
        // First, create a signature
        DocumentSignature signature = createTestSignature();
        signature = documentSignatureRepository.save(signature);

        mockMvc.perform(delete("/api/worker/{workerId}/unsign/{signatureId}",
                testWorker.getId(), signature.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Document unsigned successfully"));

        // Verify signature was deleted
        Optional<DocumentSignature> deletedSignature = documentSignatureRepository.findById(signature.getId());
        assertFalse(deletedSignature.isPresent());
    }

    @Test
    @DisplayName("Worker cannot unsign signature that doesn't exist")
    void testWorkerUnsignNonExistentSignature() throws Exception {
        mockMvc.perform(delete("/api/worker/{workerId}/unsign/{signatureId}",
                testWorker.getId(), 999L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Signature not found"));
    }

    @Test
    @DisplayName("Worker cannot unsign another worker's signature")
    void testWorkerCannotUnsignOtherWorkerSignature() throws Exception {
        // Create another worker
        Worker otherWorker = new Worker();
        otherWorker.setNom("Other");
        otherWorker.setPrenom("Worker");
        otherWorker = workerRepository.save(otherWorker);

        // Create signature for other worker
        DocumentSignature signature = createTestSignature();
        signature.setWorker(otherWorker);
        signature = documentSignatureRepository.save(signature);

        mockMvc.perform(delete("/api/worker/{workerId}/unsign/{signatureId}",
                testWorker.getId(), signature.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unauthorized to unsign"));
    }

    @Test
    @DisplayName("Document signing through DocumentController")
    void testDocumentControllerSignDocument() throws Exception {
        SignatureRequestDTO signatureRequest = createSignatureRequest();

        mockMvc.perform(post("/api/document/{documentId}/sign", testDocument.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signatureRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Document signed successfully"));

        // Verify signature was created
        Optional<DocumentSignature> signature = documentSignatureRepository.findAll()
                .stream()
                .filter(s -> s.getWorker().getId().equals(testWorker.getId()) &&
                           s.getDocument().getId().equals(testDocument.getId()))
                .findFirst();

        assertTrue(signature.isPresent());
    }

    @Test
    @DisplayName("Document signing by specific worker through DocumentController")
    void testDocumentControllerSignDocumentByWorker() throws Exception {
        SignatureRequestDTO signatureRequest = createSignatureRequest();
        // Don't set workerId as it will be set from path parameter
        signatureRequest.setWorkerId(null);

        mockMvc.perform(post("/api/document/{documentId}/sign/worker/{workerId}",
                testDocument.getId(), testWorker.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signatureRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Document signed by worker successfully"));

        // Verify signature was created
        Optional<DocumentSignature> signature = documentSignatureRepository.findAll()
                .stream()
                .filter(s -> s.getWorker().getId().equals(testWorker.getId()) &&
                           s.getDocument().getId().equals(testDocument.getId()))
                .findFirst();

        assertTrue(signature.isPresent());
    }

    @Test
    @DisplayName("Document unsigning through DocumentController")
    void testDocumentControllerUnsignDocument() throws Exception {
        // First, create a signature
        DocumentSignature signature = createTestSignature();
        signature = documentSignatureRepository.save(signature);

        mockMvc.perform(delete("/api/document/{documentId}/unsign/{signatureId}",
                testDocument.getId(), signature.getId())
                .param("workerId", testWorker.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Document unsigned successfully"));

        // Verify signature was deleted
        Optional<DocumentSignature> deletedSignature = documentSignatureRepository.findById(signature.getId());
        assertFalse(deletedSignature.isPresent());
    }

    @Test
    @DisplayName("Invalid signature image should fail")
    void testInvalidSignatureImage() throws Exception {
        SignatureRequestDTO signatureRequest = createSignatureRequest();
        signatureRequest.setSignatureImage("invalid-base64");

        mockMvc.perform(post("/api/worker/{workerId}/sign", testWorker.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signatureRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Empty signature image should fail")
    void testEmptySignatureImage() throws Exception {
        SignatureRequestDTO signatureRequest = createSignatureRequest();
        signatureRequest.setSignatureImage("");

        mockMvc.perform(post("/api/worker/{workerId}/sign", testWorker.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signatureRequest)))
                .andExpect(status().isBadRequest());
    }

    // Helper methods
    private SignatureRequestDTO createSignatureRequest() {
        SignatureRequestDTO request = new SignatureRequestDTO();
        request.setWorkerId(testWorker.getId());
        request.setDocumentId(testDocument.getId());
        request.setName("Test");
        request.setLastName("Worker");

        // Create a simple base64 encoded image (1x1 pixel PNG)
        String base64Image = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
        request.setSignatureImage(base64Image);

        return request;
    }

    private DocumentSignature createTestSignature() {
        DocumentSignature signature = new DocumentSignature();
        signature.setWorker(testWorker);
        signature.setDocument(testDocument);
        signature.setSignatureDate(new Date());
        signature.setActive(true);
        // You may need to set signatureVisual if required
        return signature;
    }

    private Document createTestDocument() {
        // Create a concrete Pdp document for testing
        Pdp pdp = new Pdp();
        // Set any required fields for Pdp
        // You may need to adjust these based on your Pdp entity requirements
        return pdp;
    }
}
