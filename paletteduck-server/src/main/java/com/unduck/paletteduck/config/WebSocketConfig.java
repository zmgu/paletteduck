package com.unduck.paletteduck.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");  // /queue 추가 (사용자별 메시지용)
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");  // 사용자별 메시지 prefix 설정
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:5173")
                .withSockJS()
                .setStreamBytesLimit(52428800)        // 50MB
                .setHttpMessageCacheSize(100000)
                .setDisconnectDelay(30000);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration
                .setMessageSizeLimit(52428800)        // 50MB
                .setSendBufferSizeLimit(52428800)     // 50MB
                .setSendTimeLimit(60000);             // 60초
    }
}