package com.mygomi.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // 웹소켓 메시지 브로커 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. 웹소켓 연결 주소 설정: /ws-stomp
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*") // 모든 도메인 허용 (테스트용)
                .withSockJS(); // 낮은 버전 브라우저 지원
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 2. 메시지 보낼 때 (발행): /pub 으로 시작하는 주소로 메시지를 보내면 컨트롤러가 받음
        registry.setApplicationDestinationPrefixes("/pub");

        // 3. 메시지 받을 때 (구독): /sub 으로 시작하는 주소를 구독하면 메시지를 전달받음
        registry.enableSimpleBroker("/sub");
    }
}