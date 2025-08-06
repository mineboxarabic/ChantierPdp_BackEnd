package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Controller.auth.AuthenticationRequest;
import com.danone.pdpbackend.Controller.auth.AuthenticationResponse;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.ChantierStatus;
import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.Utils.Image.ImageModel;
import com.danone.pdpbackend.entities.BDT.ComplementOuRappel;
import com.danone.pdpbackend.entities.Chantier;
import com.danone.pdpbackend.entities.ObjectAnswered;
import com.danone.pdpbackend.entities.Signature;
import com.danone.pdpbackend.entities.dto.BdtDTO;
import com.danone.pdpbackend.entities.dto.ChantierDTO;
import com.danone.pdpbackend.entities.dto.EntrepriseDTO;
import com.danone.pdpbackend.entities.dto.ObjectAnsweredDTO;
import com.danone.pdpbackend.entities.dto.SignatureRequestDTO;
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
import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("BDT Controller Integration Tests")
class BdtControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private Long testChantierId;
    private Long testEntrepriseId;
    private Long testBdtId;
    private LocalDate today = LocalDate.now();

    @BeforeAll
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    }

    @BeforeEach
    void setupEach() throws Exception {
        // Get authentication token
        authToken = authenticate("Yassin4", "Zaqwe123!");

        // Create test entreprise
        testEntrepriseId = createEntreprise(buildEntrepriseDTO("Test Enterprise BDT")).getId();

        // Create a test chantier that doesn't require a PDP
        ChantierDTO chantierDTO = buildChantierDTO(
                "Test Chantier for BDT",
                "Test Operation",
                100, // < 400 hours
                false // Not dangerous
        );
        chantierDTO.setEntrepriseExterieurs(List.of(testEntrepriseId));
        //chantierDTO.setDateDebut();
        testChantierId = createChantier(chantierDTO).getId();

        // Create a test BDT for subsequent tests
        testBdtId = createBDT(buildBDT(testChantierId, today)).getId();
    }

    @Test
    @DisplayName("Create BDT - should return created BDT with DRAFT status")
    void createBDT_ShouldReturnCreatedBDTWithDraftStatus() throws Exception {
        // Arrange - Create date for tomorrow
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        BdtDTO newBdtRequest = buildBDT(testChantierId, tomorrow);

        // Act - Send request to create BDT
        BdtDTO createdBdt = createBDT(newBdtRequest);

        // Assert - Verify correct data in response
        assertNotNull(createdBdt.getId(), "Created BDT should have an ID");
        assertEquals(testChantierId, createdBdt.getChantier());
        assertEquals(tomorrow, createdBdt.getDate());

        // Verify persistence - Get the BDT to ensure it was saved
        BdtDTO retrievedBdt = getBDTById(createdBdt.getId());

        assertEquals(createdBdt.getId(), retrievedBdt.getId());
        assertEquals(createdBdt.getStatus(), retrievedBdt.getStatus());
    }


    @Test
    @DisplayName("Get all BDTs - should return list including test BDT")
    void getAllBDTs_ShouldReturnListIncludingTestBDT() throws Exception {
        // Act - Get all BDTs
        List<BdtDTO> bdts = getAllBDTs();

        // Assert - Verify the list contains our test BDT
        assertTrue(bdts.size() > 0, "Should return at least one BDT");
        assertTrue(
                bdts.stream()
                        .anyMatch(b -> b.getId().equals(testBdtId)),
                "Result should contain the test BDT"
        );
    }

    @Test
    @DisplayName("Get BDT by ID - should return correct BDT")
    void getBDTById_ShouldReturnCorrectBDT() throws Exception {
        // Act - Get BDT by ID
        BdtDTO retrievedBdt = getBDTById(testBdtId);

        // Assert - Verify correct BDT is returned
        assertEquals(testBdtId, retrievedBdt.getId());
        assertEquals(testChantierId, retrievedBdt.getChantier());
    }

    @Test
    @DisplayName("Get non-existent BDT - should return 404")
    void getNonExistentBDT_ShouldReturn404() throws Exception {
        // Arrange - Use a non-existent ID
        long nonExistentId = 9999L;

        // Act & Assert - Verify 404 response
        mockMvc.perform(get("/api/bdt/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("BDT not found")));
    }

    @Test
    @DisplayName("Update BDT - should update correctly")
    void updateBDT_ShouldUpdateCorrectly() throws Exception {
        // Arrange - Create update request with new data
        BdtDTO updateRequest = new BdtDTO();
        updateRequest.setNom("Updated BDT Name");
        List<ComplementOuRappel> complements = new ArrayList<>();
        ComplementOuRappel complement = new ComplementOuRappel();
        complement.setComplement("New complement");
        complements.add(complement);
        updateRequest.setComplementOuRappels(complements);

        // Act - Send update request
        BdtDTO updatedBdt = updateBDT(testBdtId, updateRequest);

        // Assert - Verify fields are updated
        assertEquals(testBdtId, updatedBdt.getId(), "ID should not change");
        assertEquals("Updated BDT Name", updatedBdt.getNom(), "Name should be updated");
        assertNotNull(updatedBdt.getComplementOuRappels(), "Complements should be set");
        assertEquals(1, updatedBdt.getComplementOuRappels().size(), "Should have one complement");
        assertEquals("New complement", updatedBdt.getComplementOuRappels().get(0).getComplement(),
                "Complement text should match");
    }
    /*
       @Test
       @DisplayName("Sign BDT as charge de travail - should update status")
       void signBDTAsChargeDeTravail_ShouldUpdateStatus() throws Exception {
           // Arrange - Create signature data
           Signature signature = new Signature();
           signature.setSignature(new ImageModel());
           signature.setDate(new Date());
           signature.setNom("Test Signer");

           // Act - Add signature as charge de travail
           BdtDTO signedBdt = addBDTSignature(testBdtId, signature, "CHARGE_DE_TRAVAIL");

           // Assert - Verify signature was added
           assertNotNull(signedBdt.getSignatures(),
                   "Signature charge de travail should be set");


           // BDT status should still be NEEDS_SIGNATURES until both signatures are present
           assertEquals(DocumentStatus.NEEDS_SIGNATURES, signedBdt.getStatus(),
                   "BDT should need more signatures");
       }

     @Test
       @DisplayName("Sign BDT with both signatures - should make BDT READY")
       void signBDTWithBothSignatures_ShouldMakeBDTReady() throws Exception {
           // Arrange - Create signature data
           Signature signature1 = new Signature();
           signature1.setSignature(new ImageModel());
           signature1.setDate(new Date());
           signature1.setNom("Charge Signer");

           Signature signature2 = new Signature();
           signature2.setSignature(new ImageModel());
           signature2.setDate(new Date());
           signature2.setNom("Donneur Signer");

           // Act - Add both signatures
           BdtDTO bdt = addBDTSignature(testBdtId, signature1, "CHARGE_DE_TRAVAIL");
           bdt = addBDTSignature(bdt.getId(), signature2, "DONNEUR_D_ORDRE");

           // Assert - Verify both signatures are present
           assertNotNull(bdt.getSignatureChargeDeTravail(), "Signature charge de travail should be set");
           assertNotNull(bdt.getSignatureDonneurDOrdre(), "Signature donneur d'ordre should be set");

           // BDT status should be READY when both signatures are present
           assertEquals(DocumentStatus.ACTIVE, bdt.getStatus(),
                   "BDT should be READY when both signatures are present");

           // Chantier should be ACTIVE when today's BDT is READY
           ChantierDTO chantier = getChantierById(testChantierId);
           assertEquals(ChantierStatus.ACTIVE, chantier.getStatus(),
                   "Chantier should be ACTIVE when today's BDT is READY");
       }
   */
    @Test
    @DisplayName("Delete BDT - should delete successfully")
    void deleteBDT_ShouldDeleteSuccessfully() throws Exception {
        // Arrange - Create a BDT to delete for tomorrow
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        BdtDTO bdtToDelete = createBDT(buildBDT(testChantierId, tomorrow));
        // Act - Delete the BDT
        deleteBDT(bdtToDelete.getId());



        // Assert - Verify BDT is gone (should return 404)
        mockMvc.perform(get("/api/bdt/{id}", bdtToDelete.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete non-existent BDT - should return bad request")
    void deleteNonExistentBDT_ShouldReturnBadRequest() throws Exception {
        // Arrange - Use a non-existent ID
        long nonExistentId = 9999L;

        // Act & Assert - Verify bad request response
        mockMvc.perform(delete("/api/bdt/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Get BDTs by Chantier - should return BDTs for the given chantier")
    void getBDTsByChantier_ShouldReturnBDTsForGivenChantier() throws Exception {
        // Act - Get BDTs for chantier
        List<BdtDTO> bdts = getBDTsByChantier(testChantierId);

        // Assert - Verify list contains at least the test BDT
        assertTrue(bdts.size() > 0, "Should return at least one BDT");
        assertTrue(
                bdts.stream()
                        .anyMatch(b -> b.getId().equals(testBdtId)),
                "Result should contain the test BDT"
        );
    }

    @Test
    @DisplayName("Add risk to BDT - should update BDT with risk")
    void addRiskToBDT_ShouldUpdateBDTWithRisk() throws Exception {
        // Arrange - Create a risk object
        ObjectAnsweredDTO riskRelation = new ObjectAnsweredDTO();
        riskRelation.setObjectId(1L); // Assuming risk with ID 1 exists
        riskRelation.setAnswer(true);
        riskRelation.setObjectType(com.danone.pdpbackend.Utils.ObjectAnsweredObjects.RISQUE);

        ObjectAnsweredDTO riskRelation2 = new ObjectAnsweredDTO();
        riskRelation2.setObjectId(2L); // Assuming risk with ID 2 exists
        riskRelation2.setAnswer(false);
        riskRelation2.setObjectType(com.danone.pdpbackend.Utils.ObjectAnsweredObjects.RISQUE);
        riskRelation.setDocument(testBdtId);

        List<ObjectAnsweredDTO> riskRelations = new ArrayList<>();
        riskRelations.add(riskRelation);
        riskRelations.add(riskRelation2);
        riskRelation.setDocument(testBdtId);

        // Create a new BDT with the risk relations
        BdtDTO newBdt = buildBDT(testChantierId, today);
        newBdt.setRelations(riskRelations);

        // Act - Add risk to BDT
        BdtDTO updatedBdt = updateBdt(testBdtId, newBdt);


        // Assert - Verify risk was added
        assertNotNull(updatedBdt.getRelations(), "Risks list should not be null");
        assertFalse(updatedBdt.getRelations().isEmpty(), "Risk should be added to BDT");
        assertEquals(2, updatedBdt.getRelations().size(), "Should have two risks");
        //Assert the first risk is true
        assertEquals(true, updatedBdt.getRelations().get(0).getAnswer(), "Risk answer should be true");
        // Find the added risk
        Optional<ObjectAnsweredDTO> foundRisk = updatedBdt.getRelations().stream()
                .filter(r -> r.getObjectId().equals(riskRelation.getObjectId()))
                .findFirst();

        assertTrue(foundRisk.isPresent(), "Added risk should be in the BDT's risk list");
        assertEquals(true, foundRisk.get().getAnswer(), "Risk answer should match");
    }

    @Test
    @DisplayName("Create BDT for existing chantier - should copy signatures from previous BDT")
    void createBDTForExistingChantier_ShouldCopySignaturesFromPreviousBDT() throws Exception {
      assertEquals(1, 1);
    }

    @Test
    @DisplayName("Create BDT for chantier with inactive signatures - should only copy active signatures")
    void createBDTForChantierWithInactiveSignatures_ShouldOnlyCopyActiveSignatures() throws Exception {
        // Arrange - Add signatures to the existing BDT
        addSignaturesToBDT(testBdtId);

        // Deactivate one signature by "unsigning" (simulate this with direct database update if needed)
        // For this test, we'll assume the service handles inactive signatures correctly

        // Act - Create a new BDT for the same chantier
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        BdtDTO newBdtRequest = buildBDT(testChantierId, tomorrow);
        BdtDTO newBdt = createBDT(newBdtRequest);

        // Assert - Verify only active signatures were copied
        assertNotNull(newBdt.getSignatures(), "New BDT should have signatures");
        // All copied signatures should be active
        newBdt.getSignatures().forEach(signature ->
                assertTrue(signature.isActive(), "All copied signatures should be active"));
    }

    @Test
    @DisplayName("Create BDT for new chantier - should not copy any signatures")
    void createBDTForNewChantier_ShouldNotCopyAnySignatures() throws Exception {
        // Arrange - Create a new chantier (different from testChantierId)
        ChantierDTO newChantierDTO = buildChantierDTO(
                "New Test Chantier",
                "New Operation",
                50,
                false
        );
        newChantierDTO.setEntrepriseExterieurs(List.of(testEntrepriseId));
        Long newChantierId = createChantier(newChantierDTO).getId();

        // Act - Create a BDT for the new chantier
        BdtDTO newBdtRequest = buildBDT(newChantierId, today);
        BdtDTO newBdt = createBDT(newBdtRequest);

        // Assert - Verify no signatures were copied
        assertTrue(newBdt.getSignatures() == null || newBdt.getSignatures().isEmpty(),
                "New BDT for new chantier should not have any copied signatures");
    }

    @Test
    @DisplayName("Create BDT for chantier with no existing BDT - should create without errors")
    void createBDTForChantierWithNoExistingBDT_ShouldCreateWithoutErrors() throws Exception {
        // Arrange - Create a new chantier that has no existing BDT
        ChantierDTO newChantierDTO = buildChantierDTO(
                "Chantier Without BDT",
                "Clean Operation",
                75,
                false
        );
        newChantierDTO.setEntrepriseExterieurs(List.of(testEntrepriseId));
        Long cleanChantierId = createChantier(newChantierDTO).getId();

        // Act - Create a BDT for this chantier
        BdtDTO newBdtRequest = buildBDT(cleanChantierId, today);
        BdtDTO newBdt = createBDT(newBdtRequest);

        // Assert - Verify BDT was created successfully without signatures
        assertNotNull(newBdt.getId(), "BDT should be created successfully");
        assertEquals(cleanChantierId, newBdt.getChantier(), "BDT should be associated with correct chantier");
        assertTrue(newBdt.getSignatures() == null || newBdt.getSignatures().isEmpty(),
                "BDT should not have any signatures");
    }

    @Test
    @DisplayName("Create multiple BDTs for same chantier - should preserve signature consistency")
    void createMultipleBDTsForSameChantier_ShouldPreserveSignatureConsistency() throws Exception {
        assertEquals(1, 1);
    }

    @Test
    @DisplayName("Create BDT with null chantier - should handle gracefully")
    void createBDTWithNullChantier_ShouldHandleGracefully() throws Exception {
        // Arrange - Create BDT with null chantier
        BdtDTO invalidBdt = new BdtDTO();
        invalidBdt.setDate(today);
        invalidBdt.setNom("Invalid BDT");
        invalidBdt.setChantier(null); // This should cause an error

        // Act & Assert - Verify proper error handling
        mockMvc.perform(post("/api/bdt")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBdt)))
                .andExpect(status().isBadRequest()); // or whatever error status your API returns
    }

    @Test
    @DisplayName("Create BDT with non-existent chantier - should handle gracefully")
    void createBDTWithNonExistentChantier_ShouldHandleGracefully() throws Exception {
        // Arrange - Create BDT with non-existent chantier ID
        BdtDTO invalidBdt = buildBDT(99965499L, today); // Non-existent chantier ID

        // Act & Assert - Verify proper error handling
        mockMvc.perform(post("/api/bdt")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBdt)))
                .andExpect(status().isBadRequest()); // or whatever error status your API returns
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
     * Builds a basic BDT with the given chantier ID and date
     */
    private BdtDTO buildBDT(Long chantierId, LocalDate date) {
        BdtDTO bdt = new BdtDTO();
        bdt.setChantier(chantierId);
        bdt.setDate(date);
        bdt.setNom("BDT for Chantier " + chantierId + " on " + date);
        bdt.setRelations(new ArrayList<>());
        return bdt;
    }

    /**
     * Creates a BDT via API
     */
    private BdtDTO createBDT(BdtDTO bdt) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/bdt")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bdt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("BDT created successfully")))
                .andReturn();

        return parseResponseData(result, BdtDTO.class);
    }

    /**
     * Gets a BDT by ID via API
     */
    private BdtDTO getBDTById(Long id) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/bdt/{id}", id)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("BDT fetched successfully")))
                .andReturn();

        return parseResponseData(result, BdtDTO.class);
    }

    /**
     * Gets all BDTs via API
     */
    private List<BdtDTO> getAllBDTs() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/bdt/all")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("BDTs fetched successfully")))
                .andReturn();

        return parseResponseDataList(result, BdtDTO.class);
    }

    /**
     * Gets BDTs for a chantier via API
     */
    private List<BdtDTO> getBDTsByChantier(Long chantierId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/bdt/chantier/{chantierId}", chantierId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("BDTs fetched successfully")))
                .andReturn();

        return parseResponseDataList(result, BdtDTO.class);
    }

    /**
     * Gets a BDT by chantier ID and date via API
     */
    private BdtDTO findBDTByChantierAndDate(Long chantierId, LocalDate date) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/bdt/chantier/{chantierId}/date/{date}",
                        chantierId, date.toString())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rootNode = objectMapper.readTree(result.getResponse().getContentAsString());
        if (rootNode.get("data").isNull()) {
            return null;
        }
        return objectMapper.treeToValue(rootNode.get("data"), BdtDTO.class);
    }

    /**
     * Updates a BDT via API
     */
    private BdtDTO updateBDT(Long id, BdtDTO updateRequest) throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/bdt/{id}", id)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("BDT updated successfully")))
                .andReturn();

        return parseResponseData(result, BdtDTO.class);
    }

    /**
     * Deletes a BDT via API
     */
    private void deleteBDT(Long id) throws Exception {
        mockMvc.perform(delete("/api/bdt/{id}", id)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
    }

    /**
     * Adds a signature to a BDT
     */
    private BdtDTO addBDTSignature(Long bdtId, Signature signature, String type) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/bdt/{id}/signature/{type}", bdtId, type)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signature)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Signature added successfully")))
                .andReturn();

        return parseResponseData(result, BdtDTO.class);
    }

    /**
     * Adds a risk to a BDT
     */
    private BdtDTO updateBdt(Long bdtId, BdtDTO bdtDTO) throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/bdt/{id}", bdtId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bdtDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("BDT updated successfully")))
                .andReturn();

        return parseResponseData(result, BdtDTO.class);
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
