package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Controller.auth.AuthenticationRequest;
import com.danone.pdpbackend.Controller.auth.AuthenticationResponse;
import com.danone.pdpbackend.Repo.RisqueRepo;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.PermiTypes;
import com.danone.pdpbackend.entities.Risque;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class RisqueControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RisqueRepo risqueRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private Risque testRisque;
    private String authToken;

    @BeforeEach
    public void setUp() throws Exception {
        // Get authentication token
        authToken = authenticate("Yassin4", "Zaqwe123!");

        // Clear repository before each test
        List<Risque> allRisques = risqueRepo.findAll();
        for (Risque risque : allRisques) {
            risqueRepo.deleteById(risque.getId());
        }

        // Create a test risque with permitType
        testRisque = new Risque();
        testRisque.setTravailleDangereux(true);
        testRisque.setTravaillePermit(true);
        testRisque.setPermitId(123L);
        testRisque.setPermitType(PermiTypes.ATEX);
    }

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

        ApiResponse<AuthenticationResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<AuthenticationResponse>>() {}
        );

        return response.getData().getToken();
    }

    @Test
    public void testCreateRisque_WithPermitType_ShouldPersistPermitType() throws Exception {
        // Given
        String risqueJson = objectMapper.writeValueAsString(testRisque);

        // When
        MvcResult result = mockMvc.perform(post("/api/risque")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(risqueJson))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<Risque> response = objectMapper.readValue(responseContent,
                new TypeReference<ApiResponse<Risque>>() {});

        assertNotNull(response.getData());
        Long createdId = response.getData().getId();
        assertNotNull(createdId);

        // Verify permitType is persisted in database
        Risque savedRisque = risqueRepo.findRisqueById(createdId);
        assertNotNull(savedRisque);
        assertEquals(PermiTypes.ATEX, savedRisque.getPermitType());
        assertEquals(123L, savedRisque.getPermitId());
        assertTrue(savedRisque.getTravailleDangereux());
        assertTrue(savedRisque.getTravaillePermit());
    }

    @Test
    public void testUpdateRisque_WithPermitType_ShouldPersistPermitType() throws Exception {
        // Given - Create initial risque
        Risque initialRisque = new Risque();
        initialRisque.setTravailleDangereux(false);
        initialRisque.setTravaillePermit(false);
        initialRisque.setPermitType(PermiTypes.NONE);
        Risque savedRisque = risqueRepo.save(initialRisque);

        // Create update data with new permitType
        Risque updateData = new Risque();
        updateData.setPermitType(PermiTypes.HAUTEUR);
        updateData.setPermitId(456L);
        updateData.setTravailleDangereux(true);
        updateData.setTravaillePermit(true);

        String updateJson = objectMapper.writeValueAsString(updateData);

        // When
        MvcResult result = mockMvc.perform(patch("/api/risque/" + savedRisque.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<Risque> response = objectMapper.readValue(responseContent,
                new TypeReference<ApiResponse<Risque>>() {});

        assertNotNull(response.getData());

        // Verify permitType is updated in database
        Risque updatedRisque = risqueRepo.findRisqueById(savedRisque.getId());
        assertNotNull(updatedRisque);
        assertEquals(PermiTypes.HAUTEUR, updatedRisque.getPermitType());
        assertEquals(456L, updatedRisque.getPermitId());
        assertTrue(updatedRisque.getTravailleDangereux());
        assertTrue(updatedRisque.getTravaillePermit());
    }

    @Test
    public void testUpdateRisque_PartialUpdate_ShouldPreserveExistingPermitType() throws Exception {
        // Given - Create initial risque with permitType
        Risque initialRisque = new Risque();
        initialRisque.setTravailleDangereux(false);
        initialRisque.setTravaillePermit(true);
        initialRisque.setPermitType(PermiTypes.ESPACE_CONFINE);
        initialRisque.setPermitId(789L);
        Risque savedRisque = risqueRepo.save(initialRisque);

        // Create partial update data (without permitType)
        Risque updateData = new Risque();
        updateData.setTravailleDangereux(true); // Only update this field

        String updateJson = objectMapper.writeValueAsString(updateData);

        // When
        MvcResult result = mockMvc.perform(patch("/api/risque/" + savedRisque.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<Risque> response = objectMapper.readValue(responseContent,
                new TypeReference<ApiResponse<Risque>>() {});

        assertNotNull(response.getData());

        // Verify existing permitType is preserved
        Risque updatedRisque = risqueRepo.findRisqueById(savedRisque.getId());
        assertNotNull(updatedRisque);
        assertEquals(PermiTypes.ESPACE_CONFINE, updatedRisque.getPermitType()); // Should remain unchanged
        assertEquals(789L, updatedRisque.getPermitId()); // Should remain unchanged
        assertTrue(updatedRisque.getTravailleDangereux()); // Should be updated
        assertTrue(updatedRisque.getTravaillePermit()); // Should remain unchanged
    }

    @Test
    public void testUpdateRisque_WithNullPermitType_ShouldNotOverwriteExisting() throws Exception {
        // Given - Create initial risque with permitType
        Risque initialRisque = new Risque();
        initialRisque.setPermitType(PermiTypes.LEVAGE);
        initialRisque.setPermitId(999L);
        Risque savedRisque = risqueRepo.save(initialRisque);

        // Create update data with explicitly null permitType
        Risque updateData = new Risque();
        updateData.setPermitType(null);
        updateData.setPermitId(null);

        String updateJson = objectMapper.writeValueAsString(updateData);

        // When
        mockMvc.perform(patch("/api/risque/" + savedRisque.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());

        // Then - Note: Since the reflection logic only updates non-null values,
        // null values won't overwrite existing values. This is the current behavior.
        Risque updatedRisque = risqueRepo.findRisqueById(savedRisque.getId());
        assertNotNull(updatedRisque);
        // The permitType should remain unchanged because null values are ignored
        assertEquals(PermiTypes.LEVAGE, updatedRisque.getPermitType());
        assertEquals(999L, updatedRisque.getPermitId());
    }

    @Test
    public void testGetRisqueById_ShouldReturnPermitType() throws Exception {
        // Given
        Risque savedRisque = risqueRepo.save(testRisque);

        // When
        MvcResult result = mockMvc.perform(get("/api/risque/" + savedRisque.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<Risque> response = objectMapper.readValue(responseContent,
                new TypeReference<ApiResponse<Risque>>() {});

        assertNotNull(response.getData());
        assertEquals(PermiTypes.ATEX, response.getData().getPermitType());
        assertEquals(123L, response.getData().getPermitId());
    }

    @Test
    public void testGetAllRisques_ShouldReturnPermitTypes() throws Exception {
        // Given - Create multiple risques with different permitTypes
        Risque risque1 = new Risque();
        risque1.setPermitType(PermiTypes.ATEX);
        risque1.setPermitId(111L);

        Risque risque2 = new Risque();
        risque2.setPermitType(PermiTypes.HAUTEUR);
        risque2.setPermitId(222L);

        risqueRepo.save(risque1);
        risqueRepo.save(risque2);

        // When
        MvcResult result = mockMvc.perform(get("/api/risque")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<List<Risque>> response = objectMapper.readValue(responseContent,
                new TypeReference<ApiResponse<List<Risque>>>() {});

        assertNotNull(response.getData());
        assertEquals(2, response.getData().size());

        // Verify permitTypes are preserved
        boolean foundAtex = false;
        boolean foundHauteur = false;

        for (Risque risque : response.getData()) {
            if (PermiTypes.ATEX.equals(risque.getPermitType())) {
                foundAtex = true;
                assertEquals(111L, risque.getPermitId());
            } else if (PermiTypes.HAUTEUR.equals(risque.getPermitType())) {
                foundHauteur = true;
                assertEquals(222L, risque.getPermitId());
            }
        }

        assertTrue(foundAtex, "ATEX permitType should be found");
        assertTrue(foundHauteur, "HAUTEUR permitType should be found");
    }
}
