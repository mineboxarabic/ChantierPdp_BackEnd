package com.danone.pdpbackend.Controller; // Or a common test utility package

import com.danone.pdpbackend.Controller.auth.AuthenticationRequest;
import com.danone.pdpbackend.Controller.auth.AuthenticationResponse;
import com.danone.pdpbackend.Repo.UsersRepo;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.entities.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // Loads the full application context
@AutoConfigureMockMvc // Configures MockMvc
@ActiveProfiles("test") // Ensures your application-test.yml is used
@Transactional // Rolls back database changes after each test method
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Allows @BeforeAll and @AfterAll to be non-static if needed
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc; // 'protected' so subclasses can use it

    @Autowired
    protected ObjectMapper objectMapper; // 'protected' for subclasses

    @Autowired
    private UsersRepo usersRepo; // For creating the test user

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // For encoding passwords

    protected String authToken; // To store the auth token for test methods
    protected User testUserYassin4; // To store the created test user instance

    // This method runs once for the entire test class (due to @TestInstance(TestInstance.Lifecycle.PER_CLASS))
    // If you prefer it to run before each test class context is set up (more complex),
    // you might need TestExecutionListeners or static initializers.
    // For ObjectMapper, this is fine here.
    @BeforeAll
    void globalSetup() {
        objectMapper.registerModule(new JavaTimeModule());
        // Any other one-time setup for all tests inheriting this class
    }

    @BeforeEach
    void setUpGlobalTestDataAndAuth() throws Exception {
        // 1. Create the "Yassin4" user programmatically if they don't exist
        // This makes your tests independent of data.sql for this specific user.
        Optional<User> existingUser = usersRepo.findByUsername("Yassin4");
        if (existingUser.isEmpty()) {
            User userToCreate = User.builder()
                    .username("Yassin4")
                    .email("yassin4.test@example.com") // Ensure email is unique if you have constraints
                    .password(passwordEncoder.encode("Zaqwe123!")) // Use the same password as in your tests
                    .fonction("Test Dev From Base")
                    .notel("0102030405")
                    .build();
            testUserYassin4 = usersRepo.save(userToCreate);
            System.out.println("INFO: Test user 'Yassin4' created programmatically in AbstractIntegrationTest.");
        } else {
            testUserYassin4 = existingUser.get();
            System.out.println("INFO: Test user 'Yassin4' already exists, using existing user in AbstractIntegrationTest.");
        }
        assertNotNull(testUserYassin4, "Test user Yassin4 must exist or be creatable for tests.");
        assertNotNull(testUserYassin4.getId(), "Test user Yassin4 must have an ID after save/fetch.");


        // 2. Authenticate and get token for "Yassin4"
        AuthenticationRequest loginRequest = new AuthenticationRequest("Yassin4", "Zaqwe123!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseJson = loginResult.getResponse().getContentAsString();

        // Assuming ApiResponse structure: { "data": { "token": "...", "user": {...} }, "message": "..." }
        TypeReference<ApiResponse<AuthenticationResponse>> typeRef = new TypeReference<>() {};
        ApiResponse<AuthenticationResponse> authApiResponse = objectMapper.readValue(loginResponseJson, typeRef);

        assertNotNull(authApiResponse, "Auth API response should not be null.");
        assertNotNull(authApiResponse.getData(), "Auth API response data should not be null.");

        authToken = authApiResponse.getData().getToken();
        assertNotNull(authToken, "Auth token should not be null after login.");

        // You can also store the authenticated user object if needed by subclasses
        // testUserYassin4 = authApiResponse.getData().getUser();
        // assertNotNull(testUserYassin4, "Authenticated user object should not be null.");
    }

    // You can add common helper methods here that your other test classes might use,
    // for example, for parsing responses or creating other common entities.
    protected <T> ApiResponse<T> parseApiResponse(MvcResult result, TypeReference<ApiResponse<T>> typeReference) throws Exception {
        String jsonResponse = result.getResponse().getContentAsString();
        return objectMapper.readValue(jsonResponse, typeReference);
    }

    protected <T> T parseApiData(MvcResult result, Class<T> dataClass) throws Exception {
        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse<Map<String,Object>> tempResponse = objectMapper.readValue(jsonResponse, new TypeReference<ApiResponse<Map<String,Object>>>() {});
        return objectMapper.convertValue(tempResponse.getData(), dataClass);
    }
}
