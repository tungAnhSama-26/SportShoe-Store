package com.example.server.infrastructure.sse;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/sse")
@CrossOrigin("*") // Or match your security config
public class SseController {

    private final SseNotificationService sseNotificationService;

    public SseController(SseNotificationService sseNotificationService) {
        this.sseNotificationService = sseNotificationService;
    }

    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe(@RequestParam String topic) {
        return sseNotificationService.subscribe(topic);
    }
}
