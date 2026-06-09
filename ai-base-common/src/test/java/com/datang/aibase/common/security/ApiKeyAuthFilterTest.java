package com.datang.aibase.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ApiKeyAuthFilterTest {

    private ApiKeyStore store;
    private ApiKeyAuthFilter filter;

    @BeforeEach
    void setUp() {
        store = mock(ApiKeyStore.class);
        filter = new ApiKeyAuthFilter(store);
    }

    @Test
    @DisplayName("returns 401 when X-Api-Key header is missing")
    void missingHeader_returns401() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/test");
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Missing X-Api-Key");
        verifyNoInteractions(chain);
    }

    @Test
    @DisplayName("returns 401 when X-Api-Key header is blank")
    void blankHeader_returns401() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/test");
        request.addHeader("X-Api-Key", "   ");
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        verifyNoInteractions(chain);
    }

    @Test
    @DisplayName("returns 401 when API key is invalid")
    void invalidKey_returns401() throws Exception {
        when(store.isValid("bad-key")).thenReturn(false);
        var request = new MockHttpServletRequest("GET", "/api/test");
        request.addHeader("X-Api-Key", "bad-key");
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Invalid X-Api-Key");
        verifyNoInteractions(chain);
    }

    @Test
    @DisplayName("continues filter chain when API key is valid")
    void validKey_continuesChain() throws Exception {
        when(store.isValid("good-key")).thenReturn(true);
        var request = new MockHttpServletRequest("GET", "/api/test");
        request.addHeader("X-Api-Key", "good-key");
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
