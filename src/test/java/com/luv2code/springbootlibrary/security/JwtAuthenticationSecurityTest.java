
/*

supprime ce code


package com.luv2code.springbootlibrary.security;

import com.luv2code.springbootlibrary.config.JwtUtils;
import com.luv2code.springbootlibrary.entity.User;
import com.luv2code.springbootlibrary.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtAuthenticationSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtUtils jwtUtils;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private UserRepository userRepository;

    private String validToken;

    @BeforeEach
    void setUp() {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("admin@bib.fr")
                .password("")
                .authorities("ROLE_ADMIN")
                .build();
        when(userDetailsService.loadUserByUsername("admin@bib.fr")).thenReturn(userDetails);

        User adminUser = new User();
        adminUser.setId(7L);
        adminUser.setEmail("admin@bib.fr");
        when(userRepository.findByEmail("admin@bib.fr")).thenReturn(Optional.of(adminUser));

        validToken = jwtUtils.generateToken(userDetails);
    }

    @Test
    void accessAdminWithValidJwt_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/admin/audit/logs")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    @Test
    void accessAdminWithInvalidJwt_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/audit/logs")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }
}



 */