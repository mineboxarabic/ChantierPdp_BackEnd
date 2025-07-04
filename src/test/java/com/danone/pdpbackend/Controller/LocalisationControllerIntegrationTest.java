package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.entities.dto.LocalisationDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import static com.danone.pdpbackend.Controller.IntegrationTestUtils.authenticate;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("LocalisationController Integration Test")
@Transactional
class LocalisationControllerIntegrationTest {


    @Autowired
    private MockMvc mockMvc;
    private HelperMethods helperMethods;
    ObjectMapper objectMapper = new ObjectMapper();

    String authToken;

    Long localisationId;

    LocalisationDTO createLocalisationDTOAPI(String nom, String code, String description) throws Exception {
        LocalisationDTO localisationDTO = new LocalisationDTO();
        localisationDTO.setNom(nom);
        localisationDTO.setCode(code);
        localisationDTO.setDescription(description);


        MvcResult res = mockMvc.perform(post("/api/localisation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authToken)
                        .content(objectMapper.writeValueAsString(localisationDTO)))
                .andExpect(status().isOk())
                .andReturn();


        String response = res.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        ApiResponse<LocalisationDTO> localisationResponse = objectMapper.convertValue(responseMap, new TypeReference<ApiResponse<LocalisationDTO>>() {
        });

        return localisationResponse.getData();
    }


    LocalisationDTO getLocalisationById(Long id) throws Exception {
        MvcResult res = mockMvc.perform(get("/api/localisation/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        String response = res.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        ApiResponse<LocalisationDTO> localisationResponse = objectMapper.convertValue(responseMap, new TypeReference<ApiResponse<LocalisationDTO>>() {
        });

        return localisationResponse.getData();
    }

    @BeforeEach
    void setUp() throws Exception {
        helperMethods = new HelperMethods(mockMvc);


        authToken = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"Yassin4\", \"password\": \"Zaqwe123!\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        authToken = objectMapper.readTree(authToken).get("data").get("token").asText();


        // Create a new localisation for testing
        LocalisationDTO localisationDTO = createLocalisationDTOAPI("Test Localisation", "TL", "Test Description");
        localisationId = localisationDTO.getId();



    }

    @Test
    @DisplayName("Get all localisations")
    void getAllLocalisations() throws Exception {
        mockMvc.perform(get("/api/localisation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].nom").exists())
                .andExpect(jsonPath("$.data[0].code").exists())
                .andExpect(jsonPath("$.data[0].description").exists());

    }

    @Test
    @DisplayName("Get localisation by ID")
    void getLocalisationById() throws Exception {

        LocalisationDTO localisationDTO = createLocalisationDTOAPI("Test Localisation", "TL", "Test Description");
        Long id = localisationDTO.getId();
        MvcResult res = mockMvc.perform(get("/api/localisation/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        String response = res.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        ApiResponse<LocalisationDTO> localisationResponse = objectMapper.convertValue(responseMap, new TypeReference<ApiResponse<LocalisationDTO>>() {
        });
        assertNotNull(localisationResponse);
        assertEquals("Test Localisation", localisationResponse.getData().getNom());
        assertEquals("TL", localisationResponse.getData().getCode());
        assertEquals("Test Description", localisationResponse.getData().getDescription());
        assertEquals(id, localisationResponse.getData().getId());
        assertNotNull(localisationResponse.getData().getId());

    }

    @Test
    @DisplayName("Create new localisation")
    void createLocalisation() throws Exception {
        LocalisationDTO localisationDTO = createLocalisationDTOAPI("New Localisation", "NL", "New Description");
        assertNotNull(localisationDTO);
        assertEquals("New Localisation", localisationDTO.getNom());
        assertEquals("NL", localisationDTO.getCode());
        assertEquals("New Description", localisationDTO.getDescription());
        assertNotNull(localisationDTO.getId());
        assertTrue(localisationDTO.getId() > 0);
    }

    @Test
    @DisplayName("Update existing localisation")
    void updateLocalisation() {
        try {
            LocalisationDTO localisationDTO = getLocalisationById(localisationId);
            assertNotNull(localisationDTO);
            assertEquals("Test Localisation", localisationDTO.getNom());
            assertEquals("TL", localisationDTO.getCode());
            assertEquals("Test Description", localisationDTO.getDescription());

            // Update the localisation
            localisationDTO.setNom("Updated Localisation");
            localisationDTO.setCode("UL");
            localisationDTO.setDescription("Updated Description");

            MvcResult res = mockMvc.perform(patch("/api/localisation/" + localisationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + authToken)
                            .content(objectMapper.writeValueAsString(localisationDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = res.getResponse().getContentAsString();
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            ApiResponse<LocalisationDTO> localisationResponse = objectMapper.convertValue(responseMap, new TypeReference<ApiResponse<LocalisationDTO>>() {
            });

            assertNotNull(localisationResponse);
            assertEquals("Updated Localisation", localisationResponse.getData().getNom());
            assertEquals("UL", localisationResponse.getData().getCode());
            assertEquals("Updated Description", localisationResponse.getData().getDescription());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Delete existing localisation")
    void deleteLocalisation() {
        try {
            LocalisationDTO localisationDTO = createLocalisationDTOAPI("Test Localisation", "TL", "Test Description");
            // Delete the localisation
            mockMvc.perform(delete("/api/localisation/" + localisationDTO.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk());

            // Verify that the localisation is deleted
            mockMvc.perform(get("/api/localisation/" + localisationDTO.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNotFound());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}