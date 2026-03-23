package com.jy.sharework.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NotifyWebSocketHandler extends TextWebSocketHandler {
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Object uid = session.getAttributes().get("userId");
        if (uid instanceof Long) {
            sessions.put((Long) uid, session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object uid = session.getAttributes().get("userId");
        if (uid instanceof Long) {
            sessions.remove((Long) uid, session);
        }
    }

    public void sendToUser(Long userId, String json) {
        WebSocketSession s = sessions.get(userId);
        if (s != null && s.isOpen()) {
            try {
                s.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                log.warn("ws send fail user={}", userId, e);
            }
        }
    }
}
