package com.mygomi.backend.config;

import com.mygomi.backend.repository.AreaRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final AreaRepository areaRepository;
    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    @Transactional
    public void initData() {
        // 1. 이미 데이터가 있으면 건너뛰기 (중복 입력 방지)
        if (areaRepository.count() > 0) {
            log.info("ℹ️ DB에 이미 데이터가 있습니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("🚀 CSV 데이터 로딩을 시작합니다...");

        // 2. Areas 데이터 로딩
        loadAreas();

        // 3. Collection Rules 데이터 로딩
        loadCollectionRules();

        log.info("✅ 모든 데이터 로딩이 완료되었습니다!");
    }

    private void loadAreas() {
        try (CSVReader reader = new CSVReader(new InputStreamReader(new ClassPathResource("data/areas.csv").getInputStream(), StandardCharsets.UTF_8))) {
            List<String[]> rows = reader.readAll();
            rows.remove(0); // 헤더(첫 줄) 제거

            String sql = "INSERT INTO areas (id, region, prefecture, ward, town, chome, banchi_text, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

            // JDBC Batch Update로 고속 저장
            jdbcTemplate.batchUpdate(sql, rows, 1000, (ps, row) -> {
                ps.setLong(1, Long.parseLong(row[0])); // id
                ps.setString(2, row[1]); // region
                ps.setString(3, row[2]); // prefecture
                ps.setString(4, row[3]); // ward
                ps.setString(5, row[4]); // town
                // chome이 비어있으면 null 처리
                ps.setString(6, (row[5] == null || row[5].isEmpty()) ? null : row[5]);
                ps.setString(7, (row[6] == null || row[6].isEmpty()) ? null : row[6]); // banchi_text
            });

            // ID 시퀀스 값 맞추기 (중요: 이걸 안 하면 나중에 새 데이터 넣을 때 에러남)
            jdbcTemplate.execute("SELECT setval('areas_id_seq', (SELECT MAX(id) FROM areas))");

            log.info("✅ Areas 데이터 {}건 로딩 완료", rows.size());

        } catch (IOException | CsvException e) {
            log.error("❌ Areas CSV 로딩 실패: ", e);
            throw new RuntimeException("초기 데이터 로딩 실패");
        }
    }

    private void loadCollectionRules() {
        try (CSVReader reader = new CSVReader(new InputStreamReader(new ClassPathResource("data/collection_rules.csv").getInputStream(), StandardCharsets.UTF_8))) {
            List<String[]> rows = reader.readAll();
            rows.remove(0); // 헤더 제거

            String sql = "INSERT INTO collection_rules (id, area_id, waste_type, rule_type, weekdays, nth_weeks, note, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

            jdbcTemplate.batchUpdate(sql, rows, 1000, (ps, row) -> {
                ps.setLong(1, Long.parseLong(row[0])); // id
                ps.setLong(2, Long.parseLong(row[1])); // area_id
                ps.setString(3, row[2]); // waste_type
                ps.setString(4, row[3]); // rule_type
                ps.setString(5, (row[4] == null || row[4].isEmpty()) ? null : row[4]); // weekdays
                ps.setString(6, (row[5] == null || row[5].isEmpty()) ? null : row[5]); // nth_weeks
                ps.setString(7, (row[6] == null || row[6].isEmpty()) ? null : row[6]); // note
            });

            jdbcTemplate.execute("SELECT setval('collection_rules_id_seq', (SELECT MAX(id) FROM collection_rules))");

            log.info("✅ Collection Rules 데이터 {}건 로딩 완료", rows.size());

        } catch (IOException | CsvException e) {
            log.error("❌ Collection Rules CSV 로딩 실패: ", e);
            // Rules 로딩 실패는 일단 로그만 찍고 넘어감 (필수 데이터는 Areas니까)
        }
    }
}