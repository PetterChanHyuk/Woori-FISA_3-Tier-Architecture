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
  '테이블 스키마 작성 부분'
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
