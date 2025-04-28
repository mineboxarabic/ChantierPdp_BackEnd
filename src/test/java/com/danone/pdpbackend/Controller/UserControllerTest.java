package com.danone.pdpbackend.Controller;

import com.danone.pdpbackend.Services.UserService;
import com.danone.pdpbackend.entities.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void createUser_shouldReturnCreatedStatus_whenValidUserProvided() throws Exception {
        User validUser = new User();
        validUser.setUsername("John Doe");

        doNothing().when(userService).createUser(Mockito.any(User.class));

        String jsonRequest = """
                {
                    "name": "John Doe"
                }
                """;

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(content().string("User Created Successfully"));
    }
}