package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Controller.auth.AuthenticationRequest;
import com.danone.pdpbackend.Controller.auth.AuthenticationResponse;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.entities.Worker; // Import Worker entity if needed for verification
import com.danone.pdpbackend.entities.dto.EntrepriseDTO;

import com.danone.pdpbackend.entities.dto.WorkerDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rollback DB changes after each test
class EntrepriseControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    // No mocks or repository Autowiring

    private String authToken;
    private Long createdEntrepriseId; // Store ID of entreprise created in setup

    // Helper to POST and get ID from ApiResponse<EntrepriseDTO>
    private Long createEntrepriseViaApi(EntrepriseDTO requestDto) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/entreprise")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk()) // POST returns 200 OK
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse<EntrepriseDTO> apiResponse = objectMapper.readValue(jsonResponse, new TypeReference<ApiResponse<EntrepriseDTO>>() {});
        assertNotNull(apiResponse.getData(), "EntrepriseDTO data should not be null in response");
        assertNotNull(apiResponse.getData().getId(), "Created EntrepriseDTO should have an ID");
        return apiResponse.getData().getId();
    }

    // Helper to POST and get ID from ApiResponse<Worker>
    private Long createWorkerViaApi(WorkerDTO workerDto) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/worker/") // Assuming this endpoint takes Worker entity directly
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workerDto)))
                .andExpect(status().isOk()) // Assuming 200 OK on worker create
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        // Adjust TypeReference if worker endpoint returns a DTO
        ApiResponse<WorkerDTO> apiResponse = objectMapper.readValue(jsonResponse, new TypeReference<ApiResponse<WorkerDTO>>() {});
        assertNotNull(apiResponse.getData(), "Worker data should not be null in response");
        assertNotNull(apiResponse.getData().getId(), "Created Worker should have an ID");
        return apiResponse.getData().getId();
    }


    @BeforeEach
    void setUp() throws Exception {
        // --- Token Fetching ---
        AuthenticationRequest loginRequest = new AuthenticationRequest("Yassin4", "Zaqwe123!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String loginResponseJson = loginResult.getResponse().getContentAsString();
        ApiResponse<AuthenticationResponse> apiResponse = objectMapper.readValue(loginResponseJson, new TypeReference<ApiResponse<AuthenticationResponse>>() {});
        this.authToken = apiResponse.getData().getToken();
        assertNotNull(this.authToken);

        // --- Create a baseline Entreprise via API for tests ---
        EntrepriseDTO entrepriseToCreate = EntrepriseDTO.builder()
                .nom("Setup API EE " + System.nanoTime())
                .description("Created in setup")
                .build();





        // Create entreprise via API
        this.createdEntrepriseId = createEntrepriseViaApi(entrepriseToCreate);
        assertNotNull(this.createdEntrepriseId, "Setup failed to create baseline Entreprise");

        // add worker to entreprise
        WorkerDTO workerDto = new WorkerDTO();
        workerDto.setNom("WorkerA");
        workerDto.setPrenom("Test");
        workerDto.setEntreprise(createdEntrepriseId);

        // Create worker via API
        Long workerId = createWorkerViaApi(workerDto);

    }

    @Test
    void createEntreprise_shouldReturnCreatedEntrepriseDTO() throws Exception {
        // Arrange
        EntrepriseDTO requestDTO = EntrepriseDTO.builder()
                .nom("Create Test EE " + System.nanoTime())
                .description("Testing POST")
                .numTel("11223344")
                .raisonSociale("Test Raison")
                // Set other fields as needed
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/entreprise")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Entreprise created successfully"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.nom").value(requestDTO.getNom()))
                .andExpect(jsonPath("$.data.description").value(requestDTO.getDescription()))
                .andExpect(jsonPath("$.data.numTel").value(requestDTO.getNumTel()));
    }

    @Test
    void fetchAll_shouldReturnListOfEntreprises() throws Exception {
        // Arrange: Baseline entreprise created in setup

        // Act & Assert
        mockMvc.perform(get("/api/entreprise")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Entreprises fetched successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", not(empty()))) // Should contain at least the setup one
                .andExpect(jsonPath("$.data[?(@.id == %d)]", createdEntrepriseId).exists()); // Verify setup entreprise is present
    }

    @Test
    void getEntrepriseById_whenExists_shouldReturnEntrepriseDTO() throws Exception {
        // Arrange: Use ID created in setup
        Long idToGet = createdEntrepriseId;

        // Act & Assert
        mockMvc.perform(get("/api/entreprise/{id}", idToGet)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // The controller returns the DTO directly, not wrapped in ApiResponse for GET by ID
                .andExpect(jsonPath("$.id").value(idToGet))
                .andExpect(jsonPath("$.nom").value(startsWith("Setup API EE"))); // Check name from setup
    }

    @Test
    void getEntrepriseById_whenNotExists_shouldReturnNotFound() throws Exception {
        // Arrange: Use an ID that shouldn't exist
        Long idToGet = 99999L;

        // Act & Assert
        mockMvc.perform(get("/api/entreprise/{id}", idToGet)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Controller returns 404
    }

    @Test
    void updateEntreprise_shouldReturnUpdatedEntrepriseDTO() throws Exception {
        // Arrange: Use ID created in setup
        Long idToUpdate = createdEntrepriseId;
        String updatedName = "Updated EE Name " + System.nanoTime();
        String updatedDesc = "Updated Description";

        EntrepriseDTO requestDTO = EntrepriseDTO.builder()
                .id(idToUpdate) // Include ID if your logic needs it, often PATCH doesn't need ID in body
                .nom(updatedName)
                .description(updatedDesc)
                // Include other fields from original or nullify if needed
                .build();

        // Act & Assert
        mockMvc.perform(patch("/api/entreprise/{id}", idToUpdate)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Entreprise updated successfully"))
                .andExpect(jsonPath("$.data.id").value(idToUpdate))
                .andExpect(jsonPath("$.data.nom").value(updatedName))
                .andExpect(jsonPath("$.data.description").value(updatedDesc));

        // Optional: Follow-up GET to verify persistence
        mockMvc.perform(get("/api/entreprise/{id}", idToUpdate)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value(updatedName));
    }

    @Test
    void deleteEntreprise_whenExists_shouldReturnSuccess() throws Exception {
        // Arrange: Use ID created in setup
        Long idToDelete = createdEntrepriseId;

        // Act & Assert for DELETE
        mockMvc.perform(delete("/api/entreprise/{id}", idToDelete)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
        // Assert: Verify with GET
        mockMvc.perform(get("/api/entreprise/{id}", idToDelete)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound()); // Should be gone
    }

    @Test
    void deleteEntreprise_whenNotExists_shouldReturnBadRequest() throws Exception {
        // Arrange: Use an ID that shouldn't exist
        Long idToDelete = 123123123L;

        // Act & Assert for DELETE
        mockMvc.perform(delete("/api/entreprise/{id}", idToDelete)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getWorkersByEntreprise_shouldReturnWorkers() throws Exception {
        // Arrange
        // Use the entreprise created in setUp
        Long testEntId = createdEntrepriseId;

        // Worker A was already created and associated in setUp

        // Act & Assert
        mockMvc.perform(get("/api/entreprise/{entrepriseId}/workers", testEntId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Workers fetched successfully"))
                .andExpect(jsonPath("$.data").isArray())
                // Verify that the list is not empty (Worker A from setup should be present)
                .andExpect(jsonPath("$.data", not(empty())));
    }

}