package com.example.logging.domain;

/**
 * A user type safe to log. Practice 10 ("what not to log"): even if the whole
 * object is logged by accident, toString() only ever exposes the id — never the
 * username, email, or any other PII.
 */
public record SafeUser(Long id, String username, String email) {
    @Override
    public String toString() {
        return "SafeUser[id=" + id + "]";
    }
}
