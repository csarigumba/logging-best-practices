package com.example.logging.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Practice 5 (log sampling): always keep error/warn-worthy outcomes, but sample
 * successful high-traffic requests at a configurable rate to cut volume/cost.
 */
@Component
public class Sampler {

    private final double sampleRate;

    public Sampler(@Value("${logging.sample-rate:0.2}") double sampleRate) {
        this.sampleRate = sampleRate;
    }

    /** @param status HTTP status; statuses &gt;= 400 are always logged. */
    public boolean shouldLog(int status) {
        if (status >= 400) {
            return true;
        }
        return ThreadLocalRandom.current().nextDouble() < sampleRate;
    }
}
