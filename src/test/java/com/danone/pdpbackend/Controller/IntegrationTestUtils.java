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
    /**
     * -- GETTER --
     *  Gets the stored authentication token.
     *
     * @return The authentication token
     */
    @Getter
    private String authToken;

    /**
     * Constructor for IntegrationTestUtils.
     *
     * @param mockMvc      The MockMvc instance for performing HTTP requests
     * @param objectMapper The ObjectMapper for JSON serialization/deserialization
     * @param baseUrl      The base URL for the API endpoints (e.g., "/api/chantier")
     * @param entityClass  The Class object for the entity type
     */
    public IntegrationTestUtils(MockMvc mockMvc, ObjectMapper objectMapper, String baseUrl, Class<T> entityClass) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.entityClass = entityClass;
    }

    /**
     * Authenticates with the API and stores the JWT token for subsequent requests.
     *
     * @param username The username for authentication
     * @param password The password for authentication
     * @return The JWT token
     * @throws Exception if authentication fails
     */
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

    /**
     * Creates an entity via API.
     *
     * @param entity The entity to create
     * @return The created entity
     * @throws Exception if the request fails
     */
    public T create(T entity) throws Exception {
        MvcResult result = mockMvc.perform(post(baseUrl + "/")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entity)))
                .andExpect(status().isOk())
                .andReturn();

        return parseResponseData(result, entityClass);
    }

    /**
     * Creates an entity via API and expects a specific success message.
     *
     * @param entity        The entity to create
     * @param successMessage The expected success message
     * @return The created entity
     * @throws Exception if the request fails
     */
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

    /**
     * Gets an entity by ID via API.
     *
     * @param id The ID of the entity to retrieve
     * @return The retrieved entity
     * @throws Exception if the request fails
     */
    public T getById(Long id) throws Exception {
        MvcResult result = mockMvc.perform(get(baseUrl + "/{id}", id)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        return parseResponseData(result, entityClass);
    }

    /**
     * Gets all entities via API.
     *
     * @param path Optional additional path after the base URL (e.g., "/all")
     * @return List of all entities
     * @throws Exception if the request fails
     */
    public List<T> getAll(String path) throws Exception {
        String url = path != null ? baseUrl + path : baseUrl + "/all";

        MvcResult result = mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        return parseResponseDataList(result, entityClass);
    }

    /**
     * Updates an entity via API.
     *
     * @param id         The ID of the entity to update
     * @param updateData The updated entity data
     * @return The updated entity
     * @throws Exception if the request fails
     */
    public T update(Long id, T updateData) throws Exception {
        MvcResult result = mockMvc.perform(patch(baseUrl + "/{id}", id)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andReturn();

        return parseResponseData(result, entityClass);
    }

    /**
     * Updates an entity via API with a PUT request.
     *
     * @param id         The ID of the entity to update
     * @param updateData The updated entity data
     * @return The updated entity
     * @throws Exception if the request fails
     */
    public T updatePut(Long id, T updateData) throws Exception {
        MvcResult result = mockMvc.perform(put(baseUrl + "/{id}", id)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andReturn();

        return parseResponseData(result, entityClass);
    }

    /**
     * Deletes an entity via API.
     *
     * @param id The ID of the entity to delete
     * @throws Exception if the request fails
     */
    public void deleteById(Long id) throws Exception {
        mockMvc.perform(delete(baseUrl + "/{id}", id)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
    }

    /**
     * Performs a GET request to a custom endpoint.
     *
     * @param endpoint The endpoint to call
     * @param clazz    The expected return type
     * @return The response parsed to the specified type
     * @throws Exception if the request fails
     */
    public <R> R customGet(String endpoint, Class<R> clazz) throws Exception {
        MvcResult result = mockMvc.perform(get(baseUrl + endpoint)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        return parseResponseData(result, clazz);
    }

    /**
     * Performs a POST request to a custom endpoint.
     *
     * @param endpoint The endpoint to call
     * @param payload  The request payload
     * @param clazz    The expected return type
     * @return The response parsed to the specified type
     * @throws Exception if the request fails
     */
    public <P, R> R customPost(String endpoint, P payload, Class<R> clazz) throws Exception {
        MvcResult result = mockMvc.perform(post(baseUrl + endpoint)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn();

        return parseResponseData(result, clazz);
    }

    /**
     * Parses API response into specified type.
     *
     * @param result       The MvcResult from the request
     * @param typeReference The TypeReference for the response type
     * @return The parsed response
     * @throws IOException if parsing fails
     */
    private <R> ApiResponse<R> parseResponse(MvcResult result, TypeReference<ApiResponse<R>> typeReference) throws IOException {
        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseJson, typeReference);
    }

    static <R> ApiResponse<R> parseResponse(ObjectMapper objectMapper, MvcResult result, TypeReference<ApiResponse<R>> typeReference) throws IOException {
        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseJson, typeReference);
    }

    /**
     * Parses data field from API response into specified type.
     *
     * @param result The MvcResult from the request
     * @param clazz  The Class of the expected data type
     * @return The parsed data
     * @throws IOException if parsing fails
     */
    private <R> R parseResponseData(MvcResult result, Class<R> clazz) throws IOException {
        String responseJson = result.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseJson);
        JsonNode dataNode = rootNode.get("data");
        return objectMapper.treeToValue(dataNode, clazz);
    }

    /**
     * Parses data field from API response into list of specified type.
     *
     * @param result The MvcResult from the request
     * @param clazz  The Class of the list elements
     * @return The parsed list
     * @throws IOException if parsing fails
     */
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