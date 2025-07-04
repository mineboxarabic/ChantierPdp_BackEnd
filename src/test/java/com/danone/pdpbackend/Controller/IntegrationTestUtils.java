package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Controller.auth.AuthenticationRequest;
import com.danone.pdpbackend.Controller.auth.AuthenticationResponse;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Utility class for integration tests providing generic CRUD operations.
 *
 * @param <T> The entity type for which CRUD operations will be performed
 */
public class IntegrationTestUtils<T> {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final Class<T> entityClass;

    @Getter
    private String authToken;


    public IntegrationTestUtils(MockMvc mockMvc, ObjectMapper objectMapper, String baseUrl, Class<T> entityClass) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.entityClass = entityClass;
    }


    public String authenticate(String username, String password) throws Exception {
        AuthenticationRequest request = new AuthenticationRequest(username, password);

        MvcResult result = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<AuthenticationResponse> response = parseResponse(
                result, new TypeReference<ApiResponse<AuthenticationResponse>>() {}
        );

        authToken = response.getData().getToken();
        return authToken;
    }
    static String authenticate(MockMvc mockMvc,ObjectMapper objectMapper, String username, String password) throws Exception {
        AuthenticationRequest request = new AuthenticationRequest(username, password);

        MvcResult result = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<AuthenticationResponse> response = parseResponse(
                objectMapper,
                result, new TypeReference<ApiResponse<AuthenticationResponse>>() {}
        );

        return response.getData().getToken();
    }


    public T create(T entity) throws Exception {
        MvcResult result = mockMvc.perform(post(baseUrl + "/")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entity)))
                .andExpect(status().isOk())
                .andReturn();

        return parseResponseData(result, entityClass);
    }


    public T create(T entity, String successMessage) throws Exception {
        MvcResult result = mockMvc.perform(post(baseUrl + "/")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(successMessage))
                .andReturn();

        return parseResponseData(result, entityClass);
    }

    public T getById(Long id) throws Exception {
        MvcResult result = mockMvc.perform(get(baseUrl + "/{id}", id)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        return parseResponseData(result, entityClass);
    }

    public List<T> getAll(String path) throws Exception {
        String url = path != null ? baseUrl + path : baseUrl + "/all";

        MvcResult result = mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        return parseResponseDataList(result, entityClass);
    }

    public T update(Long id, T updateData) throws Exception {
        MvcResult result = mockMvc.perform(patch(baseUrl + "/{id}", id)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andReturn();

        return parseResponseData(result, entityClass);
    }


    public T updatePut(Long id, T updateData) throws Exception {
        MvcResult result = mockMvc.perform(put(baseUrl + "/{id}", id)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andReturn();

        return parseResponseData(result, entityClass);
    }

    public void deleteById(Long id) throws Exception {
        mockMvc.perform(delete(baseUrl + "/{id}", id)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
    }

    public <R> R customGet(String endpoint, Class<R> clazz) throws Exception {
        MvcResult result = mockMvc.perform(get(baseUrl + endpoint)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        return parseResponseData(result, clazz);
    }

    public <P, R> R customPost(String endpoint, P payload, Class<R> clazz) throws Exception {
        MvcResult result = mockMvc.perform(post(baseUrl + endpoint)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn();

        return parseResponseData(result, clazz);
    }

    private <R> ApiResponse<R> parseResponse(MvcResult result, TypeReference<ApiResponse<R>> typeReference) throws IOException {
        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseJson, typeReference);
    }

    static <R> ApiResponse<R> parseResponse(ObjectMapper objectMapper, MvcResult result, TypeReference<ApiResponse<R>> typeReference) throws IOException {
        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseJson, typeReference);
    }

    private <R> R parseResponseData(MvcResult result, Class<R> clazz) throws IOException {
        String responseJson = result.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseJson);
        JsonNode dataNode = rootNode.get("data");
        return objectMapper.treeToValue(dataNode, clazz);
    }

    private <R> List<R> parseResponseDataList(MvcResult result, Class<R> clazz) throws IOException {
        String responseJson = result.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseJson);
        JsonNode dataNode = rootNode.get("data");
        return objectMapper.readValue(
                dataNode.toString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, clazz)
        );
    }

}