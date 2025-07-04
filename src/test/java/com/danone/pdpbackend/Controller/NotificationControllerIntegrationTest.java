package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Controller.auth.AuthenticationRequest;
import com.danone.pdpbackend.Controller.auth.AuthenticationResponse;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.NotificationType;
import com.danone.pdpbackend.entities.Notification;
import com.danone.pdpbackend.entities.User;
import com.danone.pdpbackend.entities.dto.NotificationDTO;
import com.danone.pdpbackend.Repo.NotificationRepo;
import com.danone.pdpbackend.Repo.UsersRepo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rollback DB changes after each test
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // To use @BeforeAll non-static
@DisplayName("Notification Controller Integration Tests!")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS) // Clean up context after all tests in this class
public class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepo notificationRepo; // For setup and verification

    @Autowired
    private UsersRepo usersRepo; // For fetching the test user

    private String authToken;
    private User testUser;

    @BeforeAll
    void setupAll() {
        objectMapper.registerModule(new JavaTimeModule());
        // Ensure the test user exists or create them here if necessary
        // For simplicity, assuming "Yassin4" is findable by username/email
    }

    @BeforeEach
    void setUpEach() throws Exception {
        // Authenticate and get token
        AuthenticationRequest loginRequest = new AuthenticationRequest("Yassin4", "Zaqwe123!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String loginResponseJson = loginResult.getResponse().getContentAsString();
        ApiResponse<AuthenticationResponse> authApiResponse = objectMapper.readValue(loginResponseJson, new TypeReference<>() {});
        authToken = authApiResponse.getData().getToken();
        assertNotNull(authToken, "Token should not be null, merde!");

        // Fetch the test user by the username used for authentication
        // This assumes your UserDetails service loads by username, and that username is unique.
        // Adjust if your primary login identifier is email and it's stored in User.email
        testUser = usersRepo.findByUsername("Yassin4")
                .orElseThrow(() -> new IllegalStateException("Test user Yassin4 not found. Create this user for tests, bordel!"));
    }

    private Notification createTestNotification(User user, String message, boolean isRead, NotificationType type) {
        return notificationRepo.save(Notification.builder()
                .targetUser(user)
                .message(message)
                .isRead(isRead)
                .type(type)
                .timestamp(LocalDateTime.now().minusHours(1)) // Ensure it's sortable
                .relatedEntityId(1L)
                .relatedEntityType("TestEntity")
                .callToActionLink("/test/1")
                .build());
    }
    @Getter
    public static class CustomPage<T> {
        private List<T> content;
        private int number;
        private int size;
        private int totalPages;
        private long totalElements;
        private boolean first;
        private boolean last;
        private boolean empty;
    }
    @Test
    @DisplayName("Get Unread Notifications - Should return unread ones, sacré bleu!")
    void getUnreadNotifications_shouldReturnUnread() throws Exception {
        notificationRepo.deleteAll(); // Clean slate for this test
        createTestNotification(testUser, "Unread Message 1", false, NotificationType.CHANTIER_STATUS_BAD);
        createTestNotification(testUser, "Read Message 1", true, NotificationType.DOCUMENT_COMPLETED);
        createTestNotification(testUser, "Unread Message 2", false, NotificationType.DOCUMENT_ACTION_NEEDED);

        MvcResult result = mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + authToken)
                        .param("readStatus", "false")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Notifications récupérées. (Notifications retrieved.)")))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].message", is("Unread Message 2"))) // Sorted by timestamp desc
                .andExpect(jsonPath("$.data.content[0].read", is(false)))
                .andExpect(jsonPath("$.data.content[1].message", is("Unread Message 1")))
                .andExpect(jsonPath("$.data.content[1].read", is(false)))
                .andReturn();

        // You can deserialize and assert more if you want, but JSONPath is often enough
        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse<CustomPage<NotificationDTO>> apiResponse = objectMapper.readValue(jsonResponse, new TypeReference<>() {});
        assertEquals(2, apiResponse.getData().getTotalElements());
    }

    @Test
    @DisplayName("Get All Notifications - Should return all!")
    void getAllNotifications_shouldReturnAll() throws Exception {
        notificationRepo.deleteAll();
        createTestNotification(testUser, "Unread Message All", false, NotificationType.CHANTIER_STATUS_BAD);
        createTestNotification(testUser, "Read Message All", true, NotificationType.DOCUMENT_COMPLETED);

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + authToken)
                        .param("readStatus", "all") // Test the "all" filter
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)));
    }


    @Test
    @DisplayName("Mark Notification As Read - Should work!")
    void markNotificationAsRead_shouldUpdateStatus() throws Exception {
        Notification unreadNotif = createTestNotification(testUser, "To Be Read", false, NotificationType.CHANTIER_PENDING_BDT);

        mockMvc.perform(patch("/api/notifications/{id}/read", unreadNotif.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Notification marquée comme lue. (Notification marked as read.)")))
                .andExpect(jsonPath("$.data.id", is(unreadNotif.getId().intValue())))
                .andExpect(jsonPath("$.data.read", is(true)));

        Notification updatedNotif = notificationRepo.findById(unreadNotif.getId()).orElseThrow();
        assertTrue(updatedNotif.isRead(), "Notification should be marked as read in DB, merde!");
    }

    @Test
    @DisplayName("Mark Notification As Read - Wrong User - Should be Forbidden!")
    void markNotificationAsRead_wrongUser_shouldBeForbidden() throws Exception {
        // Create another user
        User anotherUser = usersRepo.save(User.builder().username("AnotherUser"+System.nanoTime()).email("another"+System.nanoTime()+"@example.com").password("secret").build());
        Notification othersNotification = createTestNotification(anotherUser, "Not Yours", false, NotificationType.CHANTIER_STATUS_BAD);

        mockMvc.perform(patch("/api/notifications/{id}/read", othersNotification.getId())
                        .header("Authorization", "Bearer " + authToken)) // Authenticated as testUser
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("You can only mark your own notifications as read")));
    }

    @Test
    @DisplayName("Mark Notification As Read - Not Found - Should be 404.")
    void markNotificationAsRead_notFound_shouldBeNotFound() throws Exception {
        long nonExistentId = 99999L;
        mockMvc.perform(patch("/api/notifications/{id}/read", nonExistentId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Notification not found")));
    }

    @Test
    @DisplayName("Mark All Notifications As Read - Should update all for user, et voilà!")
    void markAllNotificationsAsRead_shouldUpdateAllForUser() throws Exception {
        notificationRepo.deleteAll();
        createTestNotification(testUser, "Unread A", false, NotificationType.CHANTIER_STATUS_BAD);
        createTestNotification(testUser, "Unread B", false, NotificationType.DOCUMENT_ACTION_NEEDED);
        createTestNotification(testUser, "Already Read C", true, NotificationType.DOCUMENT_COMPLETED);
        // Create a notification for another user to ensure it's not affected
        User otherUser = usersRepo.save(User.builder().username("OtherDude"+System.nanoTime()).email("otherdude"+System.nanoTime()+"@example.com").password("pass").build());
        createTestNotification(otherUser, "Other User Unread", false, NotificationType.SYSTEM_ALERT);


        mockMvc.perform(post("/api/notifications/mark-all-read")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", is("2 notifications marquées comme lues."))) // Message contains count
                .andExpect(jsonPath("$.message", is("Toutes les notifications marquées comme lues. (All notifications marked as read.)")));

        assertEquals(0, notificationRepo.countByTargetUserAndIsReadFalse(testUser), "TestUser should have 0 unread notifications.");
        assertEquals(1, notificationRepo.countByTargetUserAndIsReadFalse(otherUser), "OtherUser's unread count should be unaffected.");
    }

    @Test
    @DisplayName("Get Unread Notification Count - Should return correct count, bien sûr!")
    void getUnreadNotificationCount_shouldReturnCorrectCount() throws Exception {
        notificationRepo.deleteAll();
        createTestNotification(testUser, "Unread Count 1", false, NotificationType.CHANTIER_STATUS_BAD);
        createTestNotification(testUser, "Unread Count 2", false, NotificationType.DOCUMENT_ACTION_NEEDED);
        createTestNotification(testUser, "Read Count 1", true, NotificationType.DOCUMENT_COMPLETED);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", is(2)))
                .andExpect(jsonPath("$.message", is("Nombre de notifications non lues. (Unread notification count.)")));
    }

    @Test
    @DisplayName("Get Notifications - No Auth - Should be Unauthorized, dégage!")
    void getNotifications_noAuth_shouldBeUnauthorized() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().is4xxClientError()); // Or 403 if your filter chain setup leads to that for missing token
    }
}