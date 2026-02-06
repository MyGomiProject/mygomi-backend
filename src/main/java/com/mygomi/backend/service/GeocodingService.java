package com.mygomi.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // 환경 변수에서 가져온 키가 여기에 주입됩니다.
    @Value("${google.maps.api-key}")
    private String apiKey;

    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    public GeoCoordinate getCoordinate(String fullAddress) {
        try {
            if (fullAddress == null || fullAddress.isBlank()) {
                return new GeoCoordinate(0.0, 0.0);
            }

            // 1. URL 생성 (한글 주소 인코딩 자동 처리)
            URI uri = UriComponentsBuilder.fromHttpUrl(GOOGLE_API_URL)
                    .queryParam("address", fullAddress)
                    .queryParam("key", apiKey)
                    .queryParam("language", "ko") // 응답 언어 (선택)
                    .build()
                    .toUri();

            log.info("Google Maps 요청: {}", fullAddress);

            // 2. API 호출
            String response = restTemplate.getForObject(uri, String.class);
            JsonNode root = objectMapper.readTree(response);

            // 3. 응답 상태 확인 ('OK'가 아니면 실패로 간주)
            String status = root.path("status").asText();
            if (!"OK".equals(status)) {
                String errorMessage = root.path("error_message").asText();
                log.error("Geocoding 실패 - 상태: {}, 메시지: {}, 주소: {}", status, errorMessage, fullAddress);
                return new GeoCoordinate(0.0, 0.0);
            }

            // 4. 좌표 추출 (첫 번째 결과 사용)
            JsonNode location = root.path("results").get(0)
                    .path("geometry").path("location");

            double lat = location.path("lat").asDouble();
            double lng = location.path("lng").asDouble();

            log.info("좌표 변환 성공: {} -> ({}, {})", fullAddress, lat, lng);
            return new GeoCoordinate(lat, lng);

        } catch (Exception e) {
            log.error("Google Maps API 연동 중 에러 발생", e);
            return new GeoCoordinate(0.0, 0.0);
        }
    }

    // 좌표 데이터를 담는 불변 객체 (DTO)
    public record GeoCoordinate(Double lat, Double lng) {}
}