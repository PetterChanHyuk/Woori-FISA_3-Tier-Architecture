package dev.sample.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Spring IoC 컨테이너 설정 클래스
 *
 * 역할:
 * 1. @ComponentScan으로 dev.sample 패키지 하위의 @Service, @Repository 빈을 자동 스캔
 * 2. 외부 라이브러리(HikariCP)로 만든 DataSource를 @Bean으로 수동 등록
 */
@Configuration
@ComponentScan(basePackages = "dev.sample")
public class AppConfig {

    /**
     * 쓰기용 (Master) DataSource - MySQL Router 6447 포트
     * 고객 등급 변경(UPDATE) 등 쓰기 작업에 사용
     */
    @Bean(name = "masterDataSource", destroyMethod = "close")
    public DataSource masterDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(
                "jdbc:mysql://router1:6447,router2:6447/card_db?serverTimezone=Asia/Seoul&useServerPrepStmts=true&cachePrepStmts=true&prepStmtCacheSize=250");
        config.setUsername("root");
        config.setPassword("1234");
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }

    /**
     * 읽기용 (Replica) DataSource - MySQL Router 6446 포트
     * 통계 조회(SELECT) 등 읽기 작업에 사용
     */
    @Bean(name = "replicaDataSource", destroyMethod = "close")
    public DataSource replicaDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(
                "jdbc:mysql://router1:6446,router2:6446/card_db?serverTimezone=Asia/Seoul&useServerPrepStmts=true&cachePrepStmts=true&prepStmtCacheSize=250");
        config.setUsername("root");
        config.setPassword("1234");
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }
}
