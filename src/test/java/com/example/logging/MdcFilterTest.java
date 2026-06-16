package com.example.logging;

import com.example.logging.web.MdcFilter;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class MdcFilterTest {
    @Test
    void setsRequestIdDuringChainAndClearsAfter() throws Exception {
        MdcFilter filter = new MdcFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders/1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> seenInsideChain = new AtomicReference<>();

        filter.doFilter(request, response, (req, res) ->
                seenInsideChain.set(MDC.get(MdcFilter.REQUEST_ID)));

        assertNotNull(seenInsideChain.get(), "requestId must be set inside the chain");
        assertNotNull(response.getHeader("X-Request-Id"), "requestId echoed on response");
        assertNull(MDC.get(MdcFilter.REQUEST_ID), "MDC must be cleared after the request");
    }

    @Test
    void honorsInboundRequestIdHeader() throws Exception {
        MdcFilter filter = new MdcFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders/1");
        request.addHeader("X-Request-Id", "abc-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> seen = new AtomicReference<>();

        filter.doFilter(request, response, (req, res) -> seen.set(MDC.get(MdcFilter.REQUEST_ID)));

        assertEquals("abc-123", seen.get());
    }

    @Test
    void rejectsMaliciousRequestIdHeader() throws Exception {
        MdcFilter filter = new MdcFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders/1");
        // CWE-117: a forged id with newline + fake log line must not be trusted
        request.addHeader("X-Request-Id", "abc\nINFO injected fake log line");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> seen = new AtomicReference<>();

        filter.doFilter(request, response, (req, res) -> seen.set(MDC.get(MdcFilter.REQUEST_ID)));

        assertFalse(seen.get().contains("\n"), "malicious id must be discarded");
        assertFalse(seen.get().contains("injected"), "malicious id must be discarded");
    }
}
