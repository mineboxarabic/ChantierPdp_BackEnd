package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.ChantierStatus;
import com.danone.pdpbackend.Utils.DocumentStatus;
import com.danone.pdpbackend.entities.Bdt;
import com.danone.pdpbackend.entities.Document;
import com.danone.pdpbackend.entities.DocumentSignature;
import com.danone.pdpbackend.entities.Pdp;
import com.danone.pdpbackend.entities.dto.BdtDTO;
import com.danone.pdpbackend.entities.dto.ChantierDTO;
import com.danone.pdpbackend.entities.dto.DocumentDTO;
import com.danone.pdpbackend.entities.dto.PdpDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Document Controller Integration Test")
public class DocumentControllerIntergrationTest {



    @Autowired
    private MockMvc mockMvc;


    String authToken;

    ObjectMapper objectMapper = new ObjectMapper();

    ChantierDTO chantierDTO;




    @BeforeEach
    public void setUp() throws Exception {
        objectMapper.registerModule(new JavaTimeModule());

        // Initialize the authToken here
        String response  = mockMvc.perform(post("/api/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"Yassin4\", \"password\": \"Zaqwe123!\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();


        Map<String, Object> responseJson = objectMapper.readValue(response, Map.class);

        Map<String, Object> data = (Map<String, Object>) responseJson.get("data");


        authToken = (String) data.get("token");



        // Initialize the chantierDTO here
        chantierDTO = createChantier();



    }

    private ChantierDTO createChantier() throws Exception {
        ChantierDTO chantierDTO = new ChantierDTO();
        chantierDTO.setId(1L);
        chantierDTO.setNom("Chantier Test");
        chantierDTO.setDonneurDOrdre(1L);

        MvcResult result = mockMvc.perform(post("/api/chantier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chantierDTO))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        String returnJson = result.getResponse().getContentAsString();


        ApiResponse<ChantierDTO> apiResponse = objectMapper.readValue(returnJson, new TypeReference<ApiResponse<ChantierDTO>>() {});
        assertEquals("Chantier created successfully", apiResponse.getMessage());
        return apiResponse.getData();
    }


    //Create Documents
    @DisplayName("Test create Document")
    @ParameterizedTest
    @ValueSource(strings = {"pdp", "bdt"})
    public void createDocument_sendWorkingData_shourldReturnCreatedDocument(String documentType) throws Exception {
        if(documentType.equals("pdp")) {
            PdpDTO pdpDTO = new PdpDTO();
            pdpDTO.setChantier(1L);
            pdpDTO.setEntrepriseExterieure(1L);
            pdpDTO.setStatus(DocumentStatus.DRAFT);
            pdpDTO.setDate(LocalDate.now());
            pdpDTO.setSignatures(new ArrayList<>());
            pdpDTO.setRelations(new ArrayList<>());

            PdpDTO createdDocument = createDocuemnt("/api/pdp",pdpDTO);
            assertNotNull(createdDocument.getId());
        }
        else{
            BdtDTO bdtDTO = new BdtDTO();
            bdtDTO.setChantier(1L);
            bdtDTO.setEntrepriseExterieure(1L);
            bdtDTO.setStatus(DocumentStatus.DRAFT);
            bdtDTO.setDate(LocalDate.now());
            bdtDTO.setSignatures(new ArrayList<>());
            bdtDTO.setRelations(new ArrayList<>());

            BdtDTO createdDocument = createDocuemnt("/api/bdt",bdtDTO);
            assertNotNull(createdDocument.getId());
        }}


    //Test creating a document in a chantier and we need check if the document is in the chantier
    @DisplayName("Test create Pdp Document in chantier")
    @Test
    public void createPdpDocumentInChantier_sendWorkingData_ShouldReturnCreatedPdpAndPdpInChantier() throws Exception {
        PdpDTO pdpDTO = new PdpDTO();
        pdpDTO.setChantier(chantierDTO.getId());
        pdpDTO.setEntrepriseExterieure(1L);
        pdpDTO.setStatus(DocumentStatus.DRAFT);
        pdpDTO.setDate(LocalDate.now());
        pdpDTO.setSignatures(new ArrayList<>());
        pdpDTO.setRelations(new ArrayList<>());

        PdpDTO createdDocument = createDocuemnt("/api/pdp",pdpDTO);
        assertNotNull(createdDocument.getId());

        // Check if the document is in the chantier
        ChantierDTO gotChantier = getChantierById(chantierDTO.getId());

        assertNotNull(gotChantier);
        assertEquals(chantierDTO.getId(), gotChantier.getId());
        assertEquals(chantierDTO.getPdps().size(), 1);
    
    }

    private ChantierDTO getChantierById(Long id) throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/chantier/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        ApiResponse<ChantierDTO> apiResponse = objectMapper.readValue(json, new TypeReference<ApiResponse<ChantierDTO>>() {});
        return apiResponse.getData();
    }

    <DOCUMENT extends DocumentDTO> DOCUMENT  createDocuemnt(String api, DOCUMENT documentDTO) throws Exception {
            MvcResult result = mockMvc.perform(post(api)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(documentDTO))
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andReturn();
            String json = result.getResponse().getContentAsString();
            if(documentDTO instanceof PdpDTO){
                ApiResponse<PdpDTO> apiResponse = objectMapper.readValue(json, new TypeReference<ApiResponse<PdpDTO>>() {});
                assertEquals("Pdp saved successfully", apiResponse.getMessage());
                return (DOCUMENT) apiResponse.getData();
            }
            else if(documentDTO instanceof BdtDTO){
                ApiResponse<BdtDTO> apiResponse = objectMapper.readValue(json, new TypeReference<ApiResponse<BdtDTO>>() {});
                assertEquals("BDT created successfully", apiResponse.getMessage());
                return (DOCUMENT) apiResponse.getData();
            }
            else{
                throw new IllegalArgumentException("Invalid document type");
            }
    }

}
