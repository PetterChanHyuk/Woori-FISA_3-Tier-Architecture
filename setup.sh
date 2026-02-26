#!/bin/bash

echo "🚀 1. 컨테이너 및 네트워크 생성 시작..."
docker-compose up -d

echo "⏳ 2. MySQL 서버가 완전히 켜질 때까지 대기 (약 15~20초)..."
sleep 20

echo "🔑 3. 소스 서버: 복제용 계정 생성"
docker exec card_data_source mysql -uroot -p1234 -e "
CREATE USER IF NOT EXISTS 'repl_user'@'%' IDENTIFIED BY '1234';
GRANT REPLICATION SLAVE ON *.* TO 'repl_user'@'%';
FLUSH PRIVILEGES;
"

echo "🔗 4. 복제 서버: 복제 연결 설정 및 시작"
docker exec card_data_replica mysql -uroot -p1234 -e "
STOP REPLICA;
RESET REPLICA ALL;
CHANGE REPLICATION SOURCE TO
  SOURCE_HOST='card_data_source',
  SOURCE_USER='repl_user',
  SOURCE_PASSWORD='1234',
  SOURCE_AUTO_POSITION=1;
START REPLICA;
"

echo "💾 5. 소스 서버: DB/테이블 생성 및 데이터 로드 (덤프 필요 없음!)"
# 주의: 아래 CREATE TABLE 문은 실제 데이터 구조에 맞게 수정하셔야 합니다!
docker exec card_data_source mysql -uroot -p1234 -e "
CREATE DATABASE IF NOT EXISTS card_db;
USE card_db;

-- 기존 테이블이 있다면 삭제하고 새로 생성 (초기화)
DROP TABLE IF EXISTS CARD_TRANSACTION;
CREATE TABLE IF NOT EXISTS CARD_TRANSACTION (
  BAS_YH CHAR(6) COMMENT '기준시점(분기)',
  SEQ VARCHAR(20) COMMENT '고객번호',
  AGE CHAR(2) COMMENT '연령대',
  SEX_CD CHAR(2) COMMENT '성별',
  MBR_RK CHAR(2) COMMENT '회원등급',
  ATT_YM CHAR(6) COMMENT '입회년월',
  HOUS_SIDO_NM VARCHAR(40) COMMENT '거주지역_1',
  DIGT_CHNL_REG_YN CHAR(1) COMMENT '디지털채널가입여부',
  DIGT_CHNL_USE_YN CHAR(1) COMMENT '디지털채널이용여부(당월)',
  LIFE_STAGE VARCHAR(40) COMMENT '라이프스테이지',
  TOT_USE_AM DECIMAL(18,0) COMMENT '총이용금액',
  CRDSL_USE_AM DECIMAL(18,0) COMMENT '신용카드이용금액',
  CNF_USE_AM DECIMAL(18,0) COMMENT '체크카드이용금액',
  INTERIOR_AM DECIMAL(18,0) COMMENT '가전/가구/주방용품',
  INSUHOS_AM DECIMAL(18,0) COMMENT '보험/병원',
  OFFEDU_AM DECIMAL(18,0) COMMENT '사무통신/서적/학원',
  TRVLEC_AM DECIMAL(18,0) COMMENT '여행/레져/문화',
  FSBZ_AM DECIMAL(18,0) COMMENT '요식업',
  SVCARC_AM DECIMAL(18,0) COMMENT '용역/수리/건축자재',
  DIST_AM DECIMAL(18,0) COMMENT '유통',
  PLSANIT_AM DECIMAL(18,0) COMMENT '보건위생',
  CLOTHGDS_AM DECIMAL(18,0) COMMENT '의류/신변잡화',
  AUTO_AM DECIMAL(18,0) COMMENT '자동차/연료/정비',
  FUNITR_AM DECIMAL(18,0) COMMENT '가구',
  APPLNC_AM DECIMAL(18,0) COMMENT '가전제품',
  HLTHFS_AM DECIMAL(18,0) COMMENT '건강식품',
  BLDMNG_AM DECIMAL(18,0) COMMENT '건물및시설관리',
  ARCHIT_AM DECIMAL(18,0) COMMENT '건축/자재',
  OPTIC_AM DECIMAL(18,0) COMMENT '광학제품',
  AGRICTR_AM DECIMAL(18,0) COMMENT '농업',
  LEISURE_S_AM DECIMAL(18,0) COMMENT '레져업소',
  LEISURE_P_AM DECIMAL(18,0) COMMENT '레져용품',
  CULTURE_AM DECIMAL(18,0) COMMENT '문화/취미',
  SANIT_AM DECIMAL(18,0) COMMENT '보건/위생',
  INSU_AM DECIMAL(18,0) COMMENT '보험',
  OFFCOM_AM DECIMAL(18,0) COMMENT '사무/통신기기',
  BOOK_AM DECIMAL(18,0) COMMENT '서적/문구',
  RPR_AM DECIMAL(18,0) COMMENT '수리서비스',
  HOTEL_AM DECIMAL(18,0) COMMENT '숙박업',
  GOODS_AM DECIMAL(18,0) COMMENT '신변잡화',
  TRVL_AM DECIMAL(18,0) COMMENT '여행업',
  FUEL_AM DECIMAL(18,0) COMMENT '연료판매',
  SVC_AM DECIMAL(18,0) COMMENT '용역서비스',
  DISTBNP_AM DECIMAL(18,0) COMMENT '유통업비영리',
  DISTBP_AM DECIMAL(18,0) COMMENT '유통업영리',
  GROCERY_AM DECIMAL(18,0) COMMENT '음식료품',
  HOS_AM DECIMAL(18,0) COMMENT '의료기관',
  CLOTH_AM DECIMAL(18,0) COMMENT '의류',
  RESTRNT_AM DECIMAL(18,0) COMMENT '일반/휴게음식',
  AUTOMNT_AM DECIMAL(18,0) COMMENT '자동차정비/유지',
  AUTOSL_AM DECIMAL(18,0) COMMENT '자동차판매',
  KITWR_AM DECIMAL(18,0) COMMENT '주방용품',
  FABRIC_AM DECIMAL(18,0) COMMENT '직물',
  ACDM_AM DECIMAL(18,0) COMMENT '학원',
  MBRSHOP_AM DECIMAL(18,0) COMMENT '회원제형태업소'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

LOAD DATA INFILE '/var/lib/mysql-files/EDU_DATA_F.dat'
INTO TABLE CARD_TRANSACTION
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\r\n'
IGNORE 1 LINES;
"

echo "✅ 모든 세팅 완료! 복제 상태를 확인합니다."
docker exec card_data_replica mysql -uroot -p1234 -e "SHOW REPLICA STATUS\G" | grep "Running:"