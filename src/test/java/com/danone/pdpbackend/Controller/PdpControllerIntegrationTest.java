package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Repo.*;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.HoraireDeTravaille;
import com.danone.pdpbackend.Utils.MisesEnDisposition;
import com.danone.pdpbackend.Utils.ObjectAnsweredObjects;
import com.danone.pdpbackend.Utils.mappers.ObjectAnsweredMapper;
import com.danone.pdpbackend.Utils.mappers.PdpMapper;
import com.danone.pdpbackend.entities.*;
import com.danone.pdpbackend.entities.dto.ObjectAnsweredDTO;
import com.danone.pdpbackend.entities.dto.PdpDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class PdpControllerIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(PdpControllerIntegrationTest.class);

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    public ObjectMapper objectMapper;

    @Autowired
    public PdpRepo pdpRepo;

    public String token;
    @Autowired
    public ChantierRepo chantierRepo;


    PdpDTO testDTO = new PdpDTO();
    @Autowired
    public RisqueRepo risqueRepo;

    @Autowired
    public ObjectAnswerRepo objectAnswerRepo;

    @Autowired
    public DispositifRepo dispositifRepo;

    @Autowired
    public PermitRepo permitRepo;

    @Autowired
    public AuditSecuRepo auditSecuRepo;

    @Autowired
    public AnalyseDeRisqueRepo analyseDeRisqueRepo;
    @Autowired
    private PdpMapper pdpMapper;
    @Autowired
    private ObjectAnsweredMapper objectAnsweredMapper;

    void getToken() throws Exception {
        // Login and obtain token
        String loginCredentials = """
            {
                "username": "Yassin4",
                "password": "Zaqwe123!"
            }
            """;

        // Perform login request
        MvcResult loginResult = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginCredentials))
                .andExpect(status().isOk())
                .andReturn();

        // Extract token from response using ApiResponse
        String loginResponse = loginResult.getResponse().getContentAsString();

        ApiResponse authResponse = objectMapper.readValue(loginResponse, ApiResponse.class);

        // Extract token from data object in ApiResponse
        Map<String, Object> data = (Map<String, Object>) authResponse.getData();
        token = (String) data.get("token");

        log.info("Token extracted: {}", token.substring(0, 20) + "..."); // Log partial token for security
    }

    @BeforeEach
    void setUp() throws Exception {
        // Get token before each test
        getToken();
        // Initialize testDTO with valid data
        testDTO.setChantier(getNewChantier().getId());
        testDTO.setDateInspection(new Date());
        testDTO.setIcpdate(new Date());
        testDTO.setDatePrevenirCSSCT(new Date());
        testDTO.setHorairesDetails("Details about working hours");
        testDTO.setDatePrev(new Date());
        testDTO.setEntrepriseDInspection(1L); // Assuming this ID exists in your DB
        testDTO.setEntrepriseExterieure(1L); // Assuming this ID exists in your DB
        testDTO.setSignatures(List.of(1L, 2L)); // Assuming these IDs exist in your DB
        testDTO.setMisesEnDisposition(new MisesEnDisposition()); // Assuming this is a valid enum value
        testDTO.setHoraireDeTravail(new HoraireDeTravaille()); // Assuming this is a valid enum value

    }



    //Post
    public <T> T performPost(PdpDTO bodyDTO, TypeReference<ApiResponse<T>> typeRef) throws Exception {
        String json = objectMapper.writeValueAsString(bodyDTO);

        RequestBuilder requestBuilder = post("/api/pdp/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(json);

        MvcResult postResult = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();

        String content = postResult.getResponse().getContentAsString();
        ApiResponse<T> response = objectMapper.readValue(content, typeRef);
        return response.getData();
    }

    //Get
    public <T> T performGet(String url, TypeReference<ApiResponse<T>> typeRef) throws Exception {
        MvcResult result = mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ApiResponse<T> response = objectMapper.readValue(content, typeRef);
        return response.getData();
    }

    //Patch
    public <T> T performPatch(String url, Object body, TypeReference<ApiResponse<T>> typeRef) throws Exception {
        String json = objectMapper.writeValueAsString(body);
    log.info("body receved: {}" , json);
        MvcResult result = mockMvc.perform(patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ApiResponse<T> response = objectMapper.readValue(content, typeRef);
        return response.getData();
    }

    //Delete
    public <T> T performDelete(String url, TypeReference<ApiResponse<T>> typeRef) throws Exception {
        MvcResult result = mockMvc.perform(delete(url)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ApiResponse<T> response = objectMapper.readValue(content, typeRef);
        return response.getData();
    }


    Chantier getNewChantier() {
        Chantier chantier = new Chantier();
        // Set properties for chantier if needed
        return chantierRepo.save(chantier);
    }
    Risque getNewRisque(){
        Risque risque = new Risque();
        return risqueRepo.save(risque);
    }

    ObjectAnsweredDTO getNewObjectAnswered(Long objectId, Pdp pdp, ObjectAnsweredObjects type){
        ObjectAnswered objectAnswered = ObjectAnswered.builder()
                .objectId(objectId)
                .pdp(pdp)
                .objectType(type)
                .build();

        ObjectAnswered savedObject= objectAnswerRepo.save(objectAnswered);

        return objectAnsweredMapper.toDTO(savedObject);
    }

    @Test
    void createPdp_withValidData_returnsCreatedPdp() throws Exception {
        PdpDTO pdpDTO =  performPost(testDTO, new TypeReference<ApiResponse<PdpDTO>>() {});

        mockMvc.perform(get("/api/pdp/" + pdpDTO.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(pdpDTO.getId()));
    }



   @ParameterizedTest
   @EnumSource(ObjectAnsweredObjects.class)
   void updatePdp_shouldHandleAddEditDeleteInOneRequest(ObjectAnsweredObjects objectType)  throws Exception {

        if(objectType == ObjectAnsweredObjects.AUDIT) {
            return; // Skip this test for AUDIT type
        }

        // 1. Create PDP
        PdpDTO existingPdpDTO = performPost(testDTO, new TypeReference<ApiResponse<PdpDTO>>() {});
        Long pdpId = existingPdpDTO.getId();
        Optional<Pdp> pdp = pdpRepo.findById(pdpId);



            // 2. Create Risques
           Long item1Id = 1L;
           Long item2Id = 2L;
           Long item3Id = 3L;



       // 3. Existing ObjectAnswered (for r1 and r2)
        ObjectAnsweredDTO o1 = getNewObjectAnswered(item1Id, pdp.get(), objectType);
        o1.setAnswer(false);
        o1 = objectAnsweredMapper.toDTO(objectAnswerRepo.save(objectAnsweredMapper.toEntity(o1)));

       ObjectAnsweredDTO o2 = getNewObjectAnswered(item2Id,pdp.get() , objectType);
        o2.setAnswer(true);
       o2 = objectAnsweredMapper.toDTO(objectAnswerRepo.save(objectAnsweredMapper.toEntity(o2)));

        // 4. Prepare update payload
       ObjectAnsweredDTO updateO1 = ObjectAnsweredDTO.builder()
                .id(o1.getId())
                .objectId(item1Id)
                .pdp(pdpId)
                .objectType(objectType)
                .answer(true) // updated
                .build();

       ObjectAnsweredDTO deleteO2 = ObjectAnsweredDTO.builder()
                .id(o2.getId())
                .objectId(item2Id)
                .objectType(objectType)
                .pdp(pdpId)
                .answer(null) // to delete
                .build();

       ObjectAnsweredDTO newO3 = ObjectAnsweredDTO.builder()
                .objectId(item3Id)
                .pdp(pdpId)
                .answer(false) // new object
                .build();
        //[{id:1, answer:true}, {id:2, answer:null}, {id:null, answer:false}
       existingPdpDTO.setRelations(List.of(updateO1, deleteO2, newO3));
        // 5. PATCH update
        PdpDTO updated = performPatch("/api/pdp/" + pdpId, existingPdpDTO, new TypeReference<ApiResponse<PdpDTO>>() {});
       //[o1 = {id:1, answer:true}, {id:3, answer:false}

       String nameInJson = switch (objectType) {
            case RISQUE -> "risques";
            case DISPOSITIF -> "dispositifs";
            case PERMIT -> "permits";
            case AUDIT -> "audits";
            case ANALYSE_DE_RISQUE -> "analyseDeRisques";
        };


                assertEquals(2, updated.getRelations().size());
                assertEquals(true, updated.getRelations().get(0).getAnswer());
                assertEquals(false, updated.getRelations().get(1).getAnswer());



       // 6. GET and assert
        mockMvc.perform(get("/api/pdp/" + pdpId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.relations.length()").value(2))
                .andExpect(jsonPath("$.data.relations[?(@.id == " + o1.getId() + ")].answer").value(true))
                .andExpect(jsonPath("$.data.relations[?(@.id == " + o2.getId() + ")]").doesNotExist())
                .andExpect(jsonPath("$.data.relations[?(@.id == " + o1.getId() + ")].answer").value(true));
    }


    @ParameterizedTest
    @EnumSource(ObjectAnsweredObjects.class)
    void updatePdp_shouldHandleAddEditDeleteInOneRequest_ShouldReturnValueOfChangedAnswers(ObjectAnsweredObjects objectType)  throws Exception {

        if(objectType == ObjectAnsweredObjects.AUDIT) {
            return; // Skip this test for AUDIT type
        }

        // 1. Create PDP
        PdpDTO existingPdpDTO = performPost(testDTO, new TypeReference<ApiResponse<PdpDTO>>() {});
        Long pdpId = existingPdpDTO.getId();
        Optional<Pdp> pdp = pdpRepo.findById(pdpId);



        // 2. Create Risques
        Long item1Id = 1L;
        Long item2Id = 2L;
        Long item3Id = 3L;



        // 3. Existing ObjectAnswered (for r1 and r2)
        ObjectAnsweredDTO o1 = getNewObjectAnswered(item1Id, pdp.get(), objectType);
        o1.setAnswer(false);
        o1 = objectAnsweredMapper.toDTO(objectAnswerRepo.save(objectAnsweredMapper.toEntity(o1)));

        ObjectAnsweredDTO o2 = getNewObjectAnswered(item2Id,pdp.get() , objectType);
        o2.setAnswer(true);
        o2 = objectAnsweredMapper.toDTO(objectAnswerRepo.save(objectAnsweredMapper.toEntity(o2)));

        // 4. Prepare update payload
        ObjectAnsweredDTO updateO1 = ObjectAnsweredDTO.builder()
                .id(o1.getId())
                .objectId(item1Id)
                .pdp(pdpId)
                .objectType(objectType)
                .answer(false) // updated
                .build();

        ObjectAnsweredDTO deleteO2 = ObjectAnsweredDTO.builder()
                .id(o2.getId())
                .objectId(item2Id)
                .objectType(objectType)
                .pdp(pdpId)
                .answer(null) // to delete
                .build();

        ObjectAnsweredDTO newO3 = ObjectAnsweredDTO.builder()
                .objectId(item3Id)
                .pdp(pdpId)
                .answer(false) // new object
                .build();
        //[{id:1, answer:true}, {id:2, answer:null}, {id:null, answer:false}

         existingPdpDTO.setRelations(List.of(updateO1, deleteO2, newO3));

        // 5. PATCH update
        PdpDTO updated = performPatch("/api/pdp/" + pdpId, existingPdpDTO, new TypeReference<ApiResponse<PdpDTO>>() {});
        //[o1 = {id:1, answer:true}, {id:3, answer:false}


                assertEquals(2, updated.getRelations().size());
                assertEquals(false, updated.getRelations().get(0).getAnswer());
                assertEquals(false, updated.getRelations().get(1).getAnswer());



        // 6. GET and assert
        mockMvc.perform(get("/api/pdp/" + pdpId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.relations.length()").value(2))
                .andExpect(jsonPath("$.data.relations[?(@.id == " + o1.getId() + ")].answer").value(false))
                .andExpect(jsonPath("$.data.relations[?(@.id == " + o2.getId() + ")]").doesNotExist())
                .andExpect(jsonPath("$.data.relations[?(@.id == " + o1.getId() + ")].answer").value(false));;
    }




    @ParameterizedTest
    @EnumSource(ObjectAnsweredObjects.class)
    void updatePdp_shouldHandleAddEditDeleteInOneRequest_ShouldReturnRightObjectTypes(ObjectAnsweredObjects objectType)  throws Exception {

        if(objectType == ObjectAnsweredObjects.AUDIT) {
            return; // Skip this test for AUDIT type
        }

        // 1. Create PDP
        PdpDTO existingPdpDTO = performPost(testDTO, new TypeReference<ApiResponse<PdpDTO>>() {});
        Long pdpId = existingPdpDTO.getId();
        Optional<Pdp> pdp = pdpRepo.findById(pdpId);



        // 2. Create Risques
        Long item1Id = 1L;
        Long item2Id = 2L;
        Long item3Id = 3L;



        // 3. Existing ObjectAnswered (for r1 and r2)
        ObjectAnsweredDTO o1 = getNewObjectAnswered(item1Id, pdp.get(), objectType);
        o1.setAnswer(false);
        o1 = objectAnsweredMapper.toDTO(objectAnswerRepo.save(objectAnsweredMapper.toEntity(o1)));

        ObjectAnsweredDTO o2 = getNewObjectAnswered(item2Id,pdp.get() , objectType);
        o2.setAnswer(true);
        o2 = objectAnsweredMapper.toDTO(objectAnswerRepo.save(objectAnsweredMapper.toEntity(o2)));

        // 4. Prepare update payload
        ObjectAnsweredDTO updateO1 = ObjectAnsweredDTO.builder()
                .id(o1.getId())
                .objectId(item1Id)
                .pdp(pdpId)
                .objectType(objectType)
                .answer(false) // updated
                .build();

        ObjectAnsweredDTO deleteO2 = ObjectAnsweredDTO.builder()
                .id(o2.getId())
                .objectId(item2Id)
                .objectType(objectType)
                .pdp(pdpId)
                .answer(null) // to delete
                .build();

        ObjectAnsweredDTO newO3 = ObjectAnsweredDTO.builder()
                .objectId(item3Id)
                .pdp(pdpId)
                .answer(false) // new object
                .build();
        //[{id:1, answer:true}, {id:2, answer:null}, {id:null, answer:false}

        existingPdpDTO.setRelations(List.of(updateO1, deleteO2, newO3));

        // 5. PATCH update
        PdpDTO updated = performPatch("/api/pdp/" + pdpId, existingPdpDTO, new TypeReference<ApiResponse<PdpDTO>>() {});
        //[o1 = {id:1, answer:true}, {id:3, answer:false}


        assertEquals(2, updated.getRelations().size());
        assertEquals(false, updated.getRelations().get(0).getAnswer());
        assertEquals(false, updated.getRelations().get(1).getAnswer());



        // 6. GET and assert
        mockMvc.perform(get("/api/pdp/" + pdpId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.relations.length()").value(2))
                .andExpect(jsonPath("$.data.relations[?(@.id == " + o1.getId() + ")].objectType").value(objectType.toString()));
    }



    // Helper method to create a Risque if needed for the test setup
    private Risque getOrCreateTestRisque() {

            Risque r = new Risque();
            r.setTitle("Test Risque for ObjectAnswered");
            return risqueRepo.save(r);
    }

    @Test
    void updatePdp_shouldUpdateObjectAnsweredEEandEUFields() throws Exception {
        // 1. Create a PDP via POST
        PdpDTO createdPdpDTO = performPost(testDTO, new TypeReference<ApiResponse<PdpDTO>>() {});
        Long pdpId = createdPdpDTO.getId();
        log.info("Created PDP with ID: {}", pdpId);

        // 2. Prepare initial ObjectAnswered relation
        Risque testRisque = getOrCreateTestRisque(); // Ensure risque exists
        Long risqueId = testRisque.getId();

        ObjectAnsweredDTO initialRelationDTO = ObjectAnsweredDTO.builder()
                .pdp(pdpId) // Link to the created PDP
                .objectId(risqueId)
                .objectType(ObjectAnsweredObjects.RISQUE)
                .answer(true)
                .EE(false) // Initial state
                .EU(false) // Initial state
                .build();

        // Create a PdpDTO for the first PATCH request
        PdpDTO patch1DTO = new PdpDTO();
        patch1DTO.setId(pdpId); // Set the ID for update
        patch1DTO.setRelations(List.of(initialRelationDTO)); // Add the new relation

        // 3. Perform first PATCH to add the relation
        log.info("Performing first PATCH to add relation: {}", objectMapper.writeValueAsString(patch1DTO));
        PdpDTO pdpAfterPatch1 = performPatch("/api/pdp/" + pdpId, patch1DTO, new TypeReference<ApiResponse<PdpDTO>>() {});

        // 4. Verify initial state & get the ID of the created ObjectAnswered
        assertNotNull(pdpAfterPatch1.getRelations(), "Relations list should not be null after first patch");
        assertEquals(1, pdpAfterPatch1.getRelations().size(), "Should have 1 relation after first patch");
        ObjectAnsweredDTO addedRelation = pdpAfterPatch1.getRelations().get(0);
        assertNotNull(addedRelation.getId(), "Added relation should have an ID");
        assertEquals(false, addedRelation.getEE());
        assertEquals(false, addedRelation.getEU());
        assertEquals(true, addedRelation.getAnswer());
        assertEquals(ObjectAnsweredObjects.RISQUE, addedRelation.getObjectType());
        assertEquals(risqueId, addedRelation.getObjectId());
        Long objectAnsweredId = addedRelation.getId(); // Get the ID for the next update
        log.info("Added relation with ID: {}", objectAnsweredId);

        // 5. Prepare second PATCH payload to update EE and EU
        ObjectAnsweredDTO updateRelationDTO = ObjectAnsweredDTO.builder()
                .id(objectAnsweredId) // Use the existing ID to update
                .pdp(pdpId)
                .objectId(risqueId)
                .objectType(ObjectAnsweredObjects.RISQUE)
                .answer(true) // Keep answer the same or change if needed
                .EE(true) // Update EE
                .EU(true) // Update EU
                .build();

        // Create a PdpDTO for the second PATCH request
        PdpDTO patch2DTO = new PdpDTO();
        patch2DTO.setId(pdpId);
        patch2DTO.setRelations(List.of(updateRelationDTO)); // Update the relation

        // 6. Perform second PATCH to update the relation's EE/EU fields
        log.info("Performing second PATCH to update relation: {}", objectMapper.writeValueAsString(patch2DTO));
        PdpDTO pdpAfterPatch2 = performPatch("/api/pdp/" + pdpId, patch2DTO, new TypeReference<ApiResponse<PdpDTO>>() {});

        // 7. Verify the updated state via the response
        log.info("Response after second PATCH: {}", objectMapper.writeValueAsString(pdpAfterPatch2));
        assertNotNull(pdpAfterPatch2.getRelations(), "Relations list should not be null after second patch");
        assertEquals(1, pdpAfterPatch2.getRelations().size(), "Should still have 1 relation after second patch");
        ObjectAnsweredDTO updatedRelation = pdpAfterPatch2.getRelations().get(0);
        assertEquals(objectAnsweredId, updatedRelation.getId(), "ID should remain the same");
        assertEquals(true, updatedRelation.getEE(), "EE should be updated to true");
        assertEquals(true, updatedRelation.getEU(), "EU should be updated to true");
        assertEquals(true, updatedRelation.getAnswer()); // Verify other fields remain correct
        assertEquals(ObjectAnsweredObjects.RISQUE, updatedRelation.getObjectType());
        assertEquals(risqueId, updatedRelation.getObjectId());


        // 8. Optionally, verify directly from the database repository
        ObjectAnswered oaFromDbOpt = objectAnswerRepo.findById(objectAnsweredId);

        assertEquals(true, oaFromDbOpt.getEe(), "DB value for EE should be true");
        assertEquals(true, oaFromDbOpt.getEu(), "DB value for EU should be true");
        assertEquals(pdpId, oaFromDbOpt.getPdp().getId(), "DB value for Pdp ID should match");
    }

}