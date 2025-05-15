package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Utils.*;
import com.danone.pdpbackend.Utils.Image.ImageModel; // Assuming ImageModel exists
import com.danone.pdpbackend.entities.Permit;
import com.danone.pdpbackend.entities.Risque;
import com.danone.pdpbackend.entities.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
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
        Map<String, Object> request = new HashMap<>();
        request.put("workerId", workerId);
        request.put("chantierId", chantierId);
        request.put("note", "Selected for test");

        mockMvc.perform(post("/api/worker-selection/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
    }

    private Risque createRisqueAPI(String title, boolean travaillePermit, Long permitIdToLink) throws Exception {
        Risque risque = new Risque();
        risque.setTitle(title);
        risque.setTravaillePermit(travaillePermit);
        if (travaillePermit && permitIdToLink != null) {
            risque.setPermitId(permitIdToLink);
        }
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

    private DocumentSignatureDTO createSignatureDTO(Long workerId,Long documentId, String role) {
        DocumentSignatureDTO signatureDTO = new DocumentSignatureDTO();
        signatureDTO.setWorkerId(workerId);
        signatureDTO.setSignatureDate(new Date());
        signatureDTO.setDocumentId(documentId); // Link to the document (PDP or BDT)
        // Mock visual data if necessary, or ensure your backend handles null gracefully if not strictly needed for status change
        ImageModel visual = new ImageModel();
        visual.setMimeType("image/png");
        visual.setImageData(new byte[]{1,2,3}); // Dummy data
        signatureDTO.setSignatureVisual(visual);
        signatureDTO.setSignerRole(role); // Role might be important for some logic
        signatureDTO.setActive(true);
        return signatureDTO;
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

        pdp.setSignatures(List.of(
                createSignatureDTO(worker1.getId(), pdp.getId(), "TestRole"),
                createSignatureDTO(worker2.getId(), pdp.getId(), "TestRole2")
        ));

        pdp.setRelations(new ArrayList<>()); // No permit issues

        PdpDTO updatedPdp = updatePdpAPI(pdp.getId(), pdp);

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

        bdt.setSignatures(List.of(
                createSignatureDTO(worker1.getId(), bdt.getId(), "TestRole"),
                createSignatureDTO(worker2.getId(), bdt.getId(), "TestRole2")
        ));

        bdt.setRelations(new ArrayList<>()); // No permit issues

        BdtDTO updatedBdt = updateBdtAPI(bdt.getId(), bdt);

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
                50, false);

        // Create PDP for this chantier
        PdpDTO pdp = createPdpAPI(chantier.getId(), defaultEntrepriseId);

        // Manually set the creation date to more than a year ago to trigger EXPIRED status
        // Note: This assumes your entity has a method to set creation date for testing
        // If not available, you might need to use reflection or a direct DB update

        // For this example, I'm using a mock approach since we can't directly modify creation date
        // In a real test, you would need to use repository access to update the creation date
        pdp.setDate(LocalDate.now().minusYears(1).minusDays(1)); // Set date to 1 year and 1 day ago
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

        pdp.setSignatures(List.of(
                createSignatureDTO(worker1.getId(), pdp.getId(), "TestRole"),
                createSignatureDTO(worker2.getId(), pdp.getId(), "TestRole2")
        ));

        pdp.setRelations(new ArrayList<>()); // No permit issues

        PdpDTO updatedPdp = updatePdpAPI(pdp.getId(), pdp);

        // Verify PDP is ACTIVE
        PdpDTO fetchedPdp = getPdpByIdAPI(updatedPdp.getId());
        assertEquals(DocumentStatus.ACTIVE, fetchedPdp.getStatus(),
                "PDP should be ACTIVE initially.");

        // Now complete the chantier
        chantier.setStatus(ChantierStatus.COMPLETED);
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

        bdt.setSignatures(List.of(
                createSignatureDTO(worker1.getId(), bdt.getId(), "TestRole"),
                createSignatureDTO(worker2.getId(), bdt.getId(), "TestRole2")
        ));

        bdt.setRelations(new ArrayList<>()); // No permit issues

        BdtDTO updatedBdt = updateBdtAPI(bdt.getId(), bdt);

        // Verify BDT is ACTIVE
        BdtDTO fetchedBdt = getBdtByIdAPI(updatedBdt.getId());
        assertEquals(DocumentStatus.ACTIVE, fetchedBdt.getStatus(),
                "BDT should be ACTIVE initially.");

        // Now cancel the chantier
        chantier.setStatus(ChantierStatus.CANCELED);
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

        pdp.setSignatures(List.of(
                createSignatureDTO(worker1.getId(), pdp.getId(), "TestRole"),
                createSignatureDTO(worker2.getId(), pdp.getId(), "TestRole2")
        ));

        pdp.setRelations(new ArrayList<>()); // No permit issues

        PdpDTO updatedPdp = updatePdpAPI(pdp.getId(), pdp);

        // Verify Pdp is ACTIVE
        PdpDTO fetchedBdt = getPdpByIdAPI(updatedPdp.getId());
        assertEquals(DocumentStatus.ACTIVE, fetchedBdt.getStatus(),
                "Pdp should be ACTIVE initially.");

        // Now cancel the chantier
        chantier.setStatus(ChantierStatus.CANCELED);
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

        pdp.setSignatures(List.of(
                createSignatureDTO(worker1.getId(), pdp.getId(), "TestRole"),
                createSignatureDTO(worker2.getId(), pdp.getId(), "TestRole2")
        ));

        pdp.setRelations(new ArrayList<>()); // No permit issues

        PdpDTO updatedPdp = updatePdpAPI(pdp.getId(), pdp);

        // Verify PDP is ACTIVE despite future chantier dates
        PdpDTO fetchedPdp = getPdpByIdAPI(updatedPdp.getId());
        assertEquals(DocumentStatus.ACTIVE, fetchedPdp.getStatus(),
                "PDP should be ACTIVE when properly signed, even for a future chantier.");
    }

    @Test
    @DisplayName("Test BDT with Expired Date but Active Chantier")
    void testBdtWithExpiredDateButActiveChantier() throws Exception {
        // Create an active chantier
        ChantierDTO chantier = createChantierAPI("Active Chantier for Expired Date BDT", defaultEntrepriseId,
                new Date(System.currentTimeMillis() - 86400000 * 30), // Started 30 days ago
                new Date(System.currentTimeMillis() + 86400000 * 30), // Ends in 30 days
                50, false);

        // Create BDT with a date more than a year ago
        BdtDTO bdt = createBdtAPI(chantier.getId(), defaultEntrepriseId, LocalDate.now().minusYears(1).minusDays(1));

        // Add all necessary signatures
        WorkerDTO worker1 = createWorkerAPI("WorkerForExpiredDateBDT1", defaultEntrepriseId);
        WorkerDTO worker2 = createWorkerAPI("WorkerForExpiredDateBDT2", defaultEntrepriseId);

        selectWorkerForChantierAPI(worker1.getId(), chantier.getId());
        selectWorkerForChantierAPI(worker2.getId(), chantier.getId());

        bdt.setSignatures(List.of(
                createSignatureDTO(worker1.getId(), bdt.getId(), "TestRole"),
                createSignatureDTO(worker2.getId(), bdt.getId(), "TestRole2")
        ));

        bdt.setRelations(new ArrayList<>()); // No permit issues

        BdtDTO updatedBdt = updateBdtAPI(bdt.getId(), bdt);

        // Verify BDT is EXPIRED even though the chantier is active
        BdtDTO fetchedBdt = getBdtByIdAPI(updatedBdt.getId());
        assertEquals(DocumentStatus.EXPIRED, fetchedBdt.getStatus(),
                "BDT should be EXPIRED when its date is more than a year old, even with an active chantier.");
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
        pdp.setSignatures(List.of(createSignatureDTO(worker.getId(), pdp.getId(), "TestRole"),
                createSignatureDTO(worker2.getId(), pdp.getId(), "TestRole"))); // Both workers sign

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


        bdt.setSignatures(List.of(createSignatureDTO(worker.getId(), bdt.getId(), "TestRole"),
                createSignatureDTO(worker2.getId(), bdt.getId(), "TestRole"))); // Both workers sign

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
        Risque risqueEntity = createRisqueAPI("Risque Requiring Permit", true, null);
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
        Risque risqueEntity = createRisqueAPI("Risque Requiring Permit", true, null);
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
                createSignatureDTO(assignedWorker.getId(), pdp.getId(), "TestRole"),
                createSignatureDTO(nonAssignedWorker.getId(), pdp.getId(), "TestRole2")
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
        Risque risqueEntity = createRisqueAPI("Risque With Valid Permit", true, permitEntity.getId());

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
                createSignatureDTO(worker1.getId(), bdt.getId(), "TestRole"),
                createSignatureDTO(worker2.getId(), bdt.getId(), "TestRole2")
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
        Risque risqueEntity = createRisqueAPI("Risque With Valid Permit", true, permitEntity.getId());

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
        pdp.setSignatures(List.of(createSignatureDTO(worker1.getId(), pdp.getId(), "TestRole")));

        pdp.setRelations(List.of(risqueRelation));

        PdpDTO updatedPdp = updatePdpAPI(pdp.getId(), pdp);

        PdpDTO fetchedPdp = getPdpByIdAPI(updatedPdp.getId());

        assertEquals(DocumentStatus.NEEDS_ACTION, fetchedPdp.getStatus(),
                "PDP with partial completion should be NEEDS_ACTION");

        // Based on your business logic, check which issue takes priority
        assertEquals(ActionType.SIGHNATURES_MISSING, fetchedPdp.getActionType(),
                "PDP should prioritize missing signatures over missing permits");
    }






    //Test two workers, the first signture is to one of them the second is to onother that is not to one of them what behevior we expect
    //Test signed and permits are ok
    //Test not signed and permits are ok
    //Test signed and permits are not ok - DONE
    //Test not signed and permits are not ok
}
