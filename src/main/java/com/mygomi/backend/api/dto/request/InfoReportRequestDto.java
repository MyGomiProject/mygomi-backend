package com.mygomi.backend.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class InfoReportRequestDto {

    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 200, message = "제목은 200자 이내로 입력해주세요")
    private String title;

    @NotBlank(message = "내용을 입력해주세요")
    private String content;

    private boolean emailReply;     // 이메일 답변 수신 여부
    private String reporterEmail;   // 이메일 답변 원할 때만 입력
    // 첨부파일은 multipart로 별도 처리
}
