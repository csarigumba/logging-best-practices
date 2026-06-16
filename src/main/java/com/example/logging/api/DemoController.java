package com.example.logging.api;

import com.example.logging.domain.DemoService;
import com.example.logging.domain.SafeUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DemoController {

    private final DemoService service;

    public DemoController(DemoService service) {
        this.service = service;
    }

    @GetMapping("/orders/{id}")
    public Map<String, Object> getOrder(@PathVariable String id) {
        return service.getOrder(id);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest body) {
        SafeUser user = service.login(body.username(), body.password());
        return Map.of("status", "ok", "userId", user.id());
    }

    @GetMapping("/flaky")
    public Map<String, Object> flaky() {
        return service.flaky();
    }

    public record LoginRequest(String username, String password) {}
}
