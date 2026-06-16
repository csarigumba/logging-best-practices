package com.example.logging;

import com.example.logging.web.Sampler;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SamplerTest {
    @Test
    void alwaysKeepsErrorsEvenAtZeroRate() {
        Sampler sampler = new Sampler(0.0);
        assertTrue(sampler.shouldLog(500));
        assertTrue(sampler.shouldLog(404));
    }

    @Test
    void dropsSuccessAtZeroRate() {
        Sampler sampler = new Sampler(0.0);
        assertFalse(sampler.shouldLog(200));
    }

    @Test
    void keepsSuccessAtFullRate() {
        Sampler sampler = new Sampler(1.0);
        assertTrue(sampler.shouldLog(200));
    }
}
