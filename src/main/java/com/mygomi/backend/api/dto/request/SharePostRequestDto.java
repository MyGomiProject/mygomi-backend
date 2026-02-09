package com.mygomi.backend.api.dto.request;

import com.mygomi.backend.domain.share.ShareCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SharePostRequestDto {

    // ✅ 프론트에서 입력하는 것만!
    private String title;
    private String description;
    private ShareCategory category;
}