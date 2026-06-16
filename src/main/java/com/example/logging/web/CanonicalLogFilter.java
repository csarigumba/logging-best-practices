package com.example.logging.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Practice 6 (canonical log lines): emit ONE summary line per request telling the
 * whole story — method, path, status, duration. Combined with practice 5: the
 * Sampler keeps every error but samples successful lines to control volume.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class CanonicalLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("canonical");
    private final Sampler sampler;

    public CanonicalLogFilter(Sampler sampler) {
        this.sampler = sampler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long startNanos = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            if (sampler.shouldLog(response.getStatus())) {
                log.info("canonical method={} path={} status={} duration_ms={}",
                        request.getMethod(), request.getRequestURI(),
                        response.getStatus(), durationMs);
            }
        }
    }
}
