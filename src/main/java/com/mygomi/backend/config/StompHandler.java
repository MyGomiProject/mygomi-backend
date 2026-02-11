package com.mygomi.backend.config;

import com.mygomi.backend.domain.chat.ChatRoom;
import com.mygomi.backend.domain.user.User;
import com.mygomi.backend.repository.ChatRoomRepository;
import com.mygomi.backend.repository.UserRepository;
import com.mygomi.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final ChatRoomRepository chatRoomRepository; // DB 조회를 위해 추가
    private final UserRepository userRepository;         // DB 조회를 위해 추가

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        System.out.println("STOMP 명령: " + accessor.getCommand());

        // 1. 연결 요청 (CONNECT) - 토큰 검증
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            System.out.println("연결 요청 들어옴! 토큰 검사 시작");
            String rawToken = accessor.getFirstNativeHeader("Authorization");
            if (rawToken != null && rawToken.startsWith("Bearer ")) {
                String token = rawToken.substring(7);
                if (jwtTokenProvider.validateToken(token)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    accessor.setUser(authentication);
                }
            }
        }

        // 2. 구독 요청 (SUBSCRIBE) - 채팅방 권한 검증 ★ [추가된 보안 로직]
        else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination(); // 예: /sub/chat/room/1

            if (destination != null && destination.startsWith("/sub/chat/room/")) {
                Long roomId = Long.parseLong(destination.substring(15)); // 방 번호 추출
                String userEmail = accessor.getUser().getName(); // 접속자 이메일

                User user = userRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));

                ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                        .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found"));

                // 참여자(구매자 or 판매자)가 아니면 구독 차단
                if (!chatRoom.getBuyer().getId().equals(user.getId()) &&
                        !chatRoom.getSeller().getId().equals(user.getId())) {
                    log.warn("무단 침입 시도 감지! User: {}, Room: {}", userEmail, roomId);
                    throw new IllegalArgumentException("이 채팅방에 입장할 수 없습니다.");
                }
            }
        }

        return message;
    }
}