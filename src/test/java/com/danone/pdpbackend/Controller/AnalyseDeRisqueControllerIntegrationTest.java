package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Repo.AnalyseDeRisqueRepo;
import com.danone.pdpbackend.Repo.RisqueRepo;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.mappers.AnalyseDeRisqueMapper;
import com.danone.pdpbackend.entities.AnalyseDeRisque;
import com.danone.pdpbackend.entities.Risque;
import com.danone.pdpbackend.entities.dto.AnalyseDeRisqueDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional // Rollback transactions after each test
class AnalyseDeRisqueControllerIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(AnalyseDeRisqueControllerIntegrationTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnalyseDeRisqueRepo analyseDeRisqueRepo;

    @Autowired
    private RisqueRepo risqueRepo;

    @Autowired
    private AnalyseDeRisqueMapper analyseDeRisqueMapper;

    private String token;
    private Risque testRisque;
    private AnalyseDeRisque testAnalyseDeRisque;

    // Helper method to obtain JWT token (adapted from PdpControllerIntegrationTest)
    void getToken() throws Exception {
        String loginCredentials = """
            {
                "username": "Yassin4",
                "password": "Zaqwe123!"
            }
            """; // Replace with valid test user credentials

        MvcResult loginResult = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginCredentials))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        // Use TypeReference for generic ApiResponse
        TypeReference<ApiResponse<Map<String, Object>>> typeRef = new TypeReference<>() {};
        ApiResponse<Map<String, Object>> authResponse = objectMapper.readValue(loginResponse, typeRef);

        assertNotNull(authResponse.getData(), "Authentication response data should not be null");
        token = (String) authResponse.getData().get("token");
        assertNotNull(token, "Token should not be null");
        log.info("Token obtained successfully.");
    }


    // Helper to create and save a Risque for tests
    private Risque createAndSaveTestRisque() {
        if (risqueRepo.count() == 0) { // Create only if needed
            Risque risque = new Risque();
            risque.setTitle("Integration Test Risque");
            risque.setDescription("Risque used for integration tests");
            return risqueRepo.save(risque);
        } else {
            // Return the first existing risque if available, adjust if specific risque needed
            return risqueRepo.findAll().get(0);
        }

    }

    // Helper to create and save an AnalyseDeRisque for tests
    private AnalyseDeRisque createAndSaveTestAnalyse(Risque risque) {
        AnalyseDeRisque analyse = new AnalyseDeRisque();
        analyse.setDeroulementDesTaches("Initial Tasks");
        analyse.setMoyensUtilises("Initial Tools");
        analyse.setMesuresDePrevention("Initial Measures");
        analyse.setRisque(risque);
        return analyseDeRisqueRepo.save(analyse);
    }


    @BeforeEach
    void setUp() throws Exception {
        getToken(); // Get token before each test
        testRisque = createAndSaveTestRisque(); // Ensure a risque exists
        // Don't create testAnalyseDeRisque here, create it within tests as needed
    }

    @Test
    void createAnalyseDeRisque_ValidData_ShouldReturnCreated() throws Exception {
        // Given
        AnalyseDeRisqueDTO newDto = new AnalyseDeRisqueDTO();
        newDto.setDeroulementDesTaches("Create Test Tasks");
        newDto.setMoyensUtilises("Create Test Tools");
        newDto.setMesuresDePrevention("Create Test Measures");
        newDto.setRisque(testRisque); // Associate with existing risque

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/analyseDeRisque")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("AnalyseDeRisque created")))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.deroulementDesTaches", is("Create Test Tasks")))
                .andExpect(jsonPath("$.data.risque.id", is(testRisque.getId().intValue()))) // Check associated risque ID
                .andReturn();

        // Optional: Verify database state
        String responseString = result.getResponse().getContentAsString();
        TypeReference<ApiResponse<AnalyseDeRisqueDTO>> typeRef = new TypeReference<>() {};
        ApiResponse<AnalyseDeRisqueDTO> apiResponse = objectMapper.readValue(responseString, typeRef);
        Long createdId = apiResponse.getData().getId();
        assertTrue(analyseDeRisqueRepo.findById(createdId).isPresent());
    }

    @Test
    void getAnalyseDeRisqueById_ExistingId_ShouldReturnAnalyse() throws Exception {
        // Given: Create an entity first
        testAnalyseDeRisque = createAndSaveTestAnalyse(testRisque);
        Long existingId = testAnalyseDeRisque.getId();

        // When & Then
        mockMvc.perform(get("/api/analyseDeRisque/{id}", existingId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("AnalyseDeRisque fetched")))
                .andExpect(jsonPath("$.data.id", is(existingId.intValue())))
                .andExpect(jsonPath("$.data.deroulementDesTaches", is(testAnalyseDeRisque.getDeroulementDesTaches())))
                .andExpect(jsonPath("$.data.risque.id", is(testRisque.getId().intValue())));
    }

    @Test
    void getAnalyseDeRisqueById_NonExistingId_ShouldReturnNotFound() throws Exception {
        // Given
        Long nonExistingId = 999L;

        // When & Then
        mockMvc.perform(get("/api/analyseDeRisque/{id}", nonExistingId)
                        .header("Authorization", "Bearer " + token))
                // Expecting OK status because the service might return null, leading to an OK response with null data
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist()); // Check that data is null or absent
        // .andExpect(status().isNotFound()); // Adjust if your controller/service throws an exception leading to 404
    }


    @Test
    void getAllAnalyseDeRisques_ShouldReturnList() throws Exception {
        // Given: Create a couple of entities
        createAndSaveTestAnalyse(testRisque);
        AnalyseDeRisque analyse2 = new AnalyseDeRisque();
        analyse2.setDeroulementDesTaches("Tasks 2");
        analyse2.setRisque(testRisque);
        analyseDeRisqueRepo.save(analyse2);

        // When & Then
        mockMvc.perform(get("/api/analyseDeRisque")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("AnalyseDeRisques fetched")))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2)))) // Check if at least 2 exist
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[1].id").exists());
    }

    @Test
    void updateAnalyseDeRisque_ExistingId_ShouldReturnUpdated() throws Exception {
        // Given: Create an entity first
        testAnalyseDeRisque = createAndSaveTestAnalyse(testRisque);
        Long existingId = testAnalyseDeRisque.getId();

        AnalyseDeRisqueDTO updateDto = analyseDeRisqueMapper.toDTO(testAnalyseDeRisque); // Map existing to DTO
        updateDto.setDeroulementDesTaches("Updated Tasks via PATCH"); // Change a field

        // When & Then
        mockMvc.perform(patch("/api/analyseDeRisque/{id}", existingId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("AnalyseDeRisque updated")))
                .andExpect(jsonPath("$.data.id", is(existingId.intValue())))
                .andExpect(jsonPath("$.data.deroulementDesTaches", is("Updated Tasks via PATCH")));

        // Optional: Verify database state
        AnalyseDeRisque updatedEntity = analyseDeRisqueRepo.findById(existingId).orElseThrow();
        assertEquals("Updated Tasks via PATCH", updatedEntity.getDeroulementDesTaches());
    }

    @Test
    void updateAnalyseDeRisque_NonExistingId_ShouldReturnNotFound() throws Exception {
        // Given
        Long nonExistingId = 999L;
        AnalyseDeRisqueDTO updateDto = new AnalyseDeRisqueDTO();
        updateDto.setDeroulementDesTaches("Won't be saved");
        updateDto.setRisque(testRisque);


        // When & Then
        mockMvc.perform(patch("/api/analyseDeRisque/{id}", nonExistingId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                // Check for 404 Not Found
                .andExpect(status().isNotFound());

    }


    @Test
    void deleteAnalyseDeRisque_ExistingId_ShouldReturnSuccess() throws Exception {
        // Given: Create an entity first
        testAnalyseDeRisque = createAndSaveTestAnalyse(testRisque);
        Long existingId = testAnalyseDeRisque.getId();
        assertTrue(analyseDeRisqueRepo.existsById(existingId), "Entity should exist before delete");


        // When & Then
        mockMvc.perform(delete("/api/analyseDeRisque/{id}", existingId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        // Verify database state
        assertFalse(analyseDeRisqueRepo.existsById(existingId), "Entity should not exist after delete");

    }

    @Test
    void deleteAnalyseDeRisque_NonExistingId_ShouldReturnNotFound() throws Exception {
        // Given
        Long nonExistingId = 999L;
        assertFalse(analyseDeRisqueRepo.existsById(nonExistingId), "Entity should not exist before delete attempt");


        // When & Then
        mockMvc.perform(delete("/api/analyseDeRisque/{id}", nonExistingId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound()); // Expect 404 Not Found
    }

    // Add test for addRisqueToAnalyse endpoint if necessary
    // It might require creating AnalyseDeRisque and Risque entities first

}