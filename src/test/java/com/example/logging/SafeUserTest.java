package com.example.logging;

import com.example.logging.domain.SafeUser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SafeUserTest {
    @Test
    void toStringExposesOnlyId() {
        SafeUser user = new SafeUser(42L, "alice", "alice@example.com");
        String s = user.toString();
        assertTrue(s.contains("42"), "should contain id");
        assertFalse(s.contains("alice"), "must not contain username");
        assertFalse(s.contains("example.com"), "must not contain email");
    }
}
