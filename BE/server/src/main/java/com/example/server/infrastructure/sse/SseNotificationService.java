package com.example.server.infrastructure.sse;

import com.example.server.infrastructure.websocket.WebSocketMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseNotificationService {

    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emittersMap = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String topic) {
        // Use 0 or -1 for no timeout. SseEmitter defaults to container timeout, we can set a long one or -1/0.
        // Spring Boot defaults might disconnect after 30s without this.
        SseEmitter emitter = new SseEmitter(0L); 
        emittersMap.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(topic, emitter));
        emitter.onTimeout(() -> removeEmitter(topic, emitter));
        emitter.onError(e -> removeEmitter(topic, emitter));

        return emitter;
    }

    private void removeEmitter(String topic, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersMap.get(topic);
        if (emitters != null) {
            emitters.remove(emitter);
        }
    }

    public <T> void sendToTopic(String topic, String type, T payload) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersMap.get(topic);
        if (emitters != null) {
            Object message = WebSocketMessage.of(type, topic, payload);
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().data(message));
                } catch (Exception e) {
                    removeEmitter(topic, emitter);
                }
            }
        }
    }
}
