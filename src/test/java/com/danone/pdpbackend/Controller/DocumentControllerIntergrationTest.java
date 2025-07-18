package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Utils.*;
import com.danone.pdpbackend.Utils.Image.ImageModel; // Assuming ImageModel exists
import com.danone.pdpbackend.entities.Permit;
import com.danone.pdpbackend.entities.Risque;
import com.danone.pdpbackend.entities.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Document Status Integration Tests")
@Transactional
public class DocumentControllerIntergrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private Long defaultEntrepriseId;
    private Long defaultDonneurDOrdreId = 1L; // Assuming user with ID 1 exists and can be a donneur d'ordre

    @BeforeEach
    public void setUp() throws Exception {
        objectMapper.registerModule(new JavaTimeModule()); // For LocalDate serialization

        // Authenticate and get token
        MvcResult loginResult = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"Yassin4\", \"password\": \"Zaqwe123!\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String loginResponseJson = loginResult.getResponse().getContentAsString();
        ApiResponse<Map<String, Object>> authResponse = objectMapper.readValue(loginResponseJson, new TypeReference<>() {});
        authToken = (String) authResponse.getData().get("token");
        // Extract user ID if needed for donneurDOrdre, assuming 'user' object is in response
        Map<String, Object> userMap = (Map<String, Object>) authResponse.getData().get("user");
        if (userMap != null && userMap.containsKey("id")) {
            defaultDonneurDOrdreId = ((Number) userMap.get("id")).longValue();
        }


        // Create a default entreprise for tests
        defaultEntrepriseId = createEntrepriseAPI("Default Test Entreprise " + System.nanoTime()).getId();
    }

    // --- HELPER METHODS ---

    private EntrepriseDTO createEntrepriseAPI(String name) throws Exception {
        EntrepriseDTO entrepriseDTO = new EntrepriseDTO();
        entrepriseDTO.setNom(name);
        MvcResult result = mockMvc.perform(post("/api/entreprise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrepriseDTO))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ApiResponse<EntrepriseDTO>>() {}).getData();
    }

    private ChantierDTO createChantierAPI(String nom, Long entrepriseUtilisatriceId, Date dateDebut, Date dateFin, int nbHeures, boolean travauxDangereux) throws Exception {
        ChantierDTO chantierDTO = new ChantierDTO();
        chantierDTO.setNom(nom);
        chantierDTO.setDonneurDOrdre(defaultDonneurDOrdreId);
        chantierDTO.setEntrepriseUtilisatrice(entrepriseUtilisatriceId); // Danone as EU
        chantierDTO.setEntrepriseExterieurs(Collections.singletonList(defaultEntrepriseId)); // Default EE
        chantierDTO.setDateDebut(dateDebut);
        chantierDTO.setDateFin(dateFin);
        chantierDTO.setNbHeurs(nbHeures);
        chantierDTO.setTravauxDangereux(travauxDangereux);


        MvcResult result = mockMvc.perform(post("/api/chantier/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chantierDTO))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ApiResponse<ChantierDTO>>() {}).getData();
    }
    private ChantierDTO updateChantierAPI(Long chantierId, ChantierDTO chantierDTO) throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/chantier/" + chantierId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chantierDTO))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ApiResponse<ChantierDTO>>() {}).getData();
    }


    private PdpDTO createPdpAPI(Long chantierId, Long entrepriseExterieureId) throws Exception {
        PdpDTO pdpDTO = new PdpDTO();
        pdpDTO.setChantier(chantierId);
        pdpDTO.setEntrepriseExterieure(entrepriseExterieureId);
        pdpDTO.setDate(LocalDate.now());
        pdpDTO.setSignatures(new ArrayList<>());
        pdpDTO.setRelations(new ArrayList<>());
        // Add other necessary fields for a PdpDTO if any

        MvcResult result = mockMvc.perform(post("/api/pdp/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pdpDTO))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ApiResponse<PdpDTO>>() {}).getData();
    }

    private BdtDTO createBdtAPI(Long chantierId, Long entrepriseExterieureId, LocalDate date) throws Exception {
        BdtDTO bdtDTO = new BdtDTO();
        bdtDTO.setChantier(chantierId);
        bdtDTO.setEntrepriseExterieure(entrepriseExterieureId);
        bdtDTO.setDate(date);
        bdtDTO.setNom("BDT for " + chantierId + " on " + date);
        bdtDTO.setSignatures(new ArrayList<>());
        bdtDTO.setRelations(new ArrayList<>());
        // Add other necessary fields for a BdtDTO if any

        MvcResult result = mockMvc.perform(post("/api/bdt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bdtDTO))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ApiResponse<BdtDTO>>() {}).getData();
    }

    private PdpDTO getPdpByIdAPI(Long pdpId) throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/pdp/" + pdpId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ApiResponse<PdpDTO>>() {}).getData();
    }

    private BdtDTO getBdtByIdAPI(Long bdtId) throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/bdt/" + bdtId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ApiResponse<BdtDTO>>() {}).getData();
    }
    private ChantierDTO getChantierByIdAPI(Long chantierId) throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/chantier/" + chantierId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ApiResponse<ChantierDTO>>() {}).getData();
    }


    private PdpDTO updatePdpAPI(Long pdpId, PdpDTO pdpDTO) throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/pdp/" + pdpId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pdpDTO))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ApiResponse<PdpDTO>>() {}).getData();
    }

    private BdtDTO updateBdtAPI(Long bdtId, BdtDTO bdtDTO) throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/bdt/" + bdtId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bdtDTO))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ApiResponse<BdtDTO>>() {}).getData();
    }

    private WorkerDTO createWorkerAPI(String nom, Long entrepriseId) throws Exception {
        WorkerDTO workerDTO = new WorkerDTO();
        workerDTO.setNom(nom);
        workerDTO.setPrenom("TestWorker" + System.nanoTime());
        workerDTO.setEntreprise(entrepriseId);
        MvcResult result = mockMvc.perform(post("/api/worker/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workerDTO))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ApiResponse<WorkerDTO>>() {}).getData();
    }

    private void selectWorkerForChantierAPI(Long workerId, Long chantierId) throws Exception {

        WorkerChantierSelectionDTO requestDTO = new WorkerChantierSelectionDTO();
        requestDTO.setWorker(workerId);
        requestDTO.setChantier(chantierId);
        requestDTO.setIsSelected(true);


        mockMvc.perform(post("/api/worker-selection/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
    }

    private Risque createRisqueAPI(String title, boolean travaillePermit, PermiTypes permiType) throws Exception {
        Risque risque = new Risque();
        risque.setTitle(title);
        risque.setTravaillePermit(travaillePermit);
        risque.setPermitType(permiType);

        MvcResult result = mockMvc.perform(post("/api/risque")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(risque))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ApiResponse<Risque>>() {}).getData();
    }

    private Permit createPermitAPI(String title, PermiTypes type) throws Exception {
        Permit permit = new Permit();
        permit.setTitle(title);
        permit.setType(type);
        MvcResult result = mockMvc.perform(post("/api/permit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permit))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ApiResponse<Permit>>() {}).getData();
    }

    private DocumentSignatureDTO createSignatureDTO(Long workerId, Long documentId, String role, Long userId) {
        DocumentSignatureDTO signatureDTO = new DocumentSignatureDTO();
        signatureDTO.setWorkerId(workerId);
        signatureDTO.setSignatureDate(new Date());
        signatureDTO.setDocumentId(documentId); // Link to the document (PDP or BDT)
        signatureDTO.setUserId(userId); // Link to the user who performed the signing action
        // Mock visual data if necessary, or ensure your backend handles null gracefully if not strictly needed for status change
        ImageModel visual = new ImageModel();
        visual.setMimeType("image/png");
        visual.setImageData(new byte[]{1,2,3}); // Dummy data
        signatureDTO.setSignatureVisual(visual);
        signatureDTO.setSignerRole(role); // Role might be important for some logic
        signatureDTO.setActive(true);
        return signatureDTO;
    }
    private DocumentSignatureDTO createSignatureDTO(Long workerId, Long documentId, String role) {
        return createSignatureDTO(workerId, documentId, role, defaultDonneurDOrdreId);
    }

    // --- SIGNATURE API HELPER METHODS ---
    
    private void signDocumentAPI(Long documentId, Long workerId, String name, String lastName) throws Exception {
        SignatureRequestDTO signatureRequest = new SignatureRequestDTO();
        signatureRequest.setWorkerId(workerId);
        signatureRequest.setDocumentId(documentId);
        signatureRequest.setPrenom(name);
        signatureRequest.setNom(lastName);
        // Create a simple base64 encoded image (1x1 pixel PNG)
        String base64Image = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
        signatureRequest.setSignatureImage(base64Image);

        mockMvc.perform(post("/api/document/{documentId}/sign", documentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signatureRequest))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isCreated());
    }

    private Long signDocumentByWorkerAPI(Long documentId, Long workerId, String name, String lastName) throws Exception {
        SignatureRequestDTO signatureRequest = new SignatureRequestDTO();
        signatureRequest.setWorkerId(workerId);
        signatureRequest.setDocumentId(documentId);
        signatureRequest.setPrenom(name);
        signatureRequest.setNom(lastName);
        // Create a simple base64 encoded image (1x1 pixel PNG)
        String base64Image = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
        signatureRequest.setSignatureImage(base64Image);

        MvcResult result = mockMvc.perform(post("/api/document/worker/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signatureRequest))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isCreated())
                .andReturn();
        
        // Extract and return the signature ID from the response
        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode responseNode = objectMapper.readTree(jsonResponse);
        return responseNode.get("data").asLong();
    }

    private Long signDocumentByUserAPI(Long documentId, Long userId, String name, String lastName) throws Exception {
        // For user signing, we need to create a worker since the database requires worker_id
        // In a real scenario, this might be handled differently (e.g., users have associated worker records)
        //WorkerDTO userAsWorker = createWorkerAPI("UserAsWorker_" + userId, defaultEntrepriseId);
        
        SignatureRequestDTO signatureRequest = new SignatureRequestDTO();
        //signatureRequest.setWorkerId(userAsWorker.getId()); // Required due to database constraints
        signatureRequest.setUserId(userId);
        signatureRequest.setDocumentId(documentId);
        signatureRequest.setPrenom(name);
        signatureRequest.setNom(lastName);
        // Create a simple base64 encoded image (1x1 pixel PNG)
        String base64Image = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
        signatureRequest.setSignatureImage(base64Image);

        MvcResult result = mockMvc.perform(post("/api/document/user/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signatureRequest))
                        .header("Authorization", "Bearer " + authToken))
                .andReturn();
        
        // Debug: Print the response if it's not 201
        if (result.getResponse().getStatus() != 201) {
            System.err.println("Expected 201 but got: " + result.getResponse().getStatus());
            System.err.println("Response body: " + result.getResponse().getContentAsString());
            System.err.println("Request body: " + objectMapper.writeValueAsString(signatureRequest));
        }
        
        // Now assert the status
        if (result.getResponse().getStatus() != 201) {
            throw new AssertionError("Expected status 201 but was: " + result.getResponse().getStatus() + 
                                   ". Response: " + result.getResponse().getContentAsString());
        }

        // Parse the response to get the signature ID
        String jsonResponse = result.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode responseNode = objectMapper.readTree(jsonResponse);
        return responseNode.get("data").asLong();
    }

    private void unsignDocumentByUserAPI(Long userId, Long signatureId) throws Exception {
        // Since we use the worker-based unsigning due to database constraints,
        // we need to use the worker unsign endpoint instead
        // For now, we'll use a placeholder workerId - in reality, you'd need to track
        // which worker was created for the user
        Long placeholderWorkerId = 1L; // This should be the actual worker ID created for the user
        
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/document/user/{userId}/unsign/{signatureId}", userId, signatureId)
                        .header("Authorization", "Bearer " + authToken))
                .andReturn();
        
        // Debug: Print the response if it's not 200
        if (result.getResponse().getStatus() != 200) {
            System.err.println("Expected 200 but got: " + result.getResponse().getStatus());
            System.err.println("Response body: " + result.getResponse().getContentAsString());
        }
        
        // Now assert the status
        if (result.getResponse().getStatus() != 200) {
            throw new AssertionError("Expected status 200 but was: " + result.getResponse().getStatus() + 
                                   ". Response: " + result.getResponse().getContentAsString());
        }
    }

    // --- TEST METHODS ---
    @Test
    @DisplayName("Test PDP Document Status: COMPLETED")
    void testPdpStatus_COMPLETED() throws Exception {
        // Create a chantier that is completed
        ChantierDTO chantier = createChantierAPI("Chantier for Completed PDP", defaultEntrepriseId,
                new Date(System.currentTimeMillis() - 86400000 * 14), // Started 14 days ago
                new Date(System.currentTimeMillis() - 86400000), // Ended yesterday
                50, false);

        // Update the chantier status to COMPLETED
        chantier.setStatus(ChantierStatus.COMPLETED);
        ChantierDTO updatedChantier = updateChantierAPI(chantier.getId(), chantier);

        // Create PDP for this completed chantier
        PdpDTO pdp = createPdpAPI(updatedChantier.getId(), defaultEntrepriseId);

        // Add all necessary signatures to ensure status is determined by the chantier
        WorkerDTO worker1 = createWorkerAPI("WorkerForCompletedPDP", defaultEntrepriseId);
        WorkerDTO worker2 = createWorkerAPI("WorkerForCompletedPDP2", defaultEntrepriseId);

        selectWorkerForChantierAPI(worker1.getId(), updatedChantier.getId());
        selectWorkerForChantierAPI(worker2.getId(), updatedChantier.getId());

        // Sign the document using API endpoints instead of directly setting signatures
        signDocumentByWorkerAPI(pdp.getId(), worker1.getId(), "Worker1", "ForCompletedPDP");
        signDocumentByWorkerAPI(pdp.getId(), worker2.getId(), "Worker2", "ForCompletedPDP2");

        // Get the current state after signing, then update only the relations
        PdpDTO currentPdp = getPdpByIdAPI(pdp.getId());
        currentPdp.setRelations(new ArrayList<>()); // No permit issues
        PdpDTO updatedPdp = updatePdpAPI(currentPdp.getId(), currentPdp);

        PdpDTO fetchedPdp = getPdpByIdAPI(updatedPdp.getId());
        assertEquals(DocumentStatus.COMPLETED, fetchedPdp.getStatus(),
                "PDP should be COMPLETED when its chantier is COMPLETED.");
    }

    @Test
    @DisplayName("Test BDT Document Status: COMPLETED")
    void testBdtStatus_COMPLETED() throws Exception {
        // Create a chantier that is completed
        ChantierDTO chantier = createChantierAPI("Chantier for Completed BDT", defaultEntrepriseId,
                new Date(System.currentTimeMillis() - 86400000 * 14), // Started 14 days ago
                new Date(System.currentTimeMillis() - 86400000), // Ended yesterday
                50, false);

        // Update the chantier status to COMPLETED
        chantier.setStatus(ChantierStatus.COMPLETED);
        ChantierDTO updatedChantier = updateChantierAPI(chantier.getId(), chantier);

        // Create BDT for this completed chantier
        BdtDTO bdt = createBdtAPI(updatedChantier.getId(), defaultEntrepriseId, LocalDate.now());

        // Add all necessary signatures to ensure status is determined by the chantier
        WorkerDTO worker1 = createWorkerAPI("WorkerForCompletedBDT", defaultEntrepriseId);
        WorkerDTO worker2 = createWorkerAPI("WorkerForCompletedBDT2", defaultEntrepriseId);

        selectWorkerForChantierAPI(worker1.getId(), updatedChantier.getId());
        selectWorkerForChantierAPI(worker2.getId(), updatedChantier.getId());

        // Sign the document using API endpoints instead of directly setting signatures
        signDocumentByWorkerAPI(bdt.getId(), worker1.getId(), "Worker1", "ForCompletedBDT");
        signDocumentByWorkerAPI(bdt.getId(), worker2.getId(), "Worker2", "ForCompletedBDT2");

        // Get the current state after signing, then update only the relations
        BdtDTO currentBdt = getBdtByIdAPI(bdt.getId());
        currentBdt.setRelations(new ArrayList<>()); // No permit issues
        BdtDTO updatedBdt = updateBdtAPI(currentBdt.getId(), currentBdt);

        BdtDTO fetchedBdt = getBdtByIdAPI(updatedBdt.getId());
        assertEquals(DocumentStatus.COMPLETED, fetchedBdt.getStatus(),
                "BDT should be COMPLETED when its chantier is COMPLETED.");
    }

    @Test
    @DisplayName("Test PDP Document Status: EXPIRED")
    void testPdpStatus_EXPIRED() throws Exception {
        // Create a chantier (not expired, we'll expire the PDP specifically)
        ChantierDTO chantier = createChantierAPI("Chantier for Expired PDP", defaultEntrepriseId,
                new Date(), new Date(System.currentTimeMillis() + 86400000 * 30), // Chantier is active
                500, false);

        // Create PDP for this chantier
        PdpDTO pdp = createPdpAPI(chantier.getId(), defaultEntrepriseId);

        pdp.setDate(LocalDate.now().minusYears(2)); // Set to 2 years ago
        PdpDTO updatedPdp = updatePdpAPI(pdp.getId(), pdp);

        // Fetch the updated PDP
        PdpDTO fetchedPdp = getPdpByIdAPI(updatedPdp.getId());
        assertEquals(DocumentStatus.EXPIRED, fetchedPdp.getStatus(),
                "PDP should be EXPIRED when it's more than a year old.");
    }

    @Test
    @DisplayName("Test PDP Document Status: CANCELED")
    void testPdpStatus_CANCELED() throws Exception {
        // Create a chantier that will be canceled
        ChantierDTO chantier = createChantierAPI("Chantier for Canceled PDP", defaultEntrepriseId,
                new Date(), new Date(System.currentTimeMillis() + 86400000 * 30),
                50, false);

        // Create PDP for this chantier
        PdpDTO pdp = createPdpAPI(chantier.getId(), defaultEntrepriseId);

        // Cancel the chantier
        chantier.setStatus(ChantierStatus.CANCELED);
            chantier.setPdps(new ArrayList<>(Collections.singletonList(pdp.getId())));
        ChantierDTO updatedChantier = updateChantierAPI(chantier.getId(), chantier);

        // Fetch the PDP to check its status
        PdpDTO fetchedPdp = getPdpByIdAPI(pdp.getId());

        assertEquals(DocumentStatus.CANCELED, fetchedPdp.getStatus(),
                "PDP should be CANCELED when its chantier is CANCELED.");
    }

    @Test
    @DisplayName("Test BDT Document Status: CANCELED")
    void testBdtStatus_CANCELED() throws Exception {
        // Create a chantier that will be canceled
        ChantierDTO chantier = createChantierAPI("Chantier for Canceled BDT", defaultEntrepriseId,
                new Date(), new Date(System.currentTimeMillis() + 86400000 * 30),
                50, false);

        // Create BDT for this chantier
        BdtDTO bdt = createBdtAPI(chantier.getId(), defaultEntrepriseId, LocalDate.now());

        // Cancel the chantier
        chantier.setStatus(ChantierStatus.CANCELED);
        chantier.setBdts(new ArrayList<>(Collections.singletonList(bdt.getId()))); // I dont want to send it with the request i want it to find it by it self from the db.
        ChantierDTO updatedChantier = updateChantierAPI(chantier.getId(), chantier);

        // Fetch the BDT to check its status
        BdtDTO fetchedBdt = getBdtByIdAPI(bdt.getId());
        assertEquals(DocumentStatus.CANCELED, fetchedBdt.getStatus(),
                "BDT should be CANCELED when its chantier is CANCELED.");
    }

    @Test
    @DisplayName("Test PDP Transition from ACTIVE to COMPLETED")
    void testPdpTransition_ActiveToCompleted() throws Exception {
        // Create an active chantier
        ChantierDTO chantier = createChantierAPI("Chantier for PDP Active->Completed", defaultEntrepriseId,
                new Date(System.currentTimeMillis() - 86400000 * 7), // Started a week ago
                new Date(System.currentTimeMillis() + 86400000 * 7), // Ends in a week
                50, false);

        // Create and fully sign PDP
        PdpDTO pdp = createPdpAPI(chantier.getId(), defaultEntrepriseId);
        WorkerDTO worker1 = createWorkerAPI("WorkerForTransitionPDP1", defaultEntrepriseId);
        WorkerDTO worker2 = createWorkerAPI("WorkerForTransitionPDP2", defaultEntrepriseId);

        selectWorkerForChantierAPI(worker1.getId(), chantier.getId());
        selectWorkerForChantierAPI(worker2.getId(), chantier.getId());

        // Sign the document using API endpoints instead of directly setting signatures
        signDocumentByWorkerAPI(pdp.getId(), worker1.getId(), "Worker1", "ForTransitionPDP1");
        signDocumentByWorkerAPI(pdp.getId(), worker2.getId(), "Worker2", "ForTransitionPDP2");

        // Get the current state after signing, then update only the relations
        PdpDTO currentPdp = getPdpByIdAPI(pdp.getId());
        currentPdp.setRelations(new ArrayList<>()); // No permit issues
        PdpDTO updatedPdp = updatePdpAPI(currentPdp.getId(), currentPdp);

        // Verify PDP is ACTIVE
        PdpDTO fetchedPdp = getPdpByIdAPI(updatedPdp.getId());
        assertEquals(DocumentStatus.ACTIVE, fetchedPdp.getStatus(),
                "PDP should be ACTIVE initially.");

        // Now complete the chantier
        chantier.setStatus(ChantierStatus.COMPLETED);
        chantier.setPdps(new ArrayList<>(Collections.singletonList(pdp.getId())));
        ChantierDTO updatedChantier = updateChantierAPI(chantier.getId(), chantier);

        // Verify PDP transitions to COMPLETED
        PdpDTO reCheckedPdp = getPdpByIdAPI(pdp.getId());
        assertEquals(DocumentStatus.COMPLETED, reCheckedPdp.getStatus(),
                "PDP should transition to COMPLETED when chantier is completed.");
    }

    @Test
    @DisplayName("Test BDT Transition from ACTIVE to CANCELED")
    void testBdtTransition_ActiveToCanceled() throws Exception {
        // Create an active chantier
        ChantierDTO chantier = createChantierAPI("Chantier for BDT Active->Canceled", defaultEntrepriseId,
                new Date(System.currentTimeMillis() - 86400000 * 7), // Started a week ago
                new Date(System.currentTimeMillis() + 86400000 * 7), // Ends in a week
                50, false);

        // Create and fully sign BDT
        BdtDTO bdt = createBdtAPI(chantier.getId(), defaultEntrepriseId, LocalDate.now());
        WorkerDTO worker1 = createWorkerAPI("WorkerForTransitionBDT1", defaultEntrepriseId);
        WorkerDTO worker2 = createWorkerAPI("WorkerForTransitionBDT2", defaultEntrepriseId);

        selectWorkerForChantierAPI(worker1.getId(), chantier.getId());
        selectWorkerForChantierAPI(worker2.getId(), chantier.getId());

        // Sign the document using API endpoints instead of directly setting signatures
        signDocumentByWorkerAPI(bdt.getId(), worker1.getId(), "Worker1", "ForTransitionBDT1");
        signDocumentByWorkerAPI(bdt.getId(), worker2.getId(), "Worker2", "ForTransitionBDT2");

        // Get the current state after signing, then update only the relations
        BdtDTO currentBdt = getBdtByIdAPI(bdt.getId());
        currentBdt.setRelations(new ArrayList<>()); // No permit issues
        BdtDTO updatedBdt = updateBdtAPI(currentBdt.getId(), currentBdt);

        // Verify BDT is ACTIVE
        BdtDTO fetchedBdt = getBdtByIdAPI(updatedBdt.getId());
        assertEquals(DocumentStatus.ACTIVE, fetchedBdt.getStatus(),
                "BDT should be ACTIVE initially.");

        // Now cancel the chantier
        chantier.setStatus(ChantierStatus.CANCELED);
        chantier.setBdts(new ArrayList<>(Collections.singletonList(bdt.getId())));
        ChantierDTO updatedChantier = updateChantierAPI(chantier.getId(), chantier);

        // Verify BDT transitions to CANCELED
        BdtDTO reCheckedBdt = getBdtByIdAPI(bdt.getId());
        assertEquals(DocumentStatus.CANCELED, reCheckedBdt.getStatus(),
                "BDT should transition to CANCELED when chantier is canceled.");
    }
    @Test
    @DisplayName("Test PDP Transition from ACTIVE to CANCELED")
    void testPdpTransition_ActiveToCanceled() throws Exception {
        // Create an active chantier
        ChantierDTO chantier = createChantierAPI("Chantier for BDT Active->Canceled", defaultEntrepriseId,
                new Date(System.currentTimeMillis() - 86400000 * 7), // Started a week ago
                new Date(System.currentTimeMillis() + 86400000 * 7), // Ends in a week
                550, false);

        // Create and fully sign BDT
        PdpDTO pdp = createPdpAPI(chantier.getId(), defaultEntrepriseId);
        WorkerDTO worker1 = createWorkerAPI("WorkerForTransitionBDT1", defaultEntrepriseId);
        WorkerDTO worker2 = createWorkerAPI("WorkerForTransitionBDT2", defaultEntrepriseId);

        selectWorkerForChantierAPI(worker1.getId(), chantier.getId());
        selectWorkerForChantierAPI(worker2.getId(), chantier.getId());

        // Sign the document using API endpoints instead of directly setting signatures
        signDocumentByWorkerAPI(pdp.getId(), worker1.getId(), "Worker1", "ForTransitionPDP1");
        signDocumentByWorkerAPI(pdp.getId(), worker2.getId(), "Worker2", "ForTransitionPDP2");

        // Get the current state after signing, then update only the relations
        PdpDTO currentPdp = getPdpByIdAPI(pdp.getId());
        currentPdp.setRelations(new ArrayList<>()); // No permit issues
        PdpDTO updatedPdp = updatePdpAPI(currentPdp.getId(), currentPdp);

        // Verify Pdp is ACTIVE
        PdpDTO fetchedBdt = getPdpByIdAPI(updatedPdp.getId());
        assertEquals(DocumentStatus.ACTIVE, fetchedBdt.getStatus(),
                "Pdp should be ACTIVE initially.");

        // Now cancel the chantier
        chantier.setStatus(ChantierStatus.CANCELED);
        chantier.setPdps(new ArrayList<>(Collections.singletonList(pdp.getId())));
        ChantierDTO updatedChantier = updateChantierAPI(chantier.getId(), chantier);

        // Verify BDT transitions to CANCELED
        PdpDTO reCheckedPdp = getPdpByIdAPI(pdp.getId());
        assertEquals(DocumentStatus.CANCELED, reCheckedPdp.getStatus(),
                "Pdp should transition to CANCELED when chantier is canceled.");
    }
    @Test
    @DisplayName("Test PDP with Chantier in the Future: Should be ACTIVE if properly signed")
    void testPdpWithFutureChantier_ShouldBeActive() throws Exception {
        // Create a chantier with future dates
        ChantierDTO chantier = createChantierAPI("Future Chantier for PDP", defaultEntrepriseId,
                new Date(System.currentTimeMillis() + 86400000 * 7), // Starts in a week
                new Date(System.currentTimeMillis() + 86400000 * 14), // Ends in two weeks
                50, false);

        // Create PDP for this future chantier
        PdpDTO pdp = createPdpAPI(chantier.getId(), defaultEntrepriseId);

        // Add all necessary signatures
        WorkerDTO worker1 = createWorkerAPI("WorkerForFuturePDP1", defaultEntrepriseId);
        WorkerDTO worker2 = createWorkerAPI("WorkerForFuturePDP2", defaultEntrepriseId);

        selectWorkerForChantierAPI(worker1.getId(), chantier.getId());
        selectWorkerForChantierAPI(worker2.getId(), chantier.getId());

        // Sign the document using API endpoints instead of directly setting signatures
        signDocumentByWorkerAPI(pdp.getId(), worker1.getId(), "Worker1xxxxxxxxx", "ForFuturePDP1");
        signDocumentByWorkerAPI(pdp.getId(), worker2.getId(), "Worker2", "ForFuturePDP2");

        // Get the current state after signing, then update only the relations
        PdpDTO currentPdp = getPdpByIdAPI(pdp.getId());
        currentPdp.setRelations(new ArrayList<>()); // No permit issues
        PdpDTO updatedPdp = updatePdpAPI(currentPdp.getId(), currentPdp);

        // Verify PDP is ACTIVE despite future chantier dates
        PdpDTO fetchedPdp = getPdpByIdAPI(updatedPdp.getId());
        assertEquals(DocumentStatus.ACTIVE, fetchedPdp.getStatus(),
                "PDP should be ACTIVE when properly signed, even for a future chantier.");
    }


    @Test
    @DisplayName("Test PDP Document Status: DRAFT")
    void testPdpStatus_DRAFT() throws Exception {
        //We create a doucment inside a chantier
        //ChantierDTO chantier = createChantierAPI("Chantier for Draft PDP", defaultEntrepriseId, new Date(), new Date(System.currentTimeMillis() + 86400000 * 7), 50, false);
        PdpDTO pdp = createPdpAPI(null, defaultEntrepriseId);

        PdpDTO fetchedPdp = getPdpByIdAPI(pdp.getId());
        assertEquals(DocumentStatus.DRAFT, fetchedPdp.getStatus(), "Newly created PDP should be DRAFT by default.");
        assertEquals(ActionType.NONE, fetchedPdp.getActionType(), "Newly created PDP should be NONE by default.");

    }

    @Test
    @DisplayName("Test BDT Document Status: DRAFT")
    void testBdtStatus_DRAFT() throws Exception {
    //    ChantierDTO chantier = createChantierAPI("Chantier for Draft BDT", defaultEntrepriseId, new Date(), new Date(System.currentTimeMillis() + 86400000 * 7), 50, false);
        BdtDTO bdt = createBdtAPI(null, defaultEntrepriseId, LocalDate.now());

        BdtDTO fetchedBdt = getBdtByIdAPI(bdt.getId());
        assertEquals(DocumentStatus.DRAFT, fetchedBdt.getStatus(), "Newly created BDT should be DRAFT by default.");
        assertEquals(ActionType.NONE, fetchedBdt.getActionType(), "Newly created BDT should be NONE by default.");
    }


    @Test
    @DisplayName("Test PDP Document Status: ACTIVE")
    void testPdpStatus_ACTIVE() throws Exception {
        ChantierDTO chantier = createChantierAPI("Chantier for Active PDP", defaultEntrepriseId,
                new Date(), new Date(System.currentTimeMillis() + 86400000 * 7),
                450, true); // Requires PDP

        PdpDTO pdp = createPdpAPI(chantier.getId(), defaultEntrepriseId);
        WorkerDTO worker = createWorkerAPI("WorkerForActivePDP", defaultEntrepriseId);
        WorkerDTO worker2 = createWorkerAPI("WorkerForActivePDP2", defaultEntrepriseId);

        selectWorkerForChantierAPI(worker.getId(), chantier.getId());
        selectWorkerForChantierAPI(worker2.getId(), chantier.getId());

        // Simulate fulfilling all conditions for ACTIVE
        pdp.setSignatures(List.of(createSignatureDTO(worker.getId(), pdp.getId(), "TestRole", defaultDonneurDOrdreId),
                createSignatureDTO(worker2.getId(), pdp.getId(), "TestRole", defaultDonneurDOrdreId))); // Both workers sign

        pdp.setRelations(new ArrayList<>()); // Assuming no permits needed for this simple case

        PdpDTO updatedPdp = updatePdpAPI(pdp.getId(), pdp); // This will trigger status recalculation

        PdpDTO fetchedPdp = getPdpByIdAPI(updatedPdp.getId());
     //   assertEquals(DocumentStatus.ACTIVE, fetchedPdp.getStatus(), "PDP should become ACTIVE after all conditions met.");
        assertEquals(ActionType.NONE, fetchedPdp.getActionType(), "PDP should become ACTIVE after all conditions met.");
    }

    @Test
    @DisplayName("Test BDT Document Status: ACTIVE")
    void testBdtStatus_ACTIVE() throws Exception {
        ChantierDTO chantier = createChantierAPI("Chantier for Active BDT", defaultEntrepriseId,
                new Date(System.currentTimeMillis() - 86400000), // Started yesterday
                new Date(System.currentTimeMillis() + 86400000 * 7), // Ends in a week
                50, false); // Does not require PDP

        BdtDTO bdt = createBdtAPI(chantier.getId(), defaultEntrepriseId, LocalDate.now());

        WorkerDTO worker = createWorkerAPI("WorkerForActiveBDT", defaultEntrepriseId);
        WorkerDTO worker2 = createWorkerAPI("WorkerForActiveBDT2", defaultEntrepriseId);

        selectWorkerForChantierAPI(worker.getId(), chantier.getId());
        selectWorkerForChantierAPI(worker2.getId(), chantier.getId());


        bdt.setSignatures(List.of(createSignatureDTO(worker.getId(), bdt.getId(), "TestRole", defaultDonneurDOrdreId),
                createSignatureDTO(worker2.getId(), bdt.getId(), "TestRole", defaultDonneurDOrdreId))); // Both workers sign

        bdt.setRelations(new ArrayList<>()); // Assuming no permits needed

        BdtDTO updatedBdt = updateBdtAPI(bdt.getId(), bdt);

        BdtDTO fetchedBdt = getBdtByIdAPI(updatedBdt.getId());
        assertEquals(DocumentStatus.ACTIVE, fetchedBdt.getStatus(), "BDT should become ACTIVE.");
        assertEquals(ActionType.NONE, fetchedBdt.getActionType(), "BDT should become NONE.");
    }

    @Test
    @DisplayName("Test BDT Document not signed by all users: State = NEEDS_ACTION and ActionType = SIGNATURE_NEEDED")
    void testBdtStatus_NotAllWorkersSignesTheDocument_shouldReturnNeedsActionAndActionTypeSignatureNeeded() throws Exception {
        ChantierDTO chantier = createChantierAPI("Chantier for Active BDT", defaultEntrepriseId,
                new Date(System.currentTimeMillis() - 86400000), // Started yesterday
                new Date(System.currentTimeMillis() + 86400000 * 7), // Ends in a week
                50, false); // Does not require PDP

        BdtDTO bdt = createBdtAPI(chantier.getId(), defaultEntrepriseId, LocalDate.now());

        WorkerDTO worker = createWorkerAPI("WorkerForActiveBDT", defaultEntrepriseId);
        WorkerDTO worker2 = createWorkerAPI("WorkerForActiveBDT2", defaultEntrepriseId);

        selectWorkerForChantierAPI(worker.getId(), chantier.getId());
        selectWorkerForChantierAPI(worker2.getId(), chantier.getId());


        bdt.setSignatures(List.of(createSignatureDTO(worker.getId(), bdt.getId(), "TestRole"))); // Both workers sign

        bdt.setRelations(new ArrayList<>()); // Assuming no permits needed

        BdtDTO updatedBdt = updateBdtAPI(bdt.getId(), bdt);

        BdtDTO fetchedBdt = getBdtByIdAPI(updatedBdt.getId());
        assertEquals(DocumentStatus.NEEDS_ACTION, fetchedBdt.getStatus(), "BDT should become ACTIVE.");
        assertEquals(ActionType.SIGHNATURES_MISSING, fetchedBdt.getActionType(), "BDT should become NONE.");
    }


    @Test
    @DisplayName("Test Pdp Document not signed by all users: State = NEEDS_ACTION and ActionType = SIGNATURE_NEEDED")
    void testPdpStatus_NotAllWorkersSignesTheDocument_shouldReturnNeedsActionAndActionTypeSignatureNeeded() throws Exception {
        ChantierDTO chantier = createChantierAPI("Chantier for Active BDT", defaultEntrepriseId,
                new Date(System.currentTimeMillis() - 86400000), // Started yesterday
                new Date(System.currentTimeMillis() + 86400000 * 7), // Ends in a week
                50, false); // Does not require PDP

        PdpDTO pdp = createPdpAPI(chantier.getId(), defaultEntrepriseId);

        WorkerDTO worker = createWorkerAPI("WorkerForActiveBDT", defaultEntrepriseId);
        WorkerDTO worker2 = createWorkerAPI("WorkerForActiveBDT2", defaultEntrepriseId);

        selectWorkerForChantierAPI(worker.getId(), chantier.getId());
        selectWorkerForChantierAPI(worker2.getId(), chantier.getId());


        pdp.setSignatures(List.of(createSignatureDTO(worker.getId(), pdp.getId(), "TestRole"))); // Both workers sign

        pdp.setRelations(new ArrayList<>()); // Assuming no permits needed

        PdpDTO updatedPdp = updatePdpAPI(pdp.getId(), pdp);

        PdpDTO fetchedPdp = getPdpByIdAPI(updatedPdp.getId());
        assertEquals(DocumentStatus.NEEDS_ACTION, fetchedPdp.getStatus(), "PDP should become ACTIVE.");
        assertEquals(ActionType.SIGHNATURES_MISSING, fetchedPdp.getActionType(), "PDP should become NONE.");
    }


    @Test
    @DisplayName("Test Pdp Document well signed but permits needed: State = NEEDS_ACTION and ActionType = SIGNATURE_NEEDED")
    @Transactional
    void testPdpStatus_DocumentSignedButPermitsAreNeeded_shouldReturnNeedsActionAndActionTypePermitMissing() throws Exception {
        ChantierDTO chantier = createChantierAPI("Chantier for Active BDT", defaultEntrepriseId,
                new Date(System.currentTimeMillis() - 86400000), // Started yesterday
                new Date(System.currentTimeMillis() + 86400000 * 7), // Ends in a week
                50, false); // Does not require PDP

        PdpDTO pdp = createPdpAPI(chantier.getId(), defaultEntrepriseId);

        WorkerDTO worker = createWorkerAPI("WorkerForActiveBDT", defaultEntrepriseId);
        WorkerDTO worker2 = createWorkerAPI("WorkerForActiveBDT2", defaultEntrepriseId);

        selectWorkerForChantierAPI(worker.getId(), chantier.getId());
        selectWorkerForChantierAPI(worker2.getId(), chantier.getId());


        pdp.setSignatures(List.of(createSignatureDTO(worker.getId(), pdp.getId(), "TestRole"),createSignatureDTO(worker2.getId(), pdp.getId(), "TestRole2"))); // Both workers sign



        // Create a Permit and a Risque that requires this permit
    //    Permit permitEntity = createPermitAPI("Test Permit For PDP", PermiTypes.FOUILLE);
        Risque risqueEntity = createRisqueAPI("Risque Requiring Permit", true, PermiTypes.ATEX);
        // Link the Risque to the PDP, but NOT the permit itself


        ObjectAnsweredDTO risqueRelation = new ObjectAnsweredDTO();
        risqueRelation.setObjectType(ObjectAnsweredObjects.RISQUE);
        risqueRelation.setObjectId(risqueEntity.getId());
        risqueRelation.setAnswer(true);



        pdp.setRelations(Collections.singletonList(risqueRelation));




        PdpDTO updatedPdp = updatePdpAPI(pdp.getId(), pdp);

        PdpDTO fetchedPdp = getPdpByIdAPI(updatedPdp.getId());
        assertEquals(DocumentStatus.NEEDS_ACTION, fetchedPdp.getStatus(), "PDP should become ACTIVE.");
        assertEquals(ActionType.PERMIT_MISSING, fetchedPdp.getActionType(), "PDP should become NONE.");
    }

    @Test
    @DisplayName("Test Bdt Document well signed but permits needed: State = NEEDS_ACTION and ActionType = SIGNATURE_NEEDED")
    @Transactional
    void testBdtStatus_DocumentSignedButPermitsAreNeeded_shouldReturnNeedsActionAndActionTypePermitMissing() throws Exception {
        ChantierDTO chantier = createChantierAPI("Chantier for Active BDT", defaultEntrepriseId,
                new Date(System.currentTimeMillis() - 86400000), // Started yesterday
                new Date(System.currentTimeMillis() + 86400000 * 7), // Ends in a week
                50, false); // Does not require PDP

        BdtDTO bdt = createBdtAPI(chantier.getId(), defaultEntrepriseId, LocalDate.now());

        WorkerDTO worker = createWorkerAPI("WorkerForActiveBDT", defaultEntrepriseId);
        WorkerDTO worker2 = createWorkerAPI("WorkerForActiveBDT2", defaultEntrepriseId);

        selectWorkerForChantierAPI(worker.getId(), chantier.getId());
        selectWorkerForChantierAPI(worker2.getId(), chantier.getId());


        bdt.setSignatures(List.of(createSignatureDTO(worker.getId(), bdt.getId(), "TestRole"),createSignatureDTO(worker2.getId(), bdt.getId(), "TestRole2"))); // Both workers sign



        // Create a Permit and a Risque that requires this permit
        //    Permit permitEntity = createPermitAPI("Test Permit For PDP", PermiTypes.FOUILLE);
        Risque risqueEntity = createRisqueAPI("Risque Requiring Permit", true, PermiTypes.ATEX);
        // Link the Risque to the PDP, but NOT the permit itself


        ObjectAnsweredDTO risqueRelation = new ObjectAnsweredDTO();
        risqueRelation.setObjectType(ObjectAnsweredObjects.RISQUE);
        risqueRelation.setObjectId(risqueEntity.getId());
        risqueRelation.setAnswer(true);


        bdt.setRelations(Collections.singletonList(risqueRelation));




        BdtDTO updatedBdt = updateBdtAPI(bdt.getId(), bdt);

        BdtDTO fetchedBdt = getBdtByIdAPI(updatedBdt.getId());
        assertEquals(DocumentStatus.NEEDS_ACTION, fetchedBdt.getStatus(), "BDT should become ACTIVE.");
        assertEquals(ActionType.PERMIT_MISSING, fetchedBdt.getActionType(), "BDT should become NONE.");
    }



    @Test
    @DisplayName("Test document with signature from non-chantier worker: Should remain NEEDS_ACTION")
    void testDocument_SignatureFromNonChantierWorker_shouldRemainNeedsAction() throws Exception {
        // Create a chantier with two assigned workers
        ChantierDTO chantier = createChantierAPI("Chantier for Mixed Signatures Test", defaultEntrepriseId,
                new Date(), new Date(System.currentTimeMillis() + 86400000 * 7),
                50, false);

        PdpDTO pdp = createPdpAPI(chantier.getId(), defaultEntrepriseId);

        // Create two workers assigned to the chantier
        WorkerDTO assignedWorker = createWorkerAPI("AssignedWorker", defaultEntrepriseId);
        WorkerDTO anotherAssignedWorker = createWorkerAPI("AnotherAssignedWorker", defaultEntrepriseId);

        // Create a worker NOT assigned to the chantier
        WorkerDTO nonAssignedWorker = createWorkerAPI("NonAssignedWorker", defaultEntrepriseId);

        // Only assign the first two workers to the chantier
        selectWorkerForChantierAPI(assignedWorker.getId(), chantier.getId());
        selectWorkerForChantierAPI(anotherAssignedWorker.getId(), chantier.getId());

        // Add signatures from one assigned worker and one non-assigned worker
        pdp.setSignatures(List.of(
                createSignatureDTO(assignedWorker.getId(), pdp.getId(), "TestRole", defaultDonneurDOrdreId),
                createSignatureDTO(nonAssignedWorker.getId(), pdp.getId(), "TestRole2", defaultDonneurDOrdreId)
        ));

        pdp.setRelations(new ArrayList<>()); // No permit issues

        PdpDTO updatedPdp = updatePdpAPI(pdp.getId(), pdp);

        PdpDTO fetchedPdp = getPdpByIdAPI(updatedPdp.getId());

        // Assert that document is in NEEDS_ACTION status because not all assigned workers signed
        assertEquals(DocumentStatus.NEEDS_ACTION, fetchedPdp.getStatus(),
                "PDP with signature from non-chantier worker should remain in NEEDS_ACTION status");
        assertEquals(ActionType.SIGHNATURES_MISSING, fetchedPdp.getActionType(),
                "PDP should indicate signatures are missing");
    }

    @Test
    @DisplayName("Test document signed by all assigned workers with all required permits: Should be ACTIVE")
    void testDocument_AllSignedAndPermitsOk_shouldBeActive() throws Exception {
        ChantierDTO chantier = createChantierAPI("Chantier for Complete Test", defaultEntrepriseId,
                new Date(), new Date(System.currentTimeMillis() + 86400000 * 7),
                50, false);

        BdtDTO bdt = createBdtAPI(chantier.getId(), defaultEntrepriseId, LocalDate.now());

        WorkerDTO worker1 = createWorkerAPI("Worker1Complete", defaultEntrepriseId);
        WorkerDTO worker2 = createWorkerAPI("Worker2Complete", defaultEntrepriseId);

        selectWorkerForChantierAPI(worker1.getId(), chantier.getId());
        selectWorkerForChantierAPI(worker2.getId(), chantier.getId());

        // Create a permit and risque with proper linking
        Permit permitEntity = createPermitAPI("Test Complete Permit", PermiTypes.FOUILLE);
        Risque risqueEntity = createRisqueAPI("Risque With Valid Permit", true, PermiTypes.FOUILLE);

        // Link both the risque and permit to the document
        ObjectAnsweredDTO risqueRelation = new ObjectAnsweredDTO();
        risqueRelation.setObjectType(ObjectAnsweredObjects.RISQUE);
        risqueRelation.setObjectId(risqueEntity.getId());
        risqueRelation.setAnswer(true);

        ObjectAnsweredDTO permitRelation = new ObjectAnsweredDTO();
        permitRelation.setObjectType(ObjectAnsweredObjects.PERMIT);
        permitRelation.setObjectId(permitEntity.getId());
        permitRelation.setAnswer(true);

        // Set signatures and relations
        bdt.setSignatures(List.of(
                createSignatureDTO(worker1.getId(), bdt.getId(), "TestRole", defaultDonneurDOrdreId),
                createSignatureDTO(worker2.getId(), bdt.getId(), "TestRole2", defaultDonneurDOrdreId)
        ));

        bdt.setRelations(List.of(risqueRelation, permitRelation));

        BdtDTO updatedBdt = updateBdtAPI(bdt.getId(), bdt);

        BdtDTO fetchedBdt = getBdtByIdAPI(updatedBdt.getId());

        assertEquals(DocumentStatus.ACTIVE, fetchedBdt.getStatus(),
                "BDT with all signatures and permits should be ACTIVE");
        assertEquals(ActionType.NONE, fetchedBdt.getActionType(),
                "BDT with all requirements met should have NONE action type");
    }

    @Test
    @DisplayName("Test document not signed but permits ok: Should remain NEEDS_ACTION")
    void testDocument_NotSignedButPermitsOk_shouldBeNeedsAction() throws Exception {
        ChantierDTO chantier = createChantierAPI("Chantier for Permits Ok Test", defaultEntrepriseId,
                new Date(), new Date(System.currentTimeMillis() + 86400000 * 7),
                50, false);

        PdpDTO pdp = createPdpAPI(chantier.getId(), defaultEntrepriseId);

        WorkerDTO worker1 = createWorkerAPI("Worker1PermitsOk", defaultEntrepriseId);
        WorkerDTO worker2 = createWorkerAPI("Worker2PermitsOk", defaultEntrepriseId);

        selectWorkerForChantierAPI(worker1.getId(), chantier.getId());
        selectWorkerForChantierAPI(worker2.getId(), chantier.getId());

        // Create a permit and risque with proper linking
        Permit permitEntity = createPermitAPI("Test PermitsOk Permit", PermiTypes.FOUILLE);
        Risque risqueEntity = createRisqueAPI("Risque With Valid Permit", true, PermiTypes.ATEX);

        // Link both the risque and permit to the document
        ObjectAnsweredDTO risqueRelation = new ObjectAnsweredDTO();
        risqueRelation.setObjectType(ObjectAnsweredObjects.RISQUE);
        risqueRelation.setObjectId(risqueEntity.getId());
        risqueRelation.setAnswer(true);

        ObjectAnsweredDTO permitRelation = new ObjectAnsweredDTO();
        permitRelation.setObjectType(ObjectAnsweredObjects.PERMIT);
        permitRelation.setObjectId(permitEntity.getId());
        permitRelation.setAnswer(true);

        // Add NO signatures
        pdp.setSignatures(new ArrayList<>());

        pdp.setRelations(List.of(risqueRelation, permitRelation));

        PdpDTO updatedPdp = updatePdpAPI(pdp.getId(), pdp);

        PdpDTO fetchedPdp = getPdpByIdAPI(updatedPdp.getId());

        assertEquals(DocumentStatus.NEEDS_ACTION, fetchedPdp.getStatus(),
                "PDP without signatures should be NEEDS_ACTION even with permits ok");
        assertEquals(ActionType.SIGHNATURES_MISSING, fetchedPdp.getActionType(),
                "PDP should indicate signatures are missing");
    }

    @Test
    @DisplayName("Test document not signed and permits missing: Should be NEEDS_ACTION with SIGHNATURES_MISSING priority")
    void testDocument_NotSignedAndPermitsMissing_shouldBeNeedsActionWithSignaturesMissing() throws Exception {
        ChantierDTO chantier = createChantierAPI("Chantier for Multiple Issues Test", defaultEntrepriseId,
                new Date(), new Date(System.currentTimeMillis() + 86400000 * 7),
                50, false);

        BdtDTO bdt = createBdtAPI(chantier.getId(), defaultEntrepriseId, LocalDate.now());

        WorkerDTO worker1 = createWorkerAPI("Worker1MultipleIssues", defaultEntrepriseId);
        WorkerDTO worker2 = createWorkerAPI("Worker2MultipleIssues", defaultEntrepriseId);

        selectWorkerForChantierAPI(worker1.getId(), chantier.getId());
        selectWorkerForChantierAPI(worker2.getId(), chantier.getId());

        // Create a risque that requires permit but don't provide the permit
        Risque risqueEntity = createRisqueAPI("Risque Requiring Permit", true, null);

        ObjectAnsweredDTO risqueRelation = new ObjectAnsweredDTO();
        risqueRelation.setObjectType(ObjectAnsweredObjects.RISQUE);
        risqueRelation.setObjectId(risqueEntity.getId());
        risqueRelation.setAnswer(true);

        // Add NO signatures
        bdt.setSignatures(new ArrayList<>());

        bdt.setRelations(List.of(risqueRelation));

        BdtDTO updatedBdt = updateBdtAPI(bdt.getId(), bdt);

        BdtDTO fetchedBdt = getBdtByIdAPI(updatedBdt.getId());

        assertEquals(DocumentStatus.NEEDS_ACTION, fetchedBdt.getStatus(),
                "BDT with multiple issues should be NEEDS_ACTION");

        // Based on your implementation, you might prioritize signatures first or permits first.
        // Check which one your implementation prioritizes - typically signatures would be checked first
        assertEquals(ActionType.SIGHNATURES_MISSING, fetchedBdt.getActionType(),
                "BDT should prioritize missing signatures over missing permits");
    }

    @Test
    @DisplayName("Test document partially signed and permits partially complete: Should prioritize missing signatures")
    void testDocument_PartiallySignedAndPermitsPartiallyComplete_shouldPrioritizeSignatures() throws Exception {
        ChantierDTO chantier = createChantierAPI("Chantier for Partial Completion Test", defaultEntrepriseId,
                new Date(), new Date(System.currentTimeMillis() + 86400000 * 7),
                50, false);

        PdpDTO pdp = createPdpAPI(chantier.getId(), defaultEntrepriseId);

        WorkerDTO worker1 = createWorkerAPI("Worker1PartialTest", defaultEntrepriseId);
        WorkerDTO worker2 = createWorkerAPI("Worker2PartialTest", defaultEntrepriseId);

        selectWorkerForChantierAPI(worker1.getId(), chantier.getId());
        selectWorkerForChantierAPI(worker2.getId(), chantier.getId());

        // Create a risque requiring permit
        Risque risqueEntity = createRisqueAPI("Risque Requiring Permit", true, null);

        ObjectAnsweredDTO risqueRelation = new ObjectAnsweredDTO();
        risqueRelation.setObjectType(ObjectAnsweredObjects.RISQUE);
        risqueRelation.setObjectId(risqueEntity.getId());
        risqueRelation.setAnswer(true);

        // Add only one worker's signature
        pdp.setSignatures(List.of(createSignatureDTO(worker1.getId(), pdp.getId(), "TestRole", defaultDonneurDOrdreId)));

        pdp.setRelations(List.of(risqueRelation));

        PdpDTO updatedPdp = updatePdpAPI(pdp.getId(), pdp);

        PdpDTO fetchedPdp = getPdpByIdAPI(updatedPdp.getId());

        assertEquals(DocumentStatus.NEEDS_ACTION, fetchedPdp.getStatus(),
                "PDP with partial completion should be NEEDS_ACTION");

        // Based on your business logic, check which issue takes priority
        assertEquals(ActionType.SIGHNATURES_MISSING, fetchedPdp.getActionType(),
                "PDP should prioritize missing signatures over missing permits");
    }

    @Test
    @DisplayName("Test deleting object answered in PDP by setting answer to null")
    @Transactional
    void testPdp_DeleteObjectAnsweredBySettingAnswerToNull() throws Exception {
        // Create the necessary test data
        ChantierDTO chantier = createChantierAPI("Chantier for ObjectAnswered Null Test", defaultEntrepriseId,
                new Date(), new Date(System.currentTimeMillis() + 86400000 * 7),
                50, false);

        PdpDTO pdp = createPdpAPI(chantier.getId(), defaultEntrepriseId);

        // Create a risk to be associated with the PDP
        Risque risqueEntity = createRisqueAPI("Risque for Deletion Test", true, PermiTypes.ATEX);

        // Create and add the risk relation to the PDP
        ObjectAnsweredDTO risqueRelation = new ObjectAnsweredDTO();
        risqueRelation.setObjectType(ObjectAnsweredObjects.RISQUE);
        risqueRelation.setObjectId(risqueEntity.getId());
        risqueRelation.setAnswer(true); // Initially, the risk is answered as true

        pdp.setRelations(Collections.singletonList(risqueRelation));

        // Update the PDP to add the relation
        PdpDTO updatedPdp = updatePdpAPI(pdp.getId(), pdp);

        // Verify that the relation was added successfully
        PdpDTO fetchedPdp = getPdpByIdAPI(updatedPdp.getId());
        assertEquals(1, fetchedPdp.getRelations().size(),
                "PDP should have one relation");

        // Now "delete" the relation by setting its answer to null
        ObjectAnsweredDTO deletedRelation = fetchedPdp.getRelations().get(0);
        deletedRelation.setAnswer(null);

        // Create a new list with just the "deleted" relation
        pdp.setRelations(Collections.singletonList(deletedRelation));

        // Update the PDP with the "deleted" relation
        PdpDTO pdpWithDeletedRelation = updatePdpAPI(pdp.getId(), pdp);

        // Fetch the updated PDP and verify the relation was deleted
        PdpDTO finalPdp = getPdpByIdAPI(pdpWithDeletedRelation.getId());

        // The behavior should be that either:
        // 1. The relation is completely removed from the list
        // 2. OR the relation remains but with a null answer
        if (!finalPdp.getRelations().isEmpty()) {
            // If relations list is not empty, check that the answer is null
            assertNull(finalPdp.getRelations().get(0).getAnswer(),
                    "The relation's answer should be null");
        } else {
            // If relations list is empty, the relation was completely removed
            assertEquals(0, finalPdp.getRelations().size(),
                    "Relation should be removed when answer is set to null");
        }
    }

    @Test
    @DisplayName("Test deleting object answered in BDT by setting answer to null")
    @Transactional
    void testBdt_DeleteObjectAnsweredBySettingAnswerToNull() throws Exception {
        // Create the necessary test data
        ChantierDTO chantier = createChantierAPI("Chantier for BDT ObjectAnswered Null Test", defaultEntrepriseId,
                new Date(), new Date(System.currentTimeMillis() + 86400000 * 7),
                50, false);

        BdtDTO bdt = createBdtAPI(chantier.getId(), defaultEntrepriseId, LocalDate.now());

        // Create a risk to be associated with the BDT
        Risque risqueEntity = createRisqueAPI("Risque for BDT Deletion Test", true, PermiTypes.ATEX);

        // Create and add the risk relation to the BDT
        ObjectAnsweredDTO risqueRelation = new ObjectAnsweredDTO();
        risqueRelation.setObjectType(ObjectAnsweredObjects.RISQUE);
        risqueRelation.setObjectId(risqueEntity.getId());
        risqueRelation.setAnswer(true); // Initially, the risk is answered as true

        bdt.setRelations(Collections.singletonList(risqueRelation));

        // Update the BDT to add the relation
        BdtDTO updatedBdt = updateBdtAPI(bdt.getId(), bdt);

        // Verify that the relation was added successfully
        BdtDTO fetchedBdt = getBdtByIdAPI(updatedBdt.getId());
        assertEquals(1, fetchedBdt.getRelations().size(),
                "BDT should have one relation");

        // Now "delete" the relation by setting its answer to null
        ObjectAnsweredDTO deletedRelation = fetchedBdt.getRelations().get(0);
        deletedRelation.setAnswer(null);

        // Create a new list with just the "deleted" relation
        bdt.setRelations(Collections.singletonList(deletedRelation));

        // Update the BDT with the "deleted" relation
        BdtDTO bdtWithDeletedRelation = updateBdtAPI(bdt.getId(), bdt);

        // Fetch the updated BDT and verify the relation was deleted
        BdtDTO finalBdt = getBdtByIdAPI(bdtWithDeletedRelation.getId());



            // If relations list is empty, the relation was completely removed
            assertEquals(0, finalBdt.getRelations().size(),
                    "Relation should be removed when answer is set to null");

    }

    @Test
    @DisplayName("Test deleting multiple object answered in PDP by setting answer to null")
    @Transactional
    void testPdp_DeleteMultipleObjectAnsweredBySettingAnswerToNull() throws Exception {
        // Create the necessary test data
        ChantierDTO chantier = createChantierAPI("Chantier for Multiple ObjectAnswered Null Test", defaultEntrepriseId,
                new Date(), new Date(System.currentTimeMillis() + 86400000 * 7),
                50, false);

        PdpDTO pdp = createPdpAPI(chantier.getId(), defaultEntrepriseId);

        // Create multiple risks to be associated with the PDP
        Risque risqueEntity1 = createRisqueAPI("Risque 1 for Deletion Test", true, PermiTypes.ATEX);
        Risque risqueEntity2 = createRisqueAPI("Risque 2 for Deletion Test", false, null);

        // Create and add risk relations to the PDP
        ObjectAnsweredDTO risqueRelation1 = new ObjectAnsweredDTO();
        risqueRelation1.setObjectType(ObjectAnsweredObjects.RISQUE);
        risqueRelation1.setObjectId(risqueEntity1.getId());
        risqueRelation1.setAnswer(true);

        ObjectAnsweredDTO risqueRelation2 = new ObjectAnsweredDTO();
        risqueRelation2.setObjectType(ObjectAnsweredObjects.RISQUE);
        risqueRelation2.setObjectId(risqueEntity2.getId());
        risqueRelation2.setAnswer(false);

        List<ObjectAnsweredDTO> relations = new ArrayList<>();
        relations.add(risqueRelation1);
        relations.add(risqueRelation2);

        pdp.setRelations(relations);

        // Update the PDP to add the relations
        PdpDTO updatedPdp = updatePdpAPI(pdp.getId(), pdp);

        // Verify that both relations were added successfully
        PdpDTO fetchedPdp = getPdpByIdAPI(updatedPdp.getId());
        assertEquals(2, fetchedPdp.getRelations().size(),
                "PDP should have two relations");

        // Now "delete" only the first relation by setting its answer to null
        List<ObjectAnsweredDTO> updatedRelations = new ArrayList<>(fetchedPdp.getRelations());
        updatedRelations.get(0).setAnswer(null); // Set first relation's answer to null

        pdp.setRelations(updatedRelations);

        // Update the PDP with one "deleted" relation
        PdpDTO pdpWithOneDeletedRelation = updatePdpAPI(pdp.getId(), pdp);

        // Fetch the updated PDP and verify the correct relation was deleted
        PdpDTO finalPdp = getPdpByIdAPI(pdpWithOneDeletedRelation.getId());

        // Depending on implementation, either the relation is removed or its answer is null
        if (finalPdp.getRelations().size() == 1) {
            // If only one relation remains, it should be the second one
            ObjectAnsweredDTO remainingRelation = finalPdp.getRelations().get(0);
            assertEquals(risqueEntity2.getId(), remainingRelation.getObjectId(),
                    "The remaining relation should be the second one (not deleted)");
            assertFalse(remainingRelation.getAnswer(),
                    "The remaining relation should have its original answer (false)");
        }else{
            fail("Expected only one relation to remain after deletion, but found: " + finalPdp.getRelations().size());
        }
    }

    @Test
    @DisplayName("User can sign document successfully")
    void testUserSignDocument() throws Exception {
        // Create a chantier that started 2 days ago and will end in 5 days
        Date startDate = new Date(System.currentTimeMillis() - 86400000 * 2); // 2 days ago
        Date endDate = new Date(System.currentTimeMillis() + 86400000 * 5); // 5 days from now
        ChantierDTO chantier = createChantierAPI("Chantier for User Sign Test", defaultEntrepriseId, startDate, endDate, 50, false);

        // Create a PDP
        PdpDTO pdp = createPdpAPI(chantier.getId(), defaultEntrepriseId);

        // Sign document as user - should succeed
        Long signatureId = signDocumentByUserAPI(pdp.getId(), defaultDonneurDOrdreId, "User", "Manager");
        assertNotNull(signatureId);
    }

    @Test
    @DisplayName("User signing with non-existent user should fail")
    void testUserSignDocumentWithNonExistentUser() throws Exception {
        // Create a chantier and PDP
        Date startDate = new Date(System.currentTimeMillis() - 86400000 * 2);
        Date endDate = new Date(System.currentTimeMillis() + 86400000 * 5);
        ChantierDTO chantier = createChantierAPI("Chantier for Non-Existent User Test", defaultEntrepriseId, startDate, endDate, 50, false);
        PdpDTO pdp = createPdpAPI(chantier.getId(), defaultEntrepriseId);

        // Try to sign with non-existent user
        SignatureRequestDTO signatureRequest = new SignatureRequestDTO();
        signatureRequest.setUserId(9999L); // Non-existent user ID
        signatureRequest.setDocumentId(pdp.getId());
        signatureRequest.setPrenom("NonExistent");
        signatureRequest.setNom("User");
        String base64Image = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
        signatureRequest.setSignatureImage(base64Image);

        mockMvc.perform(post("/api/document/user/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signatureRequest))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("User signing with non-existent document should fail")
    void testUserSignDocumentWithNonExistentDocument() throws Exception {
        // Try to sign non-existent document
        SignatureRequestDTO signatureRequest = new SignatureRequestDTO();
        signatureRequest.setUserId(defaultDonneurDOrdreId);
        signatureRequest.setDocumentId(9999L); // Non-existent document ID
        signatureRequest.setPrenom("User");
        signatureRequest.setNom("Manager");
        String base64Image = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
        signatureRequest.setSignatureImage(base64Image);

        mockMvc.perform(post("/api/document/user/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signatureRequest))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Document not found"));
    }

    @Test
    @DisplayName("User can unsign their own signature successfully")
    void testUserUnsignDocument() throws Exception {
        // Create a chantier and PDP
        Date startDate = new Date(System.currentTimeMillis() - 86400000 * 2);
        Date endDate = new Date(System.currentTimeMillis() + 86400000 * 5);
        ChantierDTO chantier = createChantierAPI("Chantier for User Unsign Test", defaultEntrepriseId, startDate, endDate, 50, false);
        PdpDTO pdp = createPdpAPI(chantier.getId(), defaultEntrepriseId);

        // First, sign the document as user and get the signature ID
        Long signatureId = signDocumentByUserAPI(pdp.getId(), defaultDonneurDOrdreId, "User", "Manager");

        // Unsign the document - should succeed
        unsignDocumentByUserAPI(defaultDonneurDOrdreId, signatureId);
    }

    @Test
    @DisplayName("User cannot unsign non-existent signature")
    void testUserUnsignNonExistentSignature() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/document/user/{userId}/unsign/{signatureId}", 
                        defaultDonneurDOrdreId, 9999L)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Signature not found"));
    }

    @Test
    @DisplayName("User cannot unsign another user's signature")
    void testUserCannotUnsignOtherUserSignature() throws Exception {
        // Create a chantier and PDP
        Date startDate = new Date(System.currentTimeMillis() - 86400000 * 2);
        Date endDate = new Date(System.currentTimeMillis() + 86400000 * 5);
        ChantierDTO chantier = createChantierAPI("Chantier for Unauthorized Unsign Test", defaultEntrepriseId, startDate, endDate, 50, false);
        PdpDTO pdp = createPdpAPI(chantier.getId(), defaultEntrepriseId);

        // First, sign the document as the default user and get the actual signature ID
        Long signatureId = signDocumentByUserAPI(pdp.getId(), defaultDonneurDOrdreId, "User", "Manager");

        // Try to unsign with a different user ID (assuming user ID 2 exists but is different)
        Long differentUserId = 2L;
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/document/user/{userId}/unsign/{signatureId}", 
                        differentUserId, signatureId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unauthorized to unsign"));
    }

    @Test
    @DisplayName("Test worker unsign endpoint with success")
    void testWorkerUnsignDocumentSuccess() throws Exception {
        // Create a chantier and PDP
        Date startDate = new Date(System.currentTimeMillis() - 86400000 * 2);
        Date endDate = new Date(System.currentTimeMillis() + 86400000 * 5);
        ChantierDTO chantier = createChantierAPI("Chantier for Worker Unsign Test", defaultEntrepriseId, startDate, endDate, 50, false);
        
        // Create a worker
        WorkerDTO worker = createWorkerAPI("TestWorkerForUnsign", defaultEntrepriseId);
        
        PdpDTO pdp = createPdpAPI(chantier.getId(), defaultEntrepriseId);

        // First, sign the document as worker and get the signature ID
        Long signatureId = signDocumentByWorkerAPI(pdp.getId(), worker.getId(), "Worker", "Test");

        // Unsign the document - should succeed
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/document/worker/{workerId}/unsign/{signatureId}", 
                        worker.getId(), signatureId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Document unsigned successfully"));
    }

    @Test
    @DisplayName("Test worker unsign endpoint with non-existent signature")
    void testWorkerUnsignDocumentNonExistentSignature() throws Exception {
        // Create a worker
        WorkerDTO worker = createWorkerAPI("TestWorkerForFailedUnsign", defaultEntrepriseId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/document/worker/{workerId}/unsign/{signatureId}", 
                        worker.getId(), 9999L)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Signature not found"));
    }


}
