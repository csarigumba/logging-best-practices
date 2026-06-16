package com.example.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.logging.web.CanonicalLogFilter;
import com.example.logging.web.Sampler;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class CanonicalLogFilterTest {

    private ListAppender<ILoggingEvent> attachAppender() {
        Logger canonical = (Logger) LoggerFactory.getLogger("canonical");
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        canonical.addAppender(appender);
        return appender;
    }

    @Test
    void emitsExactlyOneCanonicalLinePerRequest() throws Exception {
        ListAppender<ILoggingEvent> appender = attachAppender();
        CanonicalLogFilter filter = new CanonicalLogFilter(new Sampler(1.0));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders/7");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> ((MockHttpServletResponse) res).setStatus(200));

        assertEquals(1, appender.list.size());
        String msg = appender.list.get(0).getFormattedMessage();
        assertTrue(msg.contains("method=GET"));
        assertTrue(msg.contains("path=/api/orders/7"));
        assertTrue(msg.contains("status=200"));
        assertTrue(msg.contains("duration_ms="));
    }

    @Test
    void sampledOutSuccessEmitsNothing() throws Exception {
        ListAppender<ILoggingEvent> appender = attachAppender();
        CanonicalLogFilter filter = new CanonicalLogFilter(new Sampler(0.0));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders/7");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> ((MockHttpServletResponse) res).setStatus(200));

        assertEquals(0, appender.list.size(), "sampled-out success should not log");
    }
}
