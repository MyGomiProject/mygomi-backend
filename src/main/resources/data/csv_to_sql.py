import csv
import os
import sys

# 1. 파일명 설정
file_areas = 'areas.csv'
file_rules = 'collection_rules.csv'
output_file = 'V3__insert_data.sql'

def escape_sql(val):
    """SQL에 넣을 수 있게 특수문자 처리 및 NULL 처리"""
    if val is None:
        return 'NULL'
    val = str(val).strip()
    # 빈 문자열, nan, NULL 문자열은 NULL로 처리
    if val == '' or val.lower() == 'nan' or val.lower() == 'null':
        return 'NULL'
    # 작은따옴표(')가 있으면 ('')로 이스케이프
    return f"'{val.replace("'", "''")}'"

def make_sql():
    # 파일 확인
    if not os.path.exists(file_areas) or not os.path.exists(file_rules):
        print(f"⚠️ 파일을 찾을 수 없습니다.")
        print(f"   - {file_areas}: {os.path.exists(file_areas)}")
        print(f"   - {file_rules}: {os.path.exists(file_rules)}")
        return

    print("🚀 변환을 시작합니다...")

    try:
        # 2. areas.csv 읽기
        insert_areas = []
        with open(file_areas, 'r', encoding='utf-8-sig') as f:
            reader = csv.DictReader(f)
            for row in reader:
                # areas 컬럼: id, region, prefecture, ward, town, chome, banchi_text
                val = f"({row['id']}, {escape_sql(row['region'])}, {escape_sql(row['prefecture'])}, {escape_sql(row['ward'])}, {escape_sql(row['town'])}, {escape_sql(row.get('chome'))}, {escape_sql(row.get('banchi_text'))})"
                insert_areas.append(val)

        # 3. collection_rules.csv 읽기
        insert_rules = []
        with open(file_rules, 'r', encoding='utf-8-sig') as f:
            reader = csv.DictReader(f)
            for row in reader:
                # rules 컬럼: id, area_id, waste_type, rule_type, weekdays, nth_weeks, note
                val = f"({row['id']}, {row['area_id']}, {escape_sql(row['waste_type'])}, {escape_sql(row['rule_type'])}, {escape_sql(row.get('weekdays'))}, {escape_sql(row.get('nth_weeks'))}, {escape_sql(row.get('note'))})"
                insert_rules.append(val)

        # 4. SQL 파일 쓰기
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write("-- V3__insert_data.sql\n")
            f.write("-- Generated from CSV (No Pandas Version)\n\n")

            # Areas
            if insert_areas:
                f.write(f"-- Inserting {len(insert_areas)} areas\n")
                f.write("INSERT INTO areas (id, region, prefecture, ward, town, chome, banchi_text) VALUES\n")
                f.write(",\n".join(insert_areas) + ";\n\n")

            # Rules
            if insert_rules:
                f.write(f"-- Inserting {len(insert_rules)} collection_rules\n")
                f.write("INSERT INTO collection_rules (id, area_id, waste_type, rule_type, weekdays, nth_weeks, note) VALUES\n")
                f.write(",\n".join(insert_rules) + ";\n")

        print(f"✅ 변환 완료! '{output_file}' 파일이 생성되었습니다.")
        print(f"   - Areas: {len(insert_areas)}개")
        print(f"   - Rules: {len(insert_rules)}개")

    except Exception as e:
        print(f"❌ 에러 발생: {e}")

if __name__ == '__main__':
    make_sql()