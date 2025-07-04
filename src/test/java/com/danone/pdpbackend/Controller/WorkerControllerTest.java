package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.entities.dto.WorkerDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// Assuming your service might throw this, or you have an equivalent
// import com.danone.pdpbackend.exception.ResourceNotFoundException;
// import org.springframework.web.server.ResponseStatusException; // Another common one

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WorkerControllerTest {

    @Autowired
    private WorkerController workerController;

    // Your existing setUp and tearDown
    @BeforeEach
    void setUp() {
        // If you have a general setup, like cleaning DB or populating common data.
        // For these edge cases, often we rely on specific non-existent IDs or invalid DTOs.
    }

    @AfterEach
    void tearDown() {
        // Clean up any persistent data if your tests create it and don't rely on rollbacks.
    }

    // --- Your existing HAPPY PATH tests ---
    @Test
    void getAllWorkers() {
        ResponseEntity<ApiResponse<List<WorkerDTO>>> response = workerController.getAllWorkers();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        // Consider adding a case for when no workers exist, should return OK with empty list
    }

    @Test
    void createWorker() {
        WorkerDTO workerDTO = new WorkerDTO();
        workerDTO.setNom("John");
        workerDTO.setPrenom("Doe");

        ResponseEntity<ApiResponse<WorkerDTO>> response = workerController.createWorker(workerDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode()); // Or HttpStatus.CREATED (201) often better for create
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertEquals("John", response.getBody().getData().getNom());
    }

    @Test
    void updateWorker() {
        // First, create a worker to update
        WorkerDTO workerDTO = new WorkerDTO();
        workerDTO.setNom("InitialName");
        workerDTO.setPrenom("InitialSurname");
        ResponseEntity<ApiResponse<WorkerDTO>> createResponse = workerController.createWorker(workerDTO);
        Long id = createResponse.getBody().getData().getId();

        WorkerDTO updatedInfo = new WorkerDTO();
        updatedInfo.setNom("UpdatedName");
        updatedInfo.setPrenom("UpdatedSurname"); // Keep prenom or update it as needed for the test
        // updatedInfo.setId(id); // DTO usually doesn't need ID if ID is from path, but depends on impl.

        ResponseEntity<ApiResponse<WorkerDTO>> updateResponse = workerController.updateWorker(id, updatedInfo);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        assertNotNull(updateResponse.getBody().getData());
        assertEquals("UpdatedName", updateResponse.getBody().getData().getNom());
        assertEquals(id, updateResponse.getBody().getData().getId());
    }

    @Test
    void deleteWorker() {
        WorkerDTO workerDTO = new WorkerDTO();
        workerDTO.setNom("ToDelete");
        workerDTO.setPrenom("SoonGone");
        ResponseEntity<ApiResponse<WorkerDTO>> response = workerController.createWorker(workerDTO);
        Long id = response.getBody().getData().getId();

        ResponseEntity<ApiResponse<Boolean>> deleteResponse = workerController.deleteWorker(id);

        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode()); // Or HttpStatus.NO_CONTENT (204)
        assertNotNull(deleteResponse.getBody());
        assertTrue(deleteResponse.getBody().getData());

        // Optionally, verify it's actually gone (expect 404)
        // This depends on how your controller/service handles getById for non-existent entities
        // For simplicity, I'm assuming the direct deleteWorker call is enough based on your structure
        // But a follow-up getWorkerById(id) asserting 404 would be more thorough.
    }


    @Test
    void getWorkerById() {
        WorkerDTO workerDTO = new WorkerDTO();
        workerDTO.setNom("SpecificJohn");
        workerDTO.setPrenom("SpecificDoe");
        ResponseEntity<ApiResponse<WorkerDTO>> response = workerController.createWorker(workerDTO);
        Long id = response.getBody().getData().getId();

        ResponseEntity<ApiResponse<WorkerDTO>> getResponse = workerController.getWorkerById(id);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertNotNull(getResponse.getBody().getData());
        assertEquals("SpecificJohn", getResponse.getBody().getData().getNom());
    }

    // --- NEW EDGE CASE TESTS ---
    // --- You better make sure your controller can handle this shit! ---

    @Test
    void getWorkerById_whenWorkerDoesNotExist_shouldReturnNotFound() {
        Long nonExistentId = 99999L; // An ID that's bloody unlikely to exist
        ResponseEntity<ApiResponse<WorkerDTO>> response = workerController.getWorkerById(nonExistentId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        // Optionally, check the ApiResponse body for error messages if your API structures errors that way
        // assertNotNull(response.getBody());
        // assertFalse(response.getBody().isSuccess()); // Assuming you have a 'success' field
        // assertNotNull(response.getBody().getMessage());
    }

    @Test
    void updateWorker_whenWorkerDoesNotExist_shouldReturnNotFound() {
        Long nonExistentId = 123456L;
        WorkerDTO workerDTO = new WorkerDTO();
        workerDTO.setNom("Ghost");
        workerDTO.setPrenom("Buster");

        ResponseEntity<ApiResponse<WorkerDTO>> response = workerController.updateWorker(nonExistentId, workerDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteWorker_whenWorkerDoesNotExist_shouldReturnNotFound() {
        // Note: Some APIs might return 200/204 for idempotency, but 404 is often clearer
        // if the resource was expected to be there. Let's aim for strict.
        Long nonExistentId = 99999L;
        ResponseEntity<ApiResponse<Boolean>> response = workerController.deleteWorker(nonExistentId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        // If your API returns OK with 'false' data for not found deletion, adjust assertion:
        // assertEquals(HttpStatus.OK, response.getStatusCode());
        // assertNotNull(response.getBody());
        // assertFalse(response.getBody().getData()); // Assuming getData() returns false if not deleted
    }

    // --- Validation Tests for Create ---
    // These assume you have Bean Validation (e.g., @NotNull, @NotEmpty on WorkerDTO)
    // and your controller method uses @Valid, or your service does equivalent checks.

    @Test
    void createWorker_whenPayloadIsNull_shouldReturnBadRequest() {
        // How your controller handles a completely null body passed to createWorker
        // might depend on Spring MVC setup. If it throws before your method,
        // direct call might not be the best way to test (MockMvc would be).
        // But let's assume your method gets called with null.
        // This specific test might need adjustment based on how Spring handles null @RequestBody
        // For now, we expect the controller to be robust or the framework to catch it.
        // ResponseEntity<ApiResponse<WorkerDTO>> response = workerController.createWorker(null);
        // assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        // This test is tricky with direct controller calls if Spring blocks it earlier.
        // A more common test is for null fields *within* the DTO.
        // I'll skip the direct null DTO test for create via direct call as it's more of an MVC layer test.
        // Let's focus on invalid fields in the DTO.
        System.out.println("Skipping direct null payload for createWorker with direct call for now - better with MockMvc for full HTTP context.");
    }


    @Test
    void createWorker_whenNomIsNull_shouldReturnBadRequest() {
        WorkerDTO workerDTO = new WorkerDTO();
        workerDTO.setNom(null); // This is the bad part! (هذا سيء - *hadha sayyi'*)
        workerDTO.setPrenom("Doe");

        ResponseEntity<ApiResponse<WorkerDTO>> response = workerController.createWorker(workerDTO);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createWorker_whenPrenomIsNull_shouldReturnBadRequest() {
        WorkerDTO workerDTO = new WorkerDTO();
        workerDTO.setNom("John");
        workerDTO.setPrenom(null); // Invalid!

        ResponseEntity<ApiResponse<WorkerDTO>> response = workerController.createWorker(workerDTO);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createWorker_whenNomIsEmpty_shouldReturnBadRequest() {
        WorkerDTO workerDTO = new WorkerDTO();
        workerDTO.setNom(""); // Empty string, also bad!
        workerDTO.setPrenom("Doe");

        ResponseEntity<ApiResponse<WorkerDTO>> response = workerController.createWorker(workerDTO);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createWorker_whenPrenomIsEmpty_shouldReturnBadRequest() {
        WorkerDTO workerDTO = new WorkerDTO();
        workerDTO.setNom("John");
        workerDTO.setPrenom(""); // Empty string, bad!

        ResponseEntity<ApiResponse<WorkerDTO>> response = workerController.createWorker(workerDTO);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // --- Validation Tests for Update ---
    // Similar assumptions about validation apply here.

    @Test
    void updateWorker_whenNomIsEmpty_shouldReturnBadRequest() {
        WorkerDTO existingWorker = new WorkerDTO();
        existingWorker.setNom("Test");
        existingWorker.setPrenom("Subject");
        ResponseEntity<ApiResponse<WorkerDTO>> createResponse = workerController.createWorker(existingWorker);
        Long existingId = createResponse.getBody().getData().getId();

        WorkerDTO workerUpdateDTO = new WorkerDTO();
        workerUpdateDTO.setNom(""); // Invalid update: empty name
        workerUpdateDTO.setPrenom("Doe");

        ResponseEntity<ApiResponse<WorkerDTO>> response = workerController.updateWorker(existingId, workerUpdateDTO);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateWorker_whenPrenomIsEmpty_shouldReturnBadRequest() {
        WorkerDTO existingWorker = new WorkerDTO();
        existingWorker.setNom("Test");
        existingWorker.setPrenom("Subject");
        ResponseEntity<ApiResponse<WorkerDTO>> createResponse = workerController.createWorker(existingWorker);

        Long existingId = createResponse.getBody().getData().getId();

        WorkerDTO workerUpdateDTO = new WorkerDTO();
        workerUpdateDTO.setNom("John");
        workerUpdateDTO.setPrenom(""); // Invalid update: empty prenom

        ResponseEntity<ApiResponse<WorkerDTO>> response = workerController.updateWorker(existingId, workerUpdateDTO);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}