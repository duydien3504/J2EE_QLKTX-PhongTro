package com.group10.API_ManageDormitory.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityUtilsTest {

    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUsername_authenticated_returnsUsername() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("testUser");
        when(authentication.getName()).thenReturn("testUser");

        String username = SecurityUtils.getCurrentUsername();
        assertEquals("testUser", username);
    }

    @Test
    void getCurrentUsername_anonymous_returnsNull() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        String username = SecurityUtils.getCurrentUsername();
        assertNull(username);
    }

    @Test
    void hasRole_withRole_returnsTrue() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(List.of(new SimpleGrantedAuthority("SCOPE_ADMIN"))).when(authentication).getAuthorities();

        assertTrue(SecurityUtils.hasRole("SCOPE_ADMIN"));
    }

    @Test
    void hasRole_withoutRole_returnsFalse() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(List.of(new SimpleGrantedAuthority("SCOPE_USER"))).when(authentication).getAuthorities();

        assertFalse(SecurityUtils.hasRole("SCOPE_ADMIN"));
    }
}
