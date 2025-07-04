package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Controller.auth.AuthenticationRequest;
import com.danone.pdpbackend.Controller.auth.AuthenticationResponse;
import com.danone.pdpbackend.Utils.ApiResponse;
// Import DTOs/Entities needed for setup requests & verification
import com.danone.pdpbackend.entities.Chantier;
import com.danone.pdpbackend.entities.Entreprise;
import com.danone.pdpbackend.entities.Worker;
import com.danone.pdpbackend.entities.dto.ChantierDTO; // Use if the endpoint returns DTO
import com.danone.pdpbackend.entities.dto.WorkerChantierSelectionDTO; // Use for verification
import com.danone.pdpbackend.entities.dto.WorkerDTO; // Use for verification

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
// Remove MethodOrderer and Order imports
// import org.junit.jupiter.api.MethodOrderer;
// import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.TestMethodOrder; // Remove this
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;


import java.util.HashMap; // For request bodies
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rollback DB changes after each test
// @TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Removed order dependency
class WorkerSelectionControllerIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(WorkerSelectionControllerIntegrationTest.class);
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String authToken;
    private Long testChantierId;
    private Long testEntrepriseId;
    private Long testWorkerId;

    // Remove static DTO
    // static WorkerChantierSelectionDTO workerChantierSelectionDTO = new WorkerChantierSelectionDTO();

    // Helper method to create resources via API calls and extract ID
    private <T> Long createResourceViaApi(String url, Object requestDto, TypeReference<ApiResponse<T>> responseTypeRef, String idFieldName) throws Exception {
        // Ensure auth token is available before making the call
        if (this.authToken == null) {
            throw new IllegalStateException("Auth token not available for API call to " + url);
        }

        MvcResult result = mockMvc.perform(post(url)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data." + idFieldName).isNotEmpty())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse<T> apiResponse = objectMapper.readValue(jsonResponse, responseTypeRef);
        T data = apiResponse.getData();
        assertNotNull(data, "Data in API response should not be null for URL: " + url);

        try {
            Object idObj;
            if (data instanceof Map) {
                idObj = ((Map<?, ?>) data).get(idFieldName);
            } else {
                // Using reflection to get ID, assuming standard getter or field name
                try {
                    java.lang.reflect.Method getIdMethod = data.getClass().getMethod("getId");
                    idObj = getIdMethod.invoke(data);
                } catch (NoSuchMethodException nsme) {
                    java.lang.reflect.Field idField = data.getClass().getDeclaredField(idFieldName);
                    idField.setAccessible(true);
                    idObj = idField.get(data);
                }
            }

            assertNotNull(idObj, "ID field '" + idFieldName + "' should not be null in response data for URL: " + url);
            if (idObj instanceof Integer) {
                return ((Integer) idObj).longValue();
            }
            assertTrue(idObj instanceof Long, "ID field '" + idFieldName + "' should be Long or Integer for URL: " + url + " but was " + idObj.getClass().getName());
            return (Long) idObj;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get ID field '" + idFieldName + "' from response object for URL: " + url, e);
        }
    }

    // Helper method to perform the selection API call
    private ResultActions selectWorker(Long workerId, Long chantierId, String note) throws Exception {

        WorkerChantierSelectionDTO selectionDTO = new WorkerChantierSelectionDTO();
        selectionDTO.setWorker(workerId);
        selectionDTO.setChantier(chantierId);
        selectionDTO.setSelectionNote(note);

        return mockMvc.perform(post("/api/worker-selection/select")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(selectionDTO)));
    }

    // Helper method to perform the deselection API call
    private ResultActions deselectWorker(Long workerId, Long chantierId) throws Exception {


        WorkerChantierSelectionDTO selectionDTO = new WorkerChantierSelectionDTO();
        selectionDTO.setWorker(workerId);
        selectionDTO.setChantier(chantierId);

        return mockMvc.perform(post("/api/worker-selection/deselect")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(selectionDTO)));
    }


    @BeforeEach
    void setUp() throws Exception {
        // --- Token Fetching ---
        AuthenticationRequest loginRequest = new AuthenticationRequest("Yassin4", "Zaqwe123!"); // Use valid credentials
        MvcResult loginResult = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String loginResponseJson = loginResult.getResponse().getContentAsString();
        ApiResponse<AuthenticationResponse> apiResponse = objectMapper.readValue(loginResponseJson, new TypeReference<ApiResponse<AuthenticationResponse>>() {});
        this.authToken = apiResponse.getData().getToken();
        assertNotNull(this.authToken);


        // --- Create Prerequisite Data via API ---
        // 1. Create Entreprise
        Map<String, String> entrepriseData = new HashMap<>();
        entrepriseData.put("nom", "Selection Test EE " + System.nanoTime());
        this.testEntrepriseId = createResourceViaApi("/api/entreprise", entrepriseData, new TypeReference<ApiResponse<Entreprise>>() {}, "id");

        // 2. Create Worker (linked to Entreprise)
        WorkerDTO workerData = new WorkerDTO();
        workerData.setNom("SelectionTestWorker");
        workerData.setPrenom(""+System.nanoTime());

        workerData.setEntreprise(testEntrepriseId);
        this.testWorkerId = createResourceViaApi("/api/worker/", workerData, new TypeReference<ApiResponse<WorkerDTO>>() {}, "id");

        // 3. Create ChantierE
        Map<String, Object> chantierData = new HashMap<>();
        chantierData.put("nom", "Selection Test Chantier " + System.nanoTime());
        this.testChantierId = createResourceViaApi("/api/chantier/", chantierData, new TypeReference<ApiResponse<Chantier>>() {}, "id");
    }

    @Test
    void selectWorker_shouldReturnSelection() throws Exception {
        // Arrange
        String note = "Selected for testing";

        // Act & Assert
        selectWorker(testWorkerId, testChantierId, note) // Use helper
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Worker selected successfully"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.worker").value(testWorkerId))
                .andExpect(jsonPath("$.data.chantier").value(testChantierId))
                .andExpect(jsonPath("$.data.isSelected").value(true))
                .andExpect(jsonPath("$.data.selectionNote").value(note));
        // Removed static DTO update
    }

    @Test
    void getWorkersForChantier_shouldReturnSelectedWorker() throws Exception {
        // Arrange: Select the worker first within this test
        selectWorker(testWorkerId, testChantierId, "Selected for getWorkersForChantier test")
                .andExpect(status().isOk());

        // Act: Get the workers for the chantier
        mockMvc.perform(get("/api/worker-selection/chantier/{chantierId}/workers", testChantierId)
                        .header("Authorization", "Bearer " + authToken))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Workers fetched"))
                .andExpect(jsonPath("$.data").isArray())
                // Assert the specific worker is present
                .andExpect(jsonPath("$.data[?(@.id == %d)]", testWorkerId).exists())
                // Optionally assert more details if WorkerDTO is returned
                .andExpect(jsonPath("$.data[?(@.id == %d)].nom", testWorkerId).value("SelectionTestWorker"));
    }

    // --- New Tests ---

    @Test
    void getChantiersForWorker_shouldReturnSelectedChantier() throws Exception {
        // Arrange: Select the worker for the chantier first
        selectWorker(testWorkerId, testChantierId, "Selected for getChantiersForWorker test")
                .andExpect(status().isOk());
        // Act: Get chantiers for the worker
        mockMvc.perform(get("/api/worker-selection/worker/{workerId}/chantiers", testWorkerId)
                        .header("Authorization", "Bearer " + authToken))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Chantiers fetched"))
                .andExpect(jsonPath("$.data").isArray())
                // Assert the specific chantier is present
                .andExpect(jsonPath("$.data[?(@.id == %d)]", testChantierId).exists());
    }

    @Test
    void getSelectionsForChantier_shouldReturnSelectionDetails() throws Exception {
        // Arrange: Select the worker first
        String selectionNote = "Note for getSelections test";
        selectWorker(testWorkerId, testChantierId, selectionNote)
                .andExpect(status().isOk())
                // Extract the created selection ID if needed for later assertions, though checking worker/chantier IDs is often sufficient
                .andReturn();

        // Act: Get selections for the chantier
        mockMvc.perform(get("/api/worker-selection/chantier/{chantierId}/selections", testChantierId)
                        .header("Authorization", "Bearer " + authToken))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Chantiers fetched")) // Message might need update in Controller if inaccurate
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1)))) // Ensure at least one selection
                // Find the specific selection by worker and chantier ID within the array
                .andExpect(jsonPath("$.data[?(@.worker == %d && @.chantier == %d)]", testWorkerId, testChantierId).exists())
                .andExpect(jsonPath("$.data[?(@.worker == %d && @.chantier == %d)].isSelected", testWorkerId, testChantierId).value(true))
                .andExpect(jsonPath("$.data[?(@.worker == %d && @.chantier == %d)].selectionNote", testWorkerId, testChantierId).value(selectionNote));
    }

    @Test
    void deselectWorker_shouldMakeWorkerNotSelected() throws Exception {
        // Arrange: Select the worker first
        selectWorker(testWorkerId, testChantierId, "To be deselected")
                .andExpect(status().isOk());

        // Verify worker is initially present
        mockMvc.perform(get("/api/worker-selection/chantier/{chantierId}/workers", testChantierId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == %d)]", testWorkerId).exists());

        // Act: Deselect the worker
        deselectWorker(testWorkerId, testChantierId)
                .andExpect(status().isOk()); // Endpoint returns 200 OK with empty body

        // Assert: Verify worker is no longer returned by getWorkersForChantier
        mockMvc.perform(get("/api/worker-selection/chantier/{chantierId}/workers", testChantierId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk()) // Endpoint still OK
                .andExpect(jsonPath("$.data").isArray()) // Data is an array
                .andExpect(jsonPath("$.data[?(@.id == %d)]", testWorkerId).doesNotExist()); // Worker is not present

        // Assert: Optionally, verify using getSelectionsForChantier that isSelected is false
        mockMvc.perform(get("/api/worker-selection/chantier/{chantierId}/selections", testChantierId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.worker == %d && @.chantier == %d)].isSelected", testWorkerId, testChantierId).value(false)); // Check isSelected is false
    }

    @Test
    void selectWorker_whenAlreadySelected_shouldUpdateNote() throws Exception {
        // Arrange: Select the worker first
        String initialNote = "Initial Selection";
        selectWorker(testWorkerId, testChantierId, initialNote)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.selectionNote").value(initialNote));

        // Act: Select the same worker again with a new note
        String updatedNote = "Updated Selection Note";
        selectWorker(testWorkerId, testChantierId, updatedNote)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.selectionNote").value(updatedNote)); // Check note is updated

        // Assert: Verify the note is updated using getSelections
        mockMvc.perform(get("/api/worker-selection/chantier/{chantierId}/selections", testChantierId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.worker == %d && @.chantier == %d)].selectionNote", testWorkerId, testChantierId).value(updatedNote));
    }

}