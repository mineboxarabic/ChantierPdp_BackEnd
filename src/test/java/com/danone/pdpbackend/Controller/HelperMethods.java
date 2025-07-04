package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Controller.auth.AuthenticationRequest;
import com.danone.pdpbackend.Controller.auth.AuthenticationResponse;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.entities.dto.EntrepriseDTO;
import com.danone.pdpbackend.entities.dto.WorkerDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HelperMethods {

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();
    String authToken;


    HelperMethods(MockMvc mockMvc) throws Exception {
        this.mockMvc = mockMvc;

        // Authenticate and get token
        MvcResult loginResult = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"Yassin4\", \"password\": \"Zaqwe123!\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String loginResponseJson = loginResult.getResponse().getContentAsString();
        ApiResponse<Map<String, Object>> authResponse = objectMapper.readValue(loginResponseJson, new TypeReference<>() {});
        authToken = (String) authResponse.getData().get("token");
    }

    public List<WorkerDTO> createWorkers(int i, Long entrepriseId) throws Exception {
        List<WorkerDTO> workers = new ArrayList<>();
        for (int j = 0; j < i; j++) {
            WorkerDTO worker = new WorkerDTO();
            worker.setNom("Worker " + j);
            worker.setPrenom("Prenom " + j);
            worker.setEntreprise(entrepriseId);
            workers.add(createWorkerAPI(worker.getNom(), entrepriseId));
        }

        return workers;
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




    public String authenticate(String username, String password) throws Exception {
        AuthenticationRequest request = new AuthenticationRequest(username, password);

        MvcResult result = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<AuthenticationResponse> response = parseResponse(result, new TypeReference<ApiResponse<AuthenticationResponse>>() {});
        return response.getData().getToken();
    }

    <R> ApiResponse<R> parseResponse(MvcResult result, TypeReference<ApiResponse<R>> typeReference) throws IOException {
        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseJson, typeReference);
    }



    //Create Entreprise
    public EntrepriseDTO createEntrepriseAPI(String name) throws Exception {
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

    public EntrepriseDTO createEntrepriseAPI(EntrepriseDTO entrepriseDTO) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/entreprise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrepriseDTO))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ApiResponse<EntrepriseDTO>>() {}).getData();
    }

}
