package com.luv2code.springbootlibrary.security;

import com.luv2code.springbootlibrary.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoleBasedAccessSecurityTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private UserRepository userRepository; // pour éviter les appels réels

    @Test
    @WithMockUser(roles = "STUDENT")
    void studentCannotAccessAdminAudit() throws Exception {
        mockMvc.perform(get("/api/admin/audit/logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessAdminAudit() throws Exception {
        mockMvc.perform(get("/api/admin/audit/logs"))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedUserCannotAccessAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/audit/logs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void studentCanAccessPublicBookSearch() throws Exception {
        mockMvc.perform(get("/api/books/search/title").param("title", "test"))
                .andExpect(status().isOk());
    }
}