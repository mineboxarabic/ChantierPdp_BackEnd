package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Controller.auth.AuthenticationRequest;
import com.danone.pdpbackend.Controller.auth.AuthenticationResponse;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.ChantierStatus;
import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.Utils.NotificationType;
import com.danone.pdpbackend.entities.User;
import com.danone.pdpbackend.entities.dto.*; // Assuming all your DTOs are here
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@DisplayName("Dashboard Controller API-Only Integration Tests - Let's F*cking Go!")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private Long testUserId;
    private Long defaultEntrepriseId; // Added for clarity, will be set in setUpEach

    // API Helper Methods

    private <T> T parseApiData(MvcResult result, Class<T> dataClass) throws IOException {
        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse<Map<String,Object>> tempResponse = objectMapper.readValue(jsonResponse, new TypeReference<ApiResponse<Map<String,Object>>>() {});
        return objectMapper.convertValue(tempResponse.getData(), dataClass);
    }

    private <T> ApiResponse<T> parseResponse(MvcResult result, TypeReference<ApiResponse<T>> typeReference) throws IOException {
        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseJson, typeReference);
    }

    private User getAuthenticatedUserFromToken(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/user")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), User.class);
    }

    // <<< NEW/MODIFIED HELPER METHOD START >>>
    private EntrepriseDTO createEntrepriseAPI(EntrepriseDTO entrepriseDTO) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/entreprise")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrepriseDTO)))
                .andExpect(status().isOk()) // Assuming 200 OK on creation
                .andReturn();
        return parseApiData(result, EntrepriseDTO.class);
    }
    // <<< NEW/MODIFIED HELPER METHOD END >>>


    private ChantierDTO createChantierAPI(ChantierDTO chantierDTO) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/chantier/")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chantierDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Chantier saved successfully")))
                .andReturn();
        return parseApiData(result, ChantierDTO.class);
    }

    private PdpDTO createPdpAPI(PdpDTO pdpDTO) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/pdp/")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pdpDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Pdp saved successfully")))
                .andReturn();
        return parseApiData(result, PdpDTO.class);
    }

    private BdtDTO createBdtAPI(BdtDTO bdtDTO) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/bdt")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bdtDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("BDT created successfully")))
                .andReturn();
        return parseApiData(result, BdtDTO.class);
    }

    // <<< NEW HELPER METHOD START >>>
    private WorkerDTO createWorkerAPI(WorkerDTO workerDTO) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/worker/")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Worker created")))
                .andReturn();
        return parseApiData(result, WorkerDTO.class);
    }

    private WorkerChantierSelectionDTO selectWorkerForChantierAPI(Long workerId, Long chantierId, String note) throws Exception {
        WorkerChantierSelectionDTO selectionDTO = new WorkerChantierSelectionDTO();
        selectionDTO.setWorker(workerId);
        selectionDTO.setChantier(chantierId);
        selectionDTO.setSelectionNote(note);

        MvcResult result = mockMvc.perform(post("/api/worker-selection/select")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(selectionDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Worker selected successfully")))
                .andReturn();
        return parseApiData(result, WorkerChantierSelectionDTO.class);
    }
    // <<< NEW HELPER METHOD END >>>


    @BeforeAll
    void setupAll() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
    }

    @BeforeEach
    void setUpEach() throws Exception {
        AuthenticationRequest loginRequest = new AuthenticationRequest("Yassin4", "Zaqwe123!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<AuthenticationResponse> authResponse = parseResponse(loginResult, new TypeReference<ApiResponse<AuthenticationResponse>>() {});
        authToken = authResponse.getData().getToken();
        assertNotNull(authToken, "Auth token is null, what the hell?");

        User authenticatedUser = authResponse.getData().getUser();
        if (authenticatedUser != null && authenticatedUser.getId() != null) {
            testUserId = authenticatedUser.getId();
        } else {
            User currentUserDetails = getAuthenticatedUserFromToken(authToken);
            testUserId = currentUserDetails.getId();
        }
        assertNotNull(testUserId, "Could not determine test user ID, merde!");

        // <<< MODIFIED SECTION START >>>
        // Create a default entreprise if needed for tests
        EntrepriseDTO entrepriseTemplate = new EntrepriseDTO();
        entrepriseTemplate.setNom("Default Test EE for Dashboard " + System.nanoTime());
        defaultEntrepriseId = createEntrepriseAPI(entrepriseTemplate).getId();
        assertNotNull(defaultEntrepriseId, "Default Entreprise ID should not be null");
        // <<< MODIFIED SECTION END >>>
    }

    private Date asDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Test
    @DisplayName("Get Monthly Stats - Original Log-Based Assertions (API Setup Attempt)")
    void getMonthlyStats_originalLogBasedAssertions() throws Exception {
        YearMonth currentMonth = YearMonth.now();
        String currentMonthStr = currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        ChantierDTO chantierDto1 = new ChantierDTO();
        chantierDto1.setNom("Stats Test Chantier API 1 " + System.nanoTime());
        chantierDto1.setDateDebut(asDate(currentMonth.atDay(1)));
        chantierDto1.setDateFin(asDate(currentMonth.atDay(5)));
        chantierDto1.setEntrepriseExterieurs(List.of(defaultEntrepriseId));
        createChantierAPI(chantierDto1);

        ChantierDTO chantierDto2 = new ChantierDTO();
        chantierDto2.setNom("Stats Test Chantier API 2 " + System.nanoTime());
        chantierDto2.setDateDebut(asDate(currentMonth.atDay(2)));
        chantierDto2.setDateFin(asDate(currentMonth.atDay(6)));
        chantierDto2.setEntrepriseExterieurs(List.of(defaultEntrepriseId));
        createChantierAPI(chantierDto2);

        Thread.sleep(500);

        mockMvc.perform(get("/api/dashboard/monthly-stats")
                        .header("Authorization", "Bearer " + authToken)
                        .param("month", currentMonthStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Statistiques mensuelles récupérées. (Monthly statistics retrieved.)")))
                .andExpect(jsonPath("$.data.month", is(currentMonthStr)))
                .andExpect(jsonPath("$.data.chantiersCreated", is(greaterThanOrEqualTo(2))))
                .andReturn();
    }


    @Test
    @DisplayName("Get Monthly Stats - Chantiers Active During Month - Counts Correctly (API Setup)")
    void getMonthlyStats_ChantiersActiveDuringMonth_CountsCorrectly() throws Exception {
        YearMonth queryMonth = YearMonth.now();
        LocalDate monthStart = queryMonth.atDay(1);
        LocalDate monthEnd = queryMonth.atEndOfMonth();

        ChantierDTO ch1Dto = new ChantierDTO();
        ch1Dto.setNom("API Chantier 1 (Active) " + System.nanoTime());
        ch1Dto.setDateDebut(asDate(monthStart.plusDays(5)));
        ch1Dto.setDateFin(asDate(monthEnd.minusDays(5)));
        ch1Dto.setStatus(ChantierStatus.ACTIVE);
        ch1Dto.setEntrepriseExterieurs(List.of(defaultEntrepriseId));
        createChantierAPI(ch1Dto);

        ChantierDTO ch2Dto = new ChantierDTO();
        ch2Dto.setNom("API Chantier 2 (Pending PDP) " + System.nanoTime());
        ch2Dto.setDateDebut(asDate(monthStart.minusDays(10)));
        ch2Dto.setDateFin(asDate(monthEnd.minusDays(2)));
        ch2Dto.setStatus(ChantierStatus.PENDING_PDP);
        ch2Dto.setEntrepriseExterieurs(List.of(defaultEntrepriseId));
        createChantierAPI(ch2Dto);

        ChantierDTO ch3Dto = new ChantierDTO();
        ch3Dto.setNom("API Chantier 3 (Inactive Today) " + System.nanoTime());
        ch3Dto.setDateDebut(asDate(monthStart.plusDays(1)));
        ch3Dto.setDateFin(asDate(monthEnd.plusDays(10)));
        ch3Dto.setStatus(ChantierStatus.INACTIVE_TODAY);
        ch3Dto.setEntrepriseExterieurs(List.of(defaultEntrepriseId));
        createChantierAPI(ch3Dto);

        ChantierDTO ch4Dto = new ChantierDTO();
        ch4Dto.setNom("API Chantier 4 (Ongoing) " + System.nanoTime());
        ch4Dto.setDateDebut(asDate(monthStart.minusDays(5)));
        ch4Dto.setDateFin(null);
        ch4Dto.setStatus(ChantierStatus.ACTIVE);
        ch4Dto.setEntrepriseExterieurs(List.of(defaultEntrepriseId));
        createChantierAPI(ch4Dto);

        ChantierDTO ch5Dto = new ChantierDTO();
        ch5Dto.setNom("API Chantier 5 (Before) " + System.nanoTime());
        ch5Dto.setDateDebut(asDate(monthStart.minusMonths(1).plusDays(1)));
        ch5Dto.setDateFin(asDate(monthStart.minusMonths(1).plusDays(10)));
        ch5Dto.setStatus(ChantierStatus.ACTIVE);
        ch5Dto.setEntrepriseExterieurs(List.of(defaultEntrepriseId));
        createChantierAPI(ch5Dto);

        String queryMonthStr = queryMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        mockMvc.perform(get("/api/dashboard/monthly-stats")
                        .header("Authorization", "Bearer " + authToken)
                        .param("month", queryMonthStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.month", is(queryMonthStr)))
                .andExpect(jsonPath("$.data.chantiersActiveDuringMonth", is(greaterThanOrEqualTo(4))));
    }

    // <<< MODIFIED TEST METHOD START >>>
    @Test
    @DisplayName("Get Monthly Stats - Documents Currently Needing Action - Counts Correctly (API Setup)")
    void getMonthlyStats_DocumentsCurrentlyNeedingAction_CountsCorrectly() throws Exception {
        ChantierDTO chantierForDocsDto = new ChantierDTO();
        chantierForDocsDto.setNom("API Chantier For Docs " + System.nanoTime());
        chantierForDocsDto.setDateDebut(new Date());
        chantierForDocsDto.setEntrepriseExterieurs(List.of(defaultEntrepriseId)); // Link to a default/created EE
        ChantierDTO defaultChantier = createChantierAPI(chantierForDocsDto);

        // Create and assign workers to this chantier to make documents require signatures
        WorkerDTO workerDto1 = new WorkerDTO();
        workerDto1.setNom("Signer1");
        workerDto1.setPrenom("Test");
        workerDto1.setEntreprise(defaultEntrepriseId); // Assuming workers belong to the default EE
        WorkerDTO createdWorker1 = createWorkerAPI(workerDto1);

        WorkerDTO workerDto2 = new WorkerDTO();
        workerDto2.setNom("Signer2");
        workerDto2.setPrenom("Test");
        workerDto2.setEntreprise(defaultEntrepriseId);
        WorkerDTO createdWorker2 = createWorkerAPI(workerDto2);

        // Select these workers for the chantier
        selectWorkerForChantierAPI(createdWorker1.getId(), defaultChantier.getId(), "Needs to sign PDP/BDT");
        selectWorkerForChantierAPI(createdWorker2.getId(), defaultChantier.getId(), "Needs to sign PDP/BDT");


        // Create PDP (without signatures, it should be NEEDS_ACTION)
        PdpDTO pdp1Dto = new PdpDTO();
        pdp1Dto.setChantier(defaultChantier.getId());
        pdp1Dto.setDate(LocalDate.now());
        pdp1Dto.setEntrepriseExterieure(defaultEntrepriseId);
        createPdpAPI(pdp1Dto); // This document should now be NEEDS_ACTION

        // Create BDT (without signatures, it should be NEEDS_ACTION)
        BdtDTO bdt1Dto = new BdtDTO();
        bdt1Dto.setChantier(defaultChantier.getId());
        bdt1Dto.setDate(LocalDate.now());
        bdt1Dto.setEntrepriseExterieure(defaultEntrepriseId);
        bdt1Dto.setNom("API BDT Needs Action " + System.nanoTime());
        createBdtAPI(bdt1Dto); // This document should now be NEEDS_ACTION

        Thread.sleep(500); // Give a moment for any async processing if present (usually not needed for this)

        YearMonth queryMonth = YearMonth.now();
        String queryMonthStr = queryMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        mockMvc.perform(get("/api/dashboard/monthly-stats")
                        .header("Authorization", "Bearer " + authToken)
                        .param("month", queryMonthStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.month", is(queryMonthStr)))
                .andExpect(jsonPath("$.data.documentsCurrentlyNeedingAction", is(greaterThanOrEqualTo(2))));
    }
    // <<< MODIFIED TEST METHOD END >>>


    @Test
    @DisplayName("Get Monthly Stats - Invalid Month Format - Should be Bad Request, espèce d'idiot!")
    void getMonthlyStats_invalidMonthFormat_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/dashboard/monthly-stats")
                        .header("Authorization", "Bearer " + authToken)
                        .param("month", "2023/05"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get Activity Log For Target Entity - Should return filtered logs (API Setup)")
    void getActivityLogForTargetEntity_shouldReturnFilteredLogs() throws Exception {
        ChantierDTO targetChantierDTO = new ChantierDTO();
        String uniqueChantierName = "Log Target Chantier API " + System.nanoTime();
        targetChantierDTO.setNom(uniqueChantierName);
        targetChantierDTO.setDateDebut(new Date());
        targetChantierDTO.setEntrepriseExterieurs(List.of(defaultEntrepriseId));
        ChantierDTO createdTargetChantier = createChantierAPI(targetChantierDTO);
        Long targetChantierId = createdTargetChantier.getId();

        createdTargetChantier.setOperation("Updated Operation for Log Test");
        mockMvc.perform(patch("/api/chantier/{id}", targetChantierId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdTargetChantier)))
                .andExpect(status().isOk());

        Thread.sleep(500);

        mockMvc.perform(get("/api/dashboard/activity-log/target/{entityType}/{entityId}", "Chantier", targetChantierId)
                        .header("Authorization", "Bearer " + authToken)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Journal d'activité récupéré pour l'entité. (Activity log retrieved for entity.)")))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.content[0].targetEntityId", is(targetChantierId.intValue())))
                .andExpect(jsonPath("$.data.content[0].targetEntityType", is("Chantier")));
    }

    @Test
    @DisplayName("Get All Activity Logs (Admin) - Should return all logs (Paginated)")
    void getAllActivityLogs_shouldReturnAllLogs() throws Exception {
        ChantierDTO chantierDto = new ChantierDTO();
        chantierDto.setNom("Activity Log Test Chantier " + System.nanoTime());
        chantierDto.setDateDebut(new Date());
        chantierDto.setEntrepriseExterieurs(List.of(defaultEntrepriseId));
        createChantierAPI(chantierDto);

        Thread.sleep(500);

        MvcResult result = mockMvc.perform(get("/api/dashboard/activity-log/all")
                        .header("Authorization", "Bearer " + authToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Tous les journaux d'activité récupérés. (All activity logs retrieved.)")))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements", is(greaterThanOrEqualTo(1))))
                .andReturn();

        ApiResponse<CustomPage<ActivityLogDTO>> apiResponse =
                objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertTrue(apiResponse.getData().getContent().size() <= 10);
    }

    public static class CustomPage<T> {
        public List<T> content;
        public int number;
        public int size;
        public int totalPages;
        public long totalElements;
        public boolean first;
        public boolean last;
        public boolean empty;
        public List<T> getContent() { return content; }
    }
}