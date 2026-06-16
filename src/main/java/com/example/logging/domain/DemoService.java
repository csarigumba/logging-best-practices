package com.example.logging.domain;

import com.example.logging.web.MdcFilter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Demonstrates intentional log levels (practice 2), business context (practices
 * 1 &amp; 4), safe handling of PII (practice 10), and metrics alongside logs
 * (practice 12).
 */
@Service
public class DemoService {

    private static final Logger log = LoggerFactory.getLogger(DemoService.class);

    private final Counter ordersRetrieved;
    private final Counter flakyErrors;

    public DemoService(MeterRegistry registry) {
        this.ordersRetrieved = registry.counter("demo.orders.retrieved");
        this.flakyErrors = registry.counter("demo.flaky.errors");
    }

    public Map<String, Object> getOrder(String id) {
        log.info("order retrieved order_id={}", id);   // INFO: business-as-usual
        ordersRetrieved.increment();                   // practice 12: metric
        return Map.of("orderId", id, "status", "SHIPPED");
    }

    public SafeUser login(String username, String password) {
        // DEMO STUB — this performs NO real authentication; it exists only to
        // show that the password is never logged (practice 10). A real impl
        // would verify credentials (e.g. Spring Security AuthenticationManager
        // + bcrypt) and only populate MDC/userId on success.
        // practice 10: the password parameter is NEVER logged.
        SafeUser user = new SafeUser(42L, username, username + "@example.com");
        MDC.put(MdcFilter.USER_ID, String.valueOf(user.id()));
        log.info("login succeeded user={}", user);     // SafeUser.toString => id only
        return user;
    }

    public Map<String, Object> flaky() {
        int roll = ThreadLocalRandom.current().nextInt(3);
        if (roll == 0) {
            log.info("flaky call ok");
            return Map.of("result", "ok");
        }
        if (roll == 1) {
            log.warn("flaky call slow threshold_ms={} actual_ms={}", 200, 850); // WARN
            return Map.of("result", "slow");
        }
        flakyErrors.increment();
        try {
            throw new IllegalStateException("downstream payment service unavailable");
        } catch (RuntimeException e) {
            log.error("flaky call failed", e);          // ERROR with full stack trace
            return Map.of("result", "error");
        }
    }
}
