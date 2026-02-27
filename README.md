# 🏗️ Woori-FISA 3-Tier Architecture

> **Nginx 로드밸런서 + Tomcat 이중화 + MySQL Master/Replica 구조**를 갖춘 3-Tier 웹 아키텍처 프로젝트

카드 거래 데이터를 기반으로 **연령대별·라이프스테이지별·지역별 소비 통계**를 조회하고, **고객 회원등급을 변경**하는 RESTful API 서버입니다.

---

## 📐 시스템 아키텍처

```mermaid
flowchart TB
    subgraph CLIENT ["👤 Client Tier"]
        Browser["Browser / APIDog"]
    end

    subgraph WEB ["⚖️ Web Tier"]
        Nginx["Nginx<br/>Load Balancer<br/>:80"]
    end

    subgraph APP ["🍅 Application Tier"]
        T1["Tomcat #1 (:8080)<br/>Servlet → Service → DAO<br/>HikariCP Pool (10 conn)"]
        T2["Tomcat #2 (:8090)<br/>Servlet → Service → DAO<br/>HikariCP Pool (10 conn)"]
    end

    subgraph DATA ["🗄️ Data Tier (Docker)"]
        subgraph ROUTER ["� MySQL Router"]
            R1["Router #1<br/>:6446 (R/W) · :6447 (R/O)"]
            R2["Router #2<br/>:7446 (R/W) · :7447 (R/O)"]
        end
        subgraph CLUSTER ["�️ InnoDB Cluster"]
            M1["🔴 mysql1<br/>Primary (R/W)"]
            M2["🟢 mysql2<br/>Secondary (R/O)"]
            M3["🟢 mysql3<br/>Secondary (R/O)"]
        end
    end

    Browser -->|"HTTP Request"| Nginx
    Nginx -->|"ip_hash 분배"| T1
    Nginx -->|"ip_hash 분배"| T2
    T1 -->|"R/W"| R1
    T2 -->|"R/W"| R2
    R1 -->|"Write"| M1
    R1 -->|"Read"| M2
    R1 -->|"Read"| M3
    R2 -->|"Write"| M1
    R2 -->|"Read"| M2
    R2 -->|"Read"| M3
    M1 <-.- M2
    M1 <-.- M3
```

---

## 🔄 요청 흐름 (Request Flow)

사용자가 `http://localhost/project/api/stats/age?age=30` 요청을 보냈을 때의 전체 여정:

```mermaid
sequenceDiagram
    participant C as 👤 Client
    participant N as ⚖️ Nginx (:80)
    participant T as 🍅 Tomcat (:8080)
    participant S as 📦 Servlet
    participant SV as ⚙️ Service
    participant D as 🗃️ DAO
    participant H as 🏊 HikariCP
    participant DB as 🟢 Replica DB

    C->>N: GET /project/api/stats/age?age=30
    N->>T: ip_hash → 8080으로 분배
    T->>S: URL 매핑 → AgeStatsServlet.doGet()
    S->>S: getReplicaDataSource() (읽기 분기)
    S->>SV: StatsService.getStatsByAge("30")
    SV->>SV: 파라미터 유효성 검증
    SV->>D: StatsDao.findStatsByAge("30")
    D->>H: ds.getConnection() (풀에서 대여)
    H-->>D: Connection 반환
    D->>DB: PreparedStatement 실행 (Server-Side Prepared)
    DB-->>D: ResultSet
    D-->>SV: List<AgeStatsDto>
    SV-->>S: 결과 전달
    S-->>T: JSON 응답 생성
    T-->>N: HTTP Response
    N-->>C: {"status": "success", "data": [...]}
```

---

## 🧩 핵심 설계 포인트

### 1. Nginx 로드밸런싱 (Web Tier)

```nginx
upstream tomcat-servers {
    ip_hash;                    # 같은 IP → 항상 같은 서버 (Sticky Session)
    server 127.0.0.1:8080;
    server 127.0.0.1:8090;
}
```
- **`ip_hash`** 알고리즘으로 세션 유지 보장
- 톰캣 한 대가 장애 나도 나머지 한 대가 서비스 유지

### 2. DB 읽기/쓰기 분리 (Application Tier)

| API | HTTP Method | DataSource | DB 방향 |
|---|---|---|---|
| `/api/stats/age` | `GET` | `getReplicaDataSource()` | 🟢 Replica (읽기) |
| `/api/stats/lifestage` | `GET` | `getReplicaDataSource()` | 🟢 Replica (읽기) |
| `/api/stats/region` | `GET` | `getReplicaDataSource()` | 🟢 Replica (읽기) |
| `/api/customer/grade` | `PUT` | `getMasterDataSource()` | 🔴 Master (쓰기) |

### 3. HikariCP 커넥션 풀 & Server-Side Prepared Statement

```
jdbc:mysql://host:port/card_db
  ?useServerPrepStmts=true    ← SQL 틀을 DB에 미리 등록, ID로 재사용
  &cachePrepStmts=true        ← Prepare된 ID를 커넥션 내 캐시
  &prepStmtCacheSize=250      ← 최대 250개 SQL 틀 기억
```

### 4. InnoDB Cluster + MySQL Router (Data Tier)

```mermaid
flowchart TB
    subgraph ROUTER ["� MySQL Router (이중화)"]
        R1["Router #1\n:6446 (R/W) · :6447 (R/O)"]
        R2["Router #2\n:7446 (R/W) · :7447 (R/O)"]
    end

    subgraph CLUSTER ["🛡️ InnoDB Cluster (Group Replication)"]
        M1["🔴 mysql1\nPrimary (R/W)\nserver-id=100"]
        M2["🟢 mysql2\nSecondary (R/O)\nserver-id=101"]
        M3["🟢 mysql3\nSecondary (R/O)\nserver-id=102"]
        M1 <-.-|"Group\nReplication"| M2
        M1 <-.-|"Group\nReplication"| M3
    end

    R1 -->|"Write (:6446)"| M1
    R1 -->|"Read (:6447)"| M2
    R1 -->|"Read (:6447)"| M3
    R2 -->|"Write (:7446)"| M1
    R2 -->|"Read (:7447)"| M2
    R2 -->|"Read (:7447)"| M3
```

| 구성 요소 | 설명 |
|---|---|
| **InnoDB Cluster** | 3개 MySQL 노드가 Group Replication으로 자동 동기화 |
| **MySQL Router** | 애플리케이션 → 클러스터 간 **자동 라우팅** (R/W 분리) |
| **Automatic Failover** | Primary 장애 시 Secondary가 자동 승격 (수동 개입 불필요) |
| **GTID 기반 복제** | Global Transaction ID로 복제 위치를 정확히 추적 |

- **Router 포트 규칙**: `:6446` → Primary(쓰기), `:6447` → Secondary(읽기)
- **Router 이중화**: Router 한 대가 장애 나도 나머지 Router가 요청을 라우팅

---

## 📁 프로젝트 구조

```
Woori-FISA_3-Tier-Architecture/
├── nginx-config/
│   └── nginx.conf                    # Nginx 로드밸런서 설정
├── docker/
│   └── DB                            
│       └──docker-compose.yml         # InnoDB Cluster + MySQL Router 컨테이너 정의
│   └── WAS                           
│       └──docker-compose.yml         # Tomcat 컨테이너 정의
├── project/src/main/java/dev/sample/
│   ├── ApplicationContextListener.java   # HikariCP 풀 2개 초기화
│   ├── controller/
│   │   ├── customer/
│   │   │   └── CustomerGradeServlet.java # PUT - 고객등급 변경 (Master)
│   │   └── stats/
│   │       ├── AgeStatsServlet.java      # GET - 연령대별 통계 (Replica)
│   │       ├── LifestageStatsServlet.java# GET - 라이프스테이지별 (Replica)
│   │       └── RegionStatsServlet.java   # GET - 지역별 통계 (Replica)
│   ├── service/
│   │   ├── CustomerService.java          # 고객 비즈니스 로직
│   │   └── StatsService.java             # 통계 비즈니스 로직 + 유효성 검증
│   ├── dao/
│   │   ├── CustomerDao.java              # 고객 DB 접근 (PreparedStatement)
│   │   └── StatsDao.java                 # 통계 DB 접근 (PreparedStatement)
│   ├── dto/                              # 데이터 전송 객체 (Lombok @Builder)
│   └── util/
│       └── JsonResponseUtil.java         # JSON 응답 유틸리티
└── libraries/                            # JAR 라이브러리 (HikariCP, MySQL 등)
```

---

## 🚀 실행 방법

### 1단계: DB 환경 구축 (Docker + InnoDB Cluster)
/docker/DB에서 docker-compose.yml을 실행한다.

```bash
# DB 컨테이너 실행
docker-compose up
```
PRIMARY가 될 MySQL 컨테이너의 MySQL Shell로 접속한다.
```bash
# MySQL Shell 접속
docker exec -it mysql1 mysqlsh root@mysql1:8081
```
인스턴스를 설정하고 클러스터링을 수행한다.
```javascript
// MySQL Shell

// 1) 인스턴스 설정
dba.configureInstance('root@mysql1:8081')
dba.configureInstance('root@mysql2:8082')
dba.configureInstance('root@mysql3:8083')

// 2) 재부팅
\c root@mysql1:8081

// 3) 클러스터 생성
var cluster = dba.createCluster('sqlCluster', {localAddress: 'mysql1:8081'});

// 4) 노드 추가
cluster.addInstance('root@host.docker.internal:8082', {localAddress: 'mysql2:8082'});
cluster.addInstance('root@host.docker.internal:8083', {localAddress: 'mysql3:8083'});

// 5) 클러스터 상태 확인
cluster.status();
```

### 2단계: Tomcat 서버 및 MySQL Router 실행 (IDE)

이클립스에서 프로젝트를 빌드한 `.war` 파일을 /docker/WAS/에 넣는다.
`ApplicationContextListener.java`에서 아래 부분의 router의 순서를 변경하여 빌드를 수행해야 한다.
각 파일의 이름은 `sample-project1.war`와 `sample-project2.war`로 변경한다.
```java
masterConfig.setJdbcUrl("jdbc:mysql://router1:6447,router2:6447...");
```
/docker/WAS에서 docker-compose.yml을 실행한다.
```bash
# WAS 컨테이너 실행
docker-compose up
```


### 3단계: Nginx 로드밸런서 실행

```bash
nginx -p "<Nginx 설치 경로>\" -c "<프로젝트 경로>\nginx-config\nginx.conf"
```
### 4단계: API 테스트

```bash
# 통계 조회 (Nginx 경유 → Replica DB)
GET http://localhost/project/api/stats/age?age=30
GET http://localhost/project/api/stats/lifestage?lifeStage=NEW_WED
GET http://localhost/project/api/stats/region

# 고객 등급 변경 (Nginx 경유 → Master DB)
PUT http://localhost/project/api/customer/grade
Body (x-www-form-urlencoded): seq=1001, mbrRk=22
```

---

## 🛠️ 기술 스택

| 계층 | 기술 | 역할 |
|---|---|---|
| Web Tier | **Nginx 1.28** | 로드밸런싱, 리버스 프록시 |
| App Tier | **Apache Tomcat 9.0** | 서블릿 컨테이너 (×2대) |
| App Tier | **Java 17 + Servlet API** | RESTful API 비즈니스 로직 |
| App Tier | **HikariCP** | JDBC 커넥션 풀 관리 |
| App Tier | **Lombok** | 보일러플레이트 코드 제거 |
| App Tier | **Logback (SLF4J)** | 애플리케이션 로깅 |
| Data Tier | **MySQL 8.0 (Docker)** | RDBMS (Master + Replica) |

---

## 👥 팀원

| 이름 | 역할 |
|---|---|
| **팀원 A** | DB 이중화 설정 (`docker-compose.yml`, `setup.sh`) |
| **팀원 B** | Nginx 로드밸런서 설정 (`nginx.conf`) |
| **팀원 C** | 애플리케이션 코드 (Servlet, Service, DAO, HikariCP 이중화) |