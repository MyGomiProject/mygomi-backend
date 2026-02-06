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

    @Value("${google.maps.api-key}")
    private String apiKey;

    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    public GeoCoordinate getCoordinate(String fullAddress) {
        try {
            if (fullAddress == null || fullAddress.isBlank()) {
                return new GeoCoordinate(0.0, 0.0);
            }

            // 📍 정확도 UP! URL 생성 로직
            URI uri = UriComponentsBuilder.fromHttpUrl(GOOGLE_API_URL)
                    .queryParam("address", fullAddress)
                    .queryParam("key", apiKey)
                    .queryParam("language", "ko") // 결과 언어 설정
                    .queryParam("region", "jp")   // ✅ 핵심: 일본 지역 바이어싱 (정확도 상승)
                    .build()
                    .toUri();

            log.info("Google Maps 요청: {}", fullAddress);

            String response = restTemplate.getForObject(uri, String.class);
            JsonNode root = objectMapper.readTree(response);

            String status = root.path("status").asText();
            if (!"OK".equals(status)) {
                String errorMessage = root.path("error_message").asText();
                log.error("Geocoding 실패 - 상태: {}, 메시지: {}, 주소: {}", status, errorMessage, fullAddress);
                return new GeoCoordinate(0.0, 0.0);
            }

            // 결과 파싱
            JsonNode result = root.path("results").get(0);

            // 💡 (선택 사항) 정확도 로그 찍어보기
            String locationType = result.path("geometry").path("location_type").asText();
            log.info("검색 정확도 타입: {}", locationType);
            // ROOFTOP: 정확한 건물 / RANGE_INTERPOLATED: 주소 범위 사이 / GEOMETRIC_CENTER: 동네 중심

            JsonNode location = result.path("geometry").path("location");
            double lat = location.path("lat").asDouble();
            double lng = location.path("lng").asDouble();

            log.info("좌표 변환 성공: {} -> ({}, {})", fullAddress, lat, lng);
            return new GeoCoordinate(lat, lng);

        } catch (Exception e) {
            log.error("Google Maps API 연동 중 에러 발생", e);
            return new GeoCoordinate(0.0, 0.0);
        }
    }

    public record GeoCoordinate(Double lat, Double lng) {}
}