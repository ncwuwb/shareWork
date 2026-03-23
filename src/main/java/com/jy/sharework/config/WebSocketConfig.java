package com.jy.sharework.config;

import com.jy.sharework.websocket.NotifyWebSocketHandler;
import com.jy.sharework.websocket.WsHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 握手会校验浏览器 {@code Origin}。
 * uni-app H5 / 各端口开发服务器与后台端口不一致，若仅用 setAllowedOrigins(5173 等) 会导致握手 403。
 * 开发环境使用 {@code setAllowedOriginPatterns("*")}；上线请改为具体域名白名单。
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final NotifyWebSocketHandler notifyWebSocketHandler;
    private final WsHandshakeInterceptor wsHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notifyWebSocketHandler, "/ws/notify")
                .addInterceptors(wsHandshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
