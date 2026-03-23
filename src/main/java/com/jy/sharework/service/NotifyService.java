package com.jy.sharework.service;

import cn.hutool.json.JSONUtil;
import com.jy.sharework.entity.BizMessage;
import com.jy.sharework.mapper.BizMessageMapper;
import com.jy.sharework.websocket.NotifyWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotifyService {
    private final BizMessageMapper bizMessageMapper;
    private final NotifyWebSocketHandler notifyWebSocketHandler;

    public void send(Long userId, String title, String content, String wsEvent) {
        BizMessage m = new BizMessage();
        m.setUserId(userId);
        m.setTitle(title);
        m.setContent(content);
        m.setIsRead(0);
        bizMessageMapper.insert(m);

        Map<String, Object> payload = new HashMap<>();
        payload.put("event", wsEvent);
        payload.put("title", title);
        payload.put("content", content);
        payload.put("messageId", m.getId());
        notifyWebSocketHandler.sendToUser(userId, JSONUtil.toJsonStr(payload));
    }
}
