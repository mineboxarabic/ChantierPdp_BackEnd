package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Controller.auth.AuthenticationRequest;
import com.danone.pdpbackend.Controller.auth.AuthenticationResponse;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.ChantierStatus;
import com.danone.pdpbackend.entities.dto.ChantierDTO;
import com.danone.pdpbackend.entities.dto.EntrepriseDTO;
import com.danone.pdpbackend.entities.dto.WorkerDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Chantier Controller Integration Tests")
class ChantierControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private ChantierDTO testChantier;

    private HelperMethods helperMethods;

    @BeforeAll
    void setup() throws Exception {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        helperMethods = new HelperMethods(mockMvc);
    }

    @BeforeEach
    void setupEach() throws Exception {
        // Get authentication token
        authToken = authenticate("Yassin4", "Zaqwe123!");

        // Create a test chantier for subsequent tests
        testChantier = createChantier(buildChantierDTO(
                "Test Chantier",
                "Test Operation",
                100,
                false
        ));
    }

    @Test
    @DisplayName("Create chantier - should return created chantier with correct status")
    void createChantier_ShouldReturnCreatedChantierWithCorrectStatus() throws Exception {
        // Arrange - Create DTO with standard values
        ChantierDTO newChantierDTO = buildChantierDTO(
                "New Test Chantier",
                "New Operation",
                50,
                false
        );
        //Date tomorow
        Date tomorrow = new Date();
        tomorrow.setTime(tomorrow.getTime() + 24 * 60 * 60 * 1000); // Add one day
        newChantierDTO.setDateDebut(tomorrow);
        // Act - Send request to create chantier
        ChantierDTO createdChantier = createChantier(newChantierDTO);

        // Assert - Verify correct data in response
        assertNotNull(createdChantier.getId(), "Created chantier should have an ID");
        assertEquals("New Test Chantier", createdChantier.getNom());
        assertEquals("New Operation", createdChantier.getOperation());
        assertEquals(50, createdChantier.getNbHeurs());
        assertEquals(ChantierStatus.INACTIVE_TODAY, createdChantier.getStatus(),
                "A chantier with < 400 hours and no dangerous work should have PLANIFIED status");

        // Verify persistence - Get the chantier to ensure it was saved
        ChantierDTO retrievedChantier = getChantierById(createdChantier.getId());
        assertEquals(createdChantier.getId(), retrievedChantier.getId());
        assertEquals(createdChantier.getNom(), retrievedChantier.getNom());
        assertEquals(createdChantier.getStatus(), retrievedChantier.getStatus());
    }

    @Test
    @DisplayName("Create chantier with high hours - should require PDP")
    void createChantierWithHighHours_ShouldRequirePdp() throws Exception {
        // Arrange - Create DTO with high hours
        ChantierDTO newChantierDTO = buildChantierDTO(
                "High Hours Chantier",
                "High Hours Operation",
                450, // > 400 hours
                false
        );

        // Act - Send request to create chantier
        ChantierDTO createdChantier = createChantier(newChantierDTO);

        // Assert - Verify status reflects need for PDP
        assertEquals(ChantierStatus.PENDING_PDP, createdChantier.getStatus(),
                "A chantier with > 400 hours should have PENDING_PDP status");

        // Verify via the requires-pdp endpoint
        Boolean requiresPdp = getRequiresPdp(createdChantier.getId());
        assertTrue(requiresPdp, "Chantier with > 400 hours should require PDP");
    }

    @Test
    @DisplayName("Create dangerous chantier - should require PDP")
    void createDangerousChantier_ShouldRequirePdp() throws Exception {
        // Arrange - Create DTO with dangerous work
        ChantierDTO newChantierDTO = buildChantierDTO(
                "Dangerous Chantier",
                "Dangerous Operation",
                100, // < 400 hours
                true  // Dangerous work
        );

        // Act - Send request to create chantier
        ChantierDTO createdChantier = createChantier(newChantierDTO);

        // Assert - Verify status reflects need for PDP
        assertEquals(ChantierStatus.PENDING_PDP, createdChantier.getStatus(),
                "A dangerous chantier should have PENDING_PDP status");

        // Verify via the requires-pdp endpoint
        Boolean requiresPdp = getRequiresPdp(createdChantier.getId());
        assertTrue(requiresPdp, "Dangerous chantier should require PDP");
    }

    @Test
    @DisplayName("Get all chantiers - should return list including test chantier")
    void getAllChantiers_ShouldReturnListIncludingTestChantier() throws Exception {
        // Act - Get all chantiers
        List<ChantierDTO> chantiers = getAllChantiers();

        // Assert - Verify the list contains our test chantier
        assertTrue(chantiers.size() > 0, "Should return at least one chantier");
        assertTrue(
                chantiers.stream()
                        .anyMatch(c -> c.getId().equals(testChantier.getId())),
                "Result should contain the test chantier"
        );
    }

    @Test
    @DisplayName("Get chantier by ID - should return correct chantier")
    void getChantierById_ShouldReturnCorrectChantier() throws Exception {
        // Act - Get chantier by ID
        ChantierDTO retrievedChantier = getChantierById(testChantier.getId());

        // Assert - Verify correct chantier is returned
        assertEquals(testChantier.getId(), retrievedChantier.getId());
        assertEquals(testChantier.getNom(), retrievedChantier.getNom());
        assertEquals(testChantier.getOperation(), retrievedChantier.getOperation());
        assertEquals(testChantier.getStatus(), retrievedChantier.getStatus());
    }

    @Test
    @DisplayName("Get non-existent chantier - should return 404")
    void getNonExistentChantier_ShouldReturn404() throws Exception {
        // Arrange - Use a non-existent ID
        long nonExistentId = 999999L;

        // Act & Assert - Verify 404 response
        mockMvc.perform(get("/api/chantier/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Chantier not found")));
    }

    @Test
    @DisplayName("Update chantier - should update correctly")
    void updateChantier_ShouldUpdateCorrectly() throws Exception {
        // Arrange - Create update DTO
        ChantierDTO updateRequest = new ChantierDTO();
        updateRequest.setNom("Updated Chantier Name");
        updateRequest.setOperation("Updated Operation");
        updateRequest.setNbHeurs(200);

        // Act - Send update request
        ChantierDTO updatedChantier = updateChantier(testChantier.getId(), updateRequest);

        // Assert - Verify fields are updated
        assertEquals(testChantier.getId(), updatedChantier.getId(), "ID should not change");
        assertEquals("Updated Chantier Name", updatedChantier.getNom(), "Name should be updated");
        assertEquals("Updated Operation", updatedChantier.getOperation(), "Operation should be updated");
        assertEquals(200, updatedChantier.getNbHeurs(), "Hours should be updated");

        // Verify persistence with a separate request
        ChantierDTO retrievedChantier = getChantierById(testChantier.getId());
        assertEquals("Updated Chantier Name", retrievedChantier.getNom());
        assertEquals(200, retrievedChantier.getNbHeurs());
    }

    @Test
    @DisplayName("Update chantier to require PDP - should update status")
    void updateChantierToRequirePdp_ShouldUpdateStatus() throws Exception {
        // Arrange - Create update DTO with high hours
        ChantierDTO updateRequest = new ChantierDTO();
        updateRequest.setNbHeurs(450); // > 400 hours requires PDP

        // Act - Send update request
        ChantierDTO updatedChantier = updateChantier(testChantier.getId(), updateRequest);

        // Assert - Verify status is updated to reflect PDP requirement
        assertEquals(ChantierStatus.PENDING_PDP, updatedChantier.getStatus(),
                "Status should change to PENDING_PDP when hours > 400");

        // Verify via a separate request
        Boolean requiresPdp = getRequiresPdp(testChantier.getId());
        assertTrue(requiresPdp, "Updated chantier should require PDP");
    }

    @Test
    @DisplayName("Delete chantier - should delete successfully")
    void deleteChantier_ShouldDeleteSuccessfully() throws Exception {
        // Arrange - Create a chantier to delete
        ChantierDTO chantierToDelete = createChantier(buildChantierDTO(
                "Chantier To Delete",
                "Delete Operation",
                100,
                false
        ));

        // Act - Delete the chantier
        deleteChantier(chantierToDelete.getId());

        // Assert - Verify chantier is gone (should return 404)
        mockMvc.perform(get("/api/chantier/{id}", chantierToDelete.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete non-existent chantier - should return bad request")
    void deleteNonExistentChantier_ShouldReturnBadRequest() throws Exception {
        // Arrange - Use a non-existent ID
        long nonExistentId = 999999L;

        // Act & Assert - Verify bad request response
        mockMvc.perform(delete("/api/chantier/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get chantier stats - should return valid stats")
    void getChantierStats_ShouldReturnValidStats() throws Exception {
        // Act - Get chantier stats
        mockMvc.perform(get("/api/chantier/{id}/stats", testChantier.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Chantier stats fetched successfully")))
                .andExpect(jsonPath("$.data.chantierId", is(testChantier.getId().intValue())))
                .andExpect(jsonPath("$.data.chantierStatus", is(testChantier.getStatus().name())));
    }

    @Test
    @DisplayName("Get recent chantiers - should return non-empty list")
    void getRecentChantiers_ShouldReturnNonEmptyList() throws Exception {
        // Create a few more chantiers to ensure there are recent ones
        createChantier(buildChantierDTO("Recent Chantier 1", "Recent Op 1", 100, false));
        createChantier(buildChantierDTO("Recent Chantier 2", "Recent Op 2", 200, false));

        // Act & Assert - Get recent chantiers
        mockMvc.perform(get("/api/chantier/recent")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Recent chantiers fetched successfully")))
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Get last chantier ID - should return valid ID")
    void getLastChantierId_ShouldReturnValidId() throws Exception {
        // Create a chantier to ensure we have a "last" one
        ChantierDTO lastChantier = createChantier(buildChantierDTO(
                "Last ID Chantier",
                "Last ID Operation",
                100,
                false
        ));

        // Act - Get last ID
        Long lastId = getLastChantierId();

        // Assert - Verify ID is at least as large as our last created chantier
        assertTrue(lastId >= lastChantier.getId(),
                "Last ID should be >= our last created chantier ID");
    }





    @Test
    @DisplayName("Get all workers that are linked to a chantier - should return non-empty list")
    void getAllWorkersByChantier_ShouldReturnNonEmptyList() throws Exception {
        // Arrange - Create a chantier with workers
        ChantierDTO chantier = createChantier(buildChantierDTO(
                "Workers Chantier",
                "Workers Operation",
                100,
                false
        ));


        EntrepriseDTO entreprise1 = helperMethods.createEntrepriseAPI("EntrepriseTest");
        EntrepriseDTO entreprise2 = helperMethods.createEntrepriseAPI("EntrepriseTest2");


        List<WorkerDTO> listEntreprise1 = helperMethods.createWorkers(5, entreprise1.getId());
        List<WorkerDTO> listEntreprise2 = helperMethods.createWorkers(3, entreprise2.getId());

        entreprise1.setWorkers(listEntreprise1.stream().map(WorkerDTO::getId).toList()); // Create 5 workers
        entreprise2.setWorkers(listEntreprise2.stream().map(WorkerDTO::getId).toList()); // Create 5 workers


        chantier.setEntrepriseExterieurs(new ArrayList<>(List.of(entreprise1.getId(), entreprise2.getId())));

        ChantierDTO chantierWithWorkers = updateChantier(chantier.getId(), chantier);



        // Act - Get workers by chantier ID
        MvcResult res = mockMvc.perform(get("/api/chantier/{id}/workers", chantierWithWorkers.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Workers fetched successfully"))).andReturn();

        ApiResponse<List<WorkerDTO>> response = parseResponse(res, new TypeReference<ApiResponse<List<WorkerDTO>>>() {});
        List<WorkerDTO> workers = response.getData();
        // Assert - Verify the list contains workers
        assertNotNull(workers, "Should return a non-null list of workers");
        assertFalse(workers.isEmpty(), "Should return a non-empty list of workers");
        assertTrue(workers.size() >= 8, "Should return at least 8 workers");
        assertTrue(workers.stream().anyMatch(w -> w.getId().equals(listEntreprise1.get(0).getId())), "Should contain workers from entreprise 1");
        assertTrue(workers.stream().anyMatch(w -> w.getId().equals(listEntreprise2.get(0).getId())), "Should contain workers from entreprise 2");
    }




    // ==================== Helper Methods ====================



    /**
     * Authenticates with the API and returns the JWT token
     */
    private String authenticate(String username, String password) throws Exception {
        AuthenticationRequest request = new AuthenticationRequest(username, password);

        MvcResult result = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<AuthenticationResponse> response = parseResponse(
                result, new TypeReference<ApiResponse<AuthenticationResponse>>() {}
        );

        return response.getData().getToken();
    }

    /**
     * Builds a ChantierDTO with the given parameters and default dates
     */
    private ChantierDTO buildChantierDTO(String nom, String operation, int nbHeurs, boolean travauxDangereux) {
        ChantierDTO dto = new ChantierDTO();
        dto.setNom(nom);
        dto.setOperation(operation);
        dto.setNbHeurs(nbHeurs);
        dto.setTravauxDangereux(travauxDangereux);

        // Set dates
        Date now = new Date();
        dto.setDateDebut(now);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 1);
        dto.setDateFin(calendar.getTime());

        return dto;
    }

    /**
     * Creates a chantier via API
     */
    private ChantierDTO createChantier(ChantierDTO chantierDTO) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/chantier/")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chantierDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Chantier saved successfully")))
                .andReturn();

        return parseResponseData(result, ChantierDTO.class);
    }

    /**
     * Gets a chantier by ID via API
     */
    private ChantierDTO getChantierById(Long id) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/chantier/{id}", id)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        return parseResponseData(result, ChantierDTO.class);
    }

    /**
     * Gets all chantiers via API
     */
    private List<ChantierDTO> getAllChantiers() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/chantier/all")
                        .header("Authorization", "Bearer " + authToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.message", is("Chantiers fetched successfully")))
                        .andReturn();

        return parseResponseDataList(result, ChantierDTO.class);
    }

    /**
     * Updates a chantier via API
     */
    private ChantierDTO updateChantier(Long id, ChantierDTO updateDTO) throws Exception {
        // Important: Don't set ID in the DTO to avoid JPA issues
        updateDTO.setId(null);

        MvcResult result = mockMvc.perform(patch("/api/chantier/{id}", id)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Chantier updated successfully")))
                .andReturn();

        return parseResponseData(result, ChantierDTO.class);
    }

    /**
     * Deletes a chantier via API
     */
    private void deleteChantier(Long id) throws Exception {
        mockMvc.perform(delete("/api/chantier/{id}", id)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());


    }

    /**
     * Gets whether a chantier requires PDP via API
     */
    private Boolean getRequiresPdp(Long id) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/chantier/{id}/requires-pdp", id)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rootNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return rootNode.get("data").asBoolean();
    }

    /**
     * Gets the last chantier ID via API
     */
    private Long getLastChantierId() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/chantier/last")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rootNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return rootNode.get("data").asLong();
    }

    /**
     * Parses API response into specified type
     */
    private <T> ApiResponse<T> parseResponse(MvcResult result, TypeReference<ApiResponse<T>> typeReference) throws IOException {
        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseJson, typeReference);
    }

    /**
     * Parses data field from API response into specified type
     */
    private <T> T parseResponseData(MvcResult result, Class<T> clazz) throws IOException {
        String responseJson = result.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseJson);
        JsonNode dataNode = rootNode.get("data");
        return objectMapper.treeToValue(dataNode, clazz);
    }

    /**
     * Parses data field from API response into list of specified type
     */
    private <T> List<T> parseResponseDataList(MvcResult result, Class<T> clazz) throws IOException {
        String responseJson = result.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseJson);
        JsonNode dataNode = rootNode.get("data");
        return objectMapper.readValue(
                dataNode.toString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, clazz)
        );
    }
}