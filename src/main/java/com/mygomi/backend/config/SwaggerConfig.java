package com.mygomi.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // 1. JWT 인증 스키마 정의 (헤더에 Bearer Token 포함)
        String jwtSchemeName = "jwtAuth";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP) // HTTP 방식
                        .scheme("bearer")
                        .bearerFormat("JWT")); // 토큰 형식이 JWT라고 명시

        // 2. Swagger UI에 표시될 정보 설정
        return new OpenAPI()
                .info(new Info()
                        .title("MyGomi API 문서")
                        .description("마이고미 프로젝트 백엔드 API 명세서입니다.")
                        .version("1.0.0"))
                .addSecurityItem(securityRequirement) // 전역 인증 적용
                .components(components);
    }
}