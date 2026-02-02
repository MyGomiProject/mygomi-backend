import pandas as pd
import os

# 1. 파일명 설정
file_areas = 'areas.csv'
file_rules = 'collection_rules.csv'
output_file = 'V2__insert_data.sql'

def escape_sql(val):
    """SQL에 넣을 수 있게 특수문자 처리 및 NULL 처리"""
    if pd.isna(val) or val == '' or str(val).lower() == 'nan':
        return 'NULL'
    # 작은따옴표(')가 있으면 ('')로 이스케이프
    return f"'{str(val).replace("'", "''")}'"

def make_sql():
    # 파일이 있는지 확인
    if not os.path.exists(file_areas) or not os.path.exists(file_rules):
        print("⚠️ 'areas.csv' 또는 'collection_rules.csv' 파일이 없습니다.")
        return

    print("🚀 변환을 시작합니다...")

    # 2. areas.csv 읽기
    df_areas = pd.read_csv(file_areas)
    insert_areas = []
    for _, row in df_areas.iterrows():
        # areas 테이블 컬럼 순서: id, region, prefecture, ward, town, chome, banchi_text
        val = f"({row['id']}, {escape_sql(row['region'])}, {escape_sql(row['prefecture'])}, {escape_sql(row['ward'])}, {escape_sql(row['town'])}, {escape_sql(row['chome'])}, {escape_sql(row['banchi_text'])})"
        insert_areas.append(val)

    # 3. collection_rules.csv 읽기
    df_rules = pd.read_csv(file_rules)
    insert_rules = []
    for _, row in df_rules.iterrows():
        # collection_rules 테이블 컬럼 순서: id, area_id, waste_type, rule_type, weekdays, nth_weeks, note
        val = f"({row['id']}, {row['area_id']}, {escape_sql(row['waste_type'])}, {escape_sql(row['rule_type'])}, {escape_sql(row['weekdays'])}, {escape_sql(row['nth_weeks'])}, {escape_sql(row['note'])})"
        insert_rules.append(val)

    # 4. SQL 파일 쓰기
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("-- V2__insert_data.sql\n")
        f.write("-- Created from areas.csv and collection_rules.csv\n\n")

        # Areas INSERT
        f.write(f"-- Inserting {len(insert_areas)} areas\n")
        f.write("INSERT INTO areas (id, region, prefecture, ward, town, chome, banchi_text) VALUES\n")
        f.write(",\n".join(insert_areas) + ";\n\n")

        # Rules INSERT
        f.write(f"-- Inserting {len(insert_rules)} collection_rules\n")
        f.write("INSERT INTO collection_rules (id, area_id, waste_type, rule_type, weekdays, nth_weeks, note) VALUES\n")
        f.write(",\n".join(insert_rules) + ";\n")

    print(f"✅ 변환 완료! '{output_file}' 파일이 생성되었습니다.")
    print(f"   - Areas: {len(insert_areas)}개")
    print(f"   - Rules: {len(insert_rules)}개")

if __name__ == '__main__':
    make_sql()