package com.mygomi.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReservationStatusResponseDto {
    private Long postId;
    private String postStatus;   // OPEN, RESERVED, COMPLETED, DELETED
    private Boolean myAgreed;
    private Boolean otherAgreed;
    private Boolean bothAgreed;
}
