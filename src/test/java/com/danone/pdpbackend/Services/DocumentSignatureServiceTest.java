package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.Repo.DocumentRepo;
import com.danone.pdpbackend.Repo.DocumentSignatureRepository;
import com.danone.pdpbackend.Repo.UsersRepo;
import com.danone.pdpbackend.Repo.WorkerRepo;
import com.danone.pdpbackend.entities.Document;
import com.danone.pdpbackend.entities.DocumentSignature;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.User;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.SignatureRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Signature Service Tests")
class DocumentSignatureServiceTest {
    @Mock
    private DocumentRepo documentRepository;

    @Mock
    private WorkerRepo workerRepository;

    @Mock
    private DocumentSignatureRepository documentSignatureRepository;

    @Mock
    private UsersRepo usersRepo;

    @InjectMocks
    private DocumentSignatureService documentSignatureService;

    private Worker testWorker;
    private User testUser;
    private Document testDocument;
    private SignatureRequestDTO validSignatureRequest;

    @BeforeEach
    void setUp() {
        // Setup test worker
        testWorker = new Worker();
        testWorker.setId(1L);
        testWorker.setNom("Doe");
        testWorker.setPrenom("John");

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("jane.manager");
        testUser.setEmail("jane@example.com");

        // Setup test document (using Pdp as concrete implementation)
        testDocument = new Pdp();
        testDocument.setId(1L);
        testDocument.setSignatures(new ArrayList<>());

        // Create valid base64 image (simple 1x1 pixel PNG)
        String validBase64Image = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

        // Setup valid signature request
        validSignatureRequest = new SignatureRequestDTO();
        validSignatureRequest.setWorkerId(1L);
        validSignatureRequest.setUserId(1L);
        validSignatureRequest.setDocumentId(1L);
        validSignatureRequest.setPrenom("John");
        validSignatureRequest.setNom("Doe");
        validSignatureRequest.setSignatureImage(validBase64Image);
    }

    @Test
    @DisplayName("Should successfully sign document with valid data")
    void signDocument_WithValidData_ShouldSucceed() {
        // Arrange
        when(workerRepository.findById(1L)).thenReturn(testWorker);
        when(usersRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);
        
        // Mock DocumentSignatureRepository.save to return a valid signature with ID
        when(documentSignatureRepository.save(any(DocumentSignature.class))).thenAnswer(invocation -> {
            DocumentSignature signature = invocation.getArgument(0);
            signature.setId(1L); // Set an ID to simulate a saved entity
            return signature;
        });

        // Act
        assertDoesNotThrow(() -> documentSignatureService.signDocument(validSignatureRequest));

        // Assert
        verify(workerRepository).findById(1L);
        verify(usersRepo).findById(1L);
        verify(documentRepository).findById(1L);
        verify(documentRepository).save(testDocument);
        assertEquals(1, testDocument.getSignatures().size());

        DocumentSignature signature = testDocument.getSignatures().get(0);
        assertEquals(testWorker, signature.getWorker());
        assertEquals(testUser, signature.getUser());
        assertEquals(testDocument, signature.getDocument());
        assertTrue(signature.isActive());
        assertNotNull(signature.getSignatureDate());
        assertNotNull(signature.getSignatureVisual());
    }

    @Test
    @DisplayName("Should throw exception when worker not found")
    void signDocument_WithNonExistentWorker_ShouldThrowException() {
        // Arrange
        when(workerRepository.findById(1L)).thenReturn(null);
        when(usersRepo.findById(1L)).thenReturn(Optional.of(new User(1L)));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
               IllegalArgumentException.class,
            () -> documentSignatureService.signDocument(validSignatureRequest)
        );
        assertEquals("Worker not found", exception.getMessage());
        verify(workerRepository).findById(1L);
        verifyNoInteractions(documentRepository);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void signDocument_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        when(workerRepository.findById(1L)).thenReturn(testWorker);
        when(usersRepo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> documentSignatureService.signDocument(validSignatureRequest)
        );
        assertEquals("User not found", exception.getMessage());
        verify(workerRepository).findById(1L);
        verify(usersRepo).findById(1L);
        verifyNoInteractions(documentRepository);
    }

    @Test
    @DisplayName("Should throw exception when document not found")
    void signDocument_WithNonExistentDocument_ShouldThrowException() {
        // Arrange
        when(workerRepository.findById(1L)).thenReturn(testWorker);
        when(usersRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> documentSignatureService.signDocument(validSignatureRequest)
        );
        assertEquals("Document not found", exception.getMessage());
        verify(workerRepository).findById(1L);
        verify(usersRepo).findById(1L);
        verify(documentRepository).findById(1L);
        verify(documentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception with invalid base64 signature image")
    void signDocument_WithInvalidBase64Image_ShouldThrowException() {
        // Arrange
        when(workerRepository.findById(1L)).thenReturn(testWorker);
        when(usersRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));

        validSignatureRequest.setSignatureImage("invalid-base64-string");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> documentSignatureService.signDocument(validSignatureRequest)
        );
        assertEquals("Invalid Base64 signature image format", exception.getMessage());
        verify(documentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception with empty signature image")
    void signDocument_WithEmptySignatureImage_ShouldThrowException() {
        // Arrange
        when(workerRepository.findById(1L)).thenReturn(testWorker);
        when(usersRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));

        // Empty base64 string
        validSignatureRequest.setSignatureImage("");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> documentSignatureService.signDocument(validSignatureRequest)
        );
        assertEquals("Invalid signature image", exception.getMessage());
        verify(documentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully unsign document with valid data")
    void unSignDocument_WithValidData_ShouldSucceed() {
        // Arrange
        DocumentSignature signature = new DocumentSignature();
        signature.setId(1L);
        signature.setWorker(testWorker);
        signature.setDocument(testDocument);
        signature.setUser(testUser);
        signature.setSignatureDate(new Date());
        signature.setActive(true);

        when(documentSignatureRepository.findById(1L)).thenReturn(Optional.of(signature));

        // Act
        assertDoesNotThrow(() -> documentSignatureService.unSignDocument(1L, 1L));

        // Assert
        verify(documentSignatureRepository).findById(1L);
        verify(documentSignatureRepository).delete(signature);
    }

    @Test
    @DisplayName("Should throw exception when unsigning non-existent signature")
    void unSignDocument_WithNonExistentSignature_ShouldThrowException() {
        // Arrange
        when(documentSignatureRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> documentSignatureService.unSignDocument(1L, 1L)
        );
        assertEquals("Signature not found", exception.getMessage());
        verify(documentSignatureRepository).findById(1L);
        verify(documentSignatureRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when worker unauthorized to unsign")
    void unSignDocument_WithUnauthorizedWorker_ShouldThrowException() {
        // Arrange
        Worker differentWorker = new Worker();
        differentWorker.setId(2L);

        DocumentSignature signature = new DocumentSignature();
        signature.setId(1L);
        signature.setWorker(differentWorker);
        signature.setDocument(testDocument);

        when(documentSignatureRepository.findById(1L)).thenReturn(Optional.of(signature));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> documentSignatureService.unSignDocument(1L, 1L)
        );
        assertEquals("Unauthorized to unsign", exception.getMessage());
        verify(documentSignatureRepository).findById(1L);
        verify(documentSignatureRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should handle multiple signatures on same document")
    void signDocument_WithMultipleSignatures_ShouldAddToExistingSignatures() {
        // Arrange
        DocumentSignature existingSignature = new DocumentSignature();
        existingSignature.setId(1L);
        testDocument.getSignatures().add(existingSignature);

        when(workerRepository.findById(1L)).thenReturn(testWorker);
        when(usersRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);
        
        // Mock DocumentSignatureRepository.save to return a valid signature with ID
        when(documentSignatureRepository.save(any(DocumentSignature.class))).thenAnswer(invocation -> {
            DocumentSignature signature = invocation.getArgument(0);
            signature.setId(2L); // Set a different ID for the new signature
            return signature;
        });

        // Act
        assertDoesNotThrow(() -> documentSignatureService.signDocument(validSignatureRequest));

        // Assert
        assertEquals(2, testDocument.getSignatures().size());
        verify(documentRepository).save(testDocument);
    }

    @Test
    @DisplayName("Should handle different users signing for same worker")
    void signDocument_WithDifferentUsersSameWorker_ShouldSucceed() {
        // Arrange
        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setUsername("another.manager");
        differentUser.setEmail("another@example.com");

        when(workerRepository.findById(1L)).thenReturn(testWorker);
        when(usersRepo.findById(2L)).thenReturn(Optional.of(differentUser));
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);
        
        // Mock DocumentSignatureRepository.save to return a valid signature with ID
        when(documentSignatureRepository.save(any(DocumentSignature.class))).thenAnswer(invocation -> {
            DocumentSignature signature = invocation.getArgument(0);
            signature.setId(1L); // Set an ID to simulate a saved entity
            return signature;
        });

        validSignatureRequest.setUserId(2L);

        // Act
        assertDoesNotThrow(() -> documentSignatureService.signDocument(validSignatureRequest));

        // Assert
        DocumentSignature signature = testDocument.getSignatures().get(0);
        assertEquals(testWorker, signature.getWorker());
        assertEquals(differentUser, signature.getUser());
        verify(documentRepository).save(testDocument);
    }

    @Test
    @DisplayName("Should return the right amount of worker who signed a document")
    void getWorkersByDocumentId_ShouldReturnCorrectWorkers() {
        // Arrange
        Long documentId = 1L;

        // Create multiple test workers
        Worker worker1 = new Worker();
        worker1.setId(1L);
        worker1.setNom("Doe");
        worker1.setPrenom("John");

        Worker worker2 = new Worker();
        worker2.setId(2L);
        worker2.setNom("Smith");
        worker2.setPrenom("Jane");

        Worker worker3 = new Worker();
        worker3.setId(3L);
        worker3.setNom("Brown");
        worker3.setPrenom("Bob");

        List<Worker> expectedWorkers = List.of(worker1, worker2, worker3);

        // Mock the repository to return the expected workers
        when(documentSignatureRepository.findWorkersByDocumentId(documentId)).thenReturn(expectedWorkers);

        // Act
        List<Worker> actualWorkers = documentSignatureService.getSignedWorkersByDocument(documentId);

        // Assert
        assertNotNull(actualWorkers);
        assertEquals(3, actualWorkers.size());
        assertEquals(expectedWorkers.size(), actualWorkers.size());
        assertTrue(actualWorkers.contains(worker1));
        assertTrue(actualWorkers.contains(worker2));
        assertTrue(actualWorkers.contains(worker3));

        // Verify the repository method was called with correct parameter
        verify(documentSignatureRepository).findWorkersByDocumentId(documentId);
    }

    @Test
    @DisplayName("Should return empty list when no workers signed the document")
    void getWorkersByDocumentId_WithNoSignatures_ShouldReturnEmptyList() {
        // Arrange
        Long documentId = 1L;
        List<Worker> emptyWorkerList = new ArrayList<>();

        when(documentSignatureRepository.findWorkersByDocumentId(documentId)).thenReturn(emptyWorkerList);

        // Act
        List<Worker> actualWorkers = documentSignatureService.getSignedWorkersByDocument(documentId);

        // Assert
        assertNotNull(actualWorkers);
        assertEquals(0, actualWorkers.size());
        assertTrue(actualWorkers.isEmpty());

        verify(documentSignatureRepository).findWorkersByDocumentId(documentId);
    }

    @Test
    @DisplayName("Should throw exception when document ID is null")
    void getWorkersByDocumentId_WithNullDocumentId_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> documentSignatureService.getSignedWorkersByDocument(null)
        );

        assertEquals("Document ID cannot be null", exception.getMessage());

        // Verify repository method was never called
        verifyNoInteractions(documentSignatureRepository);
    }
}
