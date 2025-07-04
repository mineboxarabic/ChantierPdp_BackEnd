package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Controller.auth.AuthenticationRequest;
import com.danone.pdpbackend.Controller.auth.AuthenticationResponse;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.entities.Permit;
import com.danone.pdpbackend.entities.Risque;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.*;

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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("PDP Controller Integration Tests")
class PdpControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private Long testChantierId;
    private Long testEntrepriseId;
    private Long testPdpId;
    private Long testWorkerId1;
    private Long testWorkerId2;

    @BeforeAll
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    }

    @BeforeEach
    void setupEach() throws Exception {
        // Get authentication token
        authToken = authenticate("Yassin4", "Zaqwe123!");

        // Create test data for subsequent tests
        testEntrepriseId = createEntreprise(buildEntrepriseDTO("Test Enterprise")).getId();
        testChantierId = createChantier(buildChantierDTO("Test Chantier", testEntrepriseId)).getId();

        // Create test workers
        testWorkerId1 = createWorker(buildWorkerDTO("Worker One", testEntrepriseId)).getId();
        testWorkerId2 = createWorker(buildWorkerDTO("Worker Two", testEntrepriseId)).getId();

        // Create a test PDP for subsequent tests
        testPdpId = createPdp(buildPdpDTO(
                testChantierId,
                testEntrepriseId,
                "Test PDP Details"
        )).getId();
    }

    @Test
    @DisplayName("Get all PDPs - should return list including test PDP")
    void getAllPdps_ShouldReturnListIncludingTestPdp() throws Exception {
        // Act - Get all PDPs
        List<PdpDTO> pdps = getAllPdps();

        // Assert - Verify the list contains our test PDP
        assertTrue(pdps.size() > 0, "Should return at least one PDP");
        assertTrue(
                pdps.stream()
                        .anyMatch(p -> p.getId().equals(testPdpId)),
                "Result should contain the test PDP"
        );
    }

    @Test
    @DisplayName("Get PDP by ID - should return correct PDP")
    void getPdpById_ShouldReturnCorrectPdp() throws Exception {
        // Act - Get PDP by ID
        PdpDTO retrievedPdp = getPdpById(testPdpId);

        // Assert - Verify correct PDP is returned
        assertEquals(testPdpId, retrievedPdp.getId());
        assertEquals(testChantierId, retrievedPdp.getChantier());
        assertEquals(testEntrepriseId, retrievedPdp.getEntrepriseExterieure());
        assertEquals("Test PDP Details", retrievedPdp.getHorairesDetails());
    }


    @Test
    @DisplayName("Get non-existent PDP - should return 404")
    void getNonExistentPdp_ShouldReturn404() throws Exception {
        // Arrange - Use a non-existent ID
        long nonExistentId = 9999L;

        // Act & Assert - Verify 404 response
        mockMvc.perform(get("/api/pdp/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Pdp not found")));
    }

    @Test
    @DisplayName("Create PDP - should return created PDP with correct data")
    void createPdp_ShouldReturnCreatedPdpWithCorrectData() throws Exception {
        // Arrange - Create DTO with standard values
        PdpDTO newPdpDTO = buildPdpDTO(
                testChantierId,
                testEntrepriseId,
                "New PDP Details"
        );

        // Act - Send request to create PDP
        PdpDTO createdPdp = createPdp(newPdpDTO);

        // Assert - Verify correct data in response
        assertNotNull(createdPdp.getId(), "Created PDP should have an ID");
        assertEquals(testChantierId, createdPdp.getChantier());
        assertEquals(testEntrepriseId, createdPdp.getEntrepriseExterieure());
        assertEquals("New PDP Details", createdPdp.getHorairesDetails());

        // Verify persistence - Get the PDP to ensure it was saved
        PdpDTO retrievedPdp = getPdpById(createdPdp.getId());
        assertEquals(createdPdp.getId(), retrievedPdp.getId());
        assertEquals(createdPdp.getHorairesDetails(), retrievedPdp.getHorairesDetails());
    }

    @Test
    @DisplayName("Update PDP - should update correctly")
    void updatePdp_ShouldUpdateCorrectly() throws Exception {
        // Arrange - Create update DTO
        PdpDTO updateRequest = new PdpDTO();
        updateRequest.setHorairesDetails("Updated PDP Details");

        // Act - Send update request
        PdpDTO updatedPdp = updatePdp(testPdpId, updateRequest);

        // Assert - Verify fields are updated
        assertEquals(testPdpId, updatedPdp.getId(), "ID should not change");
        assertEquals("Updated PDP Details", updatedPdp.getHorairesDetails(), "Details should be updated");

        // Verify persistence with a separate request
        PdpDTO retrievedPdp = getPdpById(testPdpId);
        assertEquals("Updated PDP Details", retrievedPdp.getHorairesDetails());
    }

    @Test
    @DisplayName("Delete PDP - should delete successfully")
    void deletePdp_ShouldDeleteSuccessfully() throws Exception {
        // Arrange - Create a PDP to delete
        PdpDTO pdpToDelete = createPdp(buildPdpDTO(
                testChantierId,
                testEntrepriseId,
                "PDP To Delete"
        ));

        // Pre-check - Ensure it exists
        getPdpById(pdpToDelete.getId());

        // Act - Delete the PDP
        deletePdp(pdpToDelete.getId());

        // Assert - Verify PDP is gone (should return 404)
        mockMvc.perform(get("/api/pdp/{id}", pdpToDelete.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete non-existent PDP - should return bad request")
    void deleteNonExistentPdp_ShouldReturnBadRequest() throws Exception {
        // Arrange - Use a non-existent ID
        long nonExistentId = 9999L;

        // Pre-check - Ensure it doesn't exist
        mockMvc.perform(get("/api/pdp/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());

        // Act & Assert - Verify bad request response
        mockMvc.perform(delete("/api/pdp/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("Get Object Answered by PDP ID - should return items for the given object type")
    void getObjectAnsweredByPdpId_ShouldReturnItemsForGivenObjectType() throws Exception {
        // Arrange - Use existing PDP
        Long pdpId = testPdpId;
        ObjectAnsweredObjects objectType = ObjectAnsweredObjects.RISQUE; // Example type

        // Act & Assert - Get object answered items
        mockMvc.perform(get("/api/pdp/{pdpId}/object-answered/{objectType}", pdpId, objectType)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("items fetched")));

        // Note: We don't expect a specific number of items as that would depend on test data
        // Just verifying that the endpoint works and returns a valid response
    }

    @Test
    @DisplayName("PDP Existence - should check if PDP exists")
    void pdpExistence_ShouldCheckIfPdpExists() throws Exception {
        // Arrange - Use existing PDP
        Long existingPdpId = testPdpId;
        Long nonExistingPdpId = 9999L;

        // Act & Assert - Check existing PDP
        mockMvc.perform(get("/api/pdp/exist/{id}", existingPdpId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", is(true)))
                .andExpect(jsonPath("$.message", is("Pdp exist")));

        // Act & Assert - Check non-existing PDP
        mockMvc.perform(get("/api/pdp/exist/{id}", nonExistingPdpId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", is(false)))
                .andExpect(jsonPath("$.message", is("Pdp exist")));
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
     * Builds an EntrepriseDTO with the given name
     */
    private EntrepriseDTO buildEntrepriseDTO(String nom) {
        return EntrepriseDTO.builder()
                .nom(nom)
                .build();
    }

    /**
     * Creates an entreprise via API
     */
    private EntrepriseDTO createEntreprise(EntrepriseDTO entrepriseDTO) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/entreprise")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrepriseDTO)))
                .andExpect(status().isOk())
                .andReturn();

        return parseResponseData(result, EntrepriseDTO.class);
    }

    /**
     * Builds a ChantierDTO with the given name and entreprise ID
     */
    private ChantierDTO buildChantierDTO(String nom, Long entrepriseId) {
        ChantierDTO dto = new ChantierDTO();
        dto.setNom(nom);
        // Set any other required fields
        if (entrepriseId != null) {
            dto.setEntrepriseExterieurs(List.of(entrepriseId));
        }

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
                .andReturn();

        return parseResponseData(result, ChantierDTO.class);
    }

    /**
     * Builds a WorkerDTO with the given name and entreprise ID
     */
    private WorkerDTO buildWorkerDTO(String name, Long entrepriseId) {
        WorkerDTO dto = new WorkerDTO();
        dto.setNom(name);
        dto.setPrenom("Test");
        dto.setEntreprise(entrepriseId);
        // Set any other required fields
        return dto;
    }

    /**
     * Creates a worker via API
     */
    private WorkerDTO createWorker(WorkerDTO workerDTO) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/worker/")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workerDTO)))
                .andExpect(status().isOk())
                .andReturn();

        return parseResponseData(result, WorkerDTO.class);
    }

    /**
     * Builds a PdpDTO with the given parameters
     */
    private PdpDTO buildPdpDTO(Long chantierId, Long entrepriseId, String horairesDetails) {
        PdpDTO dto = new PdpDTO();
        dto.setChantier(chantierId);
        dto.setEntrepriseExterieure(entrepriseId);
        dto.setHorairesDetails(horairesDetails);
        // Set other default fields as needed
        return dto;
    }

    /**
     * Creates a PDP via API
     */
    private PdpDTO createPdp(PdpDTO pdpDTO) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/pdp/")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pdpDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Pdp saved successfully")))
                .andReturn();

        return parseResponseData(result, PdpDTO.class);
    }

    /**
     * Gets a PDP by ID via API
     */
    private PdpDTO getPdpById(Long id) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/pdp/{id}", id)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Pdp fetched successfully")))
                .andReturn();

        return parseResponseData(result, PdpDTO.class);
    }

    /**
     * Gets all PDPs via API
     */
    private List<PdpDTO> getAllPdps() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/pdp/all")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Pdps fetched successfully")))
                .andReturn();

        return parseResponseDataList(result, PdpDTO.class);
    }

    /**
     * Updates a PDP via API
     */
    private PdpDTO updatePdp(Long id, PdpDTO updateDTO) throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/pdp/{id}", id)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Pdp updated successfully")))
                .andReturn();

        return parseResponseData(result, PdpDTO.class);
    }

    /**
     * Deletes a PDP via API
     */
    private void deletePdp(Long id) throws Exception {
        mockMvc.perform(delete("/api/pdp/{id}", id)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
    }

    /**
     * Signs a PDP
     */
    private void signPdp(Long pdpId) throws Exception {
        mockMvc.perform(post("/api/pdp/sign/{pdpId}", pdpId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Pdp signed successfully")));
    }

    /**
     * Gets workers associated with a PDP
     */
    private List<Worker> getWorkersByPdp(Long pdpId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/pdp/pdp/{pdpId}/workers", pdpId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Workers fetched")))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseJson);
        JsonNode dataNode = rootNode.get("data");
        return objectMapper.readValue(
                dataNode.toString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Worker.class)
        );
    }

    private Risque createRisqueViaApi(String title, Boolean travailleDangereux, Boolean travaillePermit, Long permitId) throws Exception {
        Risque risqueRequest = new Risque();
        risqueRequest.setTitle(title);
        risqueRequest.setTravailleDangereux(travailleDangereux);
        risqueRequest.setTravaillePermit(travaillePermit);
        if (permitId != null) {
            risqueRequest.setPermitId(permitId);
        }

        MvcResult result = mockMvc.perform(post("/api/risque")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(risqueRequest)))
                .andExpect(status().isOk())
                .andReturn();
        ApiResponse<Risque> apiResponse = parseResponse(result, new TypeReference<ApiResponse<Risque>>() {});
        assertNotNull(apiResponse.getData(), "Risque data should not be null in response");
        assertNotNull(apiResponse.getData().getId(), "Created Risque should have an ID");
        return apiResponse.getData();
    }

    private Permit createPermitViaApi(String title) throws Exception {
        Permit permitRequest = new Permit();
        permitRequest.setTitle(title);
        // Set other Permit fields if necessary, e.g., type

        MvcResult result = mockMvc.perform(post("/api/permit")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permitRequest)))
                .andExpect(status().isOk())
                .andReturn();
        ApiResponse<Permit> apiResponse = parseResponse(result, new TypeReference<ApiResponse<Permit>>() {});
        assertNotNull(apiResponse.getData(), "Permit data should not be null in response");
        assertNotNull(apiResponse.getData().getId(), "Created Permit should have an ID");
        return apiResponse.getData();
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


    @Test
    @DisplayName("Get Risques Without Permits - Should return risque that needs permit and has none")
    void getRisquesWithoutPermits_shouldReturnRisqueNeedingPermitAndHasNone() throws Exception {
        // 1. Create a Risque that is dangerous and needs a permit (travailleDangereux=true, permitId=null)
        Risque risqueNeedingPermit = createRisqueViaApi("Dangerous Risque No Permit " + System.nanoTime(), true, true, null);

        // 2. Create a Risque that is dangerous but has a permit
        Permit permit = createPermitViaApi("Permit for Test " + System.nanoTime());
        Risque risqueWithPermit = createRisqueViaApi("Dangerous Risque With Permit " + System.nanoTime(), true, true, permit.getId());

        // 3. Create a Risque that is not dangerous
        Risque risqueNotDangerous = createRisqueViaApi("Safe Risque " + System.nanoTime(), false, false, null);

        // 4. Link these risques to the testPdpId
        PdpDTO pdpToUpdate = getPdpById(testPdpId);
        pdpToUpdate.setRelations(Arrays.asList(
                ObjectAnsweredDTO.builder().objectType(ObjectAnsweredObjects.RISQUE).objectId(risqueNeedingPermit.getId()).answer(true).build(),
                ObjectAnsweredDTO.builder().objectType(ObjectAnsweredObjects.RISQUE).objectId(risqueWithPermit.getId()).answer(true).build(),
                ObjectAnsweredDTO.builder().objectType(ObjectAnsweredObjects.RISQUE).objectId(risqueNotDangerous.getId()).answer(true).build()
        ));
        updatePdp(testPdpId, pdpToUpdate);

        // 5. Call the endpoint
        MvcResult result = mockMvc.perform(get("/api/pdp/{pdpId}/risques-without-permits", testPdpId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("items fetched")))
                .andExpect(jsonPath("$.data", hasSize(1))) // Expecting only one
                .andExpect(jsonPath("$.data[0].objectId", is(risqueNeedingPermit.getId().intValue())))
                .andExpect(jsonPath("$.data[0].objectType", is("RISQUE")))
                .andReturn();
    }

    @Test
    @DisplayName("Get Risques Without Permits - Should return empty if all dangerous risques have permits")
    void getRisquesWithoutPermits_shouldReturnEmptyIfAllDangerousRisquesHavePermits() throws Exception {
        // 1. Create a Risque that is dangerous and has a permit
        Permit permit1 = createPermitViaApi("Permit 1 " + System.nanoTime());
        Risque risqueWithPermit1 = createRisqueViaApi("Dangerous Risque With Permit 1 " + System.nanoTime(), true, true, permit1.getId());

        // 2. Create another Risque that is dangerous and also has a permit
        Permit permit2 = createPermitViaApi("Permit 2 " + System.nanoTime());
        Risque risqueWithPermit2 = createRisqueViaApi("Dangerous Risque With Permit 2 " + System.nanoTime(), true, true, permit2.getId());

        // 3. Link these risques to the testPdpId
        PdpDTO pdpToUpdate = getPdpById(testPdpId);
        pdpToUpdate.setRelations(Arrays.asList(
                ObjectAnsweredDTO.builder().objectType(ObjectAnsweredObjects.RISQUE).objectId(risqueWithPermit1.getId()).answer(true).build(),
                ObjectAnsweredDTO.builder().objectType(ObjectAnsweredObjects.RISQUE).objectId(risqueWithPermit2.getId()).answer(true).build()
        ));
        updatePdp(testPdpId, pdpToUpdate);

        // 4. Call the endpoint
        mockMvc.perform(get("/api/pdp/{pdpId}/risques-without-permits", testPdpId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("items fetched")))
                .andExpect(jsonPath("$.data", hasSize(0))); // Expecting empty list
    }

    @Test
    @DisplayName("Get Risques Without Permits - Should return empty if no dangerous risques")
    void getRisquesWithoutPermits_shouldReturnEmptyIfNoDangerousRisques() throws Exception {
        // 1. Create a Risque that is not dangerous
        Risque safeRisque = createRisqueViaApi("Safe Risque Only " + System.nanoTime(), false, false, null);

        // 2. Link this risque to the testPdpId
        PdpDTO pdpToUpdate = getPdpById(testPdpId);
        pdpToUpdate.setRelations(Collections.singletonList(
                ObjectAnsweredDTO.builder().objectType(ObjectAnsweredObjects.RISQUE).objectId(safeRisque.getId()).answer(true).build()
        ));
        updatePdp(testPdpId, pdpToUpdate);

        // 3. Call the endpoint
        mockMvc.perform(get("/api/pdp/{pdpId}/risques-without-permits", testPdpId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("items fetched")))
                .andExpect(jsonPath("$.data", hasSize(0))); // Expecting empty list
    }

    @Test
    @DisplayName("Get Risques Without Permits - Should return empty if PDP has no risques linked")
    void getRisquesWithoutPermits_shouldReturnEmptyIfPdpHasNoRisques() throws Exception {
        // PDP is created in @BeforeEach without any relations initially.
        // Call the endpoint
        mockMvc.perform(get("/api/pdp/{pdpId}/risques-without-permits", testPdpId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("items fetched")))
                .andExpect(jsonPath("$.data", hasSize(0))); // Expecting empty list
    }

}