package dev.sample.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import dev.sample.dto.AgeStatsDto;
import dev.sample.dto.LifestageStatsDto;
import dev.sample.dto.RegionStatsDto;
import lombok.extern.slf4j.Slf4j;

/**
 * 통계 조회 DAO
 * - 읽기(Replica) DataSource를 사용
 * - 스프링 컨테이너가 싱글톤 빈으로 관리
 * - 모든 금액은 천원 단위 월평균값
 */
@Slf4j
@Repository
public class StatsDao {

    private final DataSource ds;

    // 생성자가 1개이므로 @Autowired 생략 가능 (Spring 4.3+)
    // @Qualifier로 AppConfig의 replicaDataSource 빈을 지정
    public StatsDao(@Qualifier("replicaDataSource") DataSource ds) {
        this.ds = ds;
    }

    /**
     * 연령대별 업종 소비 통계 조회
     * 
     * @param age 연령대 코드 (예: "30" → 30~34세)
     */
    public List<AgeStatsDto> findStatsByAge(String age) throws SQLException {

        String sql = "SELECT AGE, " +
                "  ROUND(AVG(FSBZ_AM))     AS fsbzAm, " +
                "  ROUND(AVG(TRVLEC_AM))   AS trvlecAm, " +
                "  ROUND(AVG(DIST_AM))     AS distAm, " +
                "  ROUND(AVG(INSUHOS_AM))  AS insuHosAm, " +
                "  ROUND(AVG(CLOTHGDS_AM)) AS clothGdsAm, " +
                "  ROUND(AVG(AUTO_AM))     AS autoAm, " +
                "  ROUND(AVG(INTERIOR_AM)) AS interiorAm, " +
                "  ROUND(AVG(OFFEDU_AM))   AS offEduAm, " +
                "  ROUND(AVG(PLSANIT_AM))  AS plSanitAm " +
                "FROM CARD_TRANSACTION " +
                "WHERE AGE = ? " +
                "GROUP BY AGE";

        List<AgeStatsDto> result = new ArrayList<>();

        try (Connection con = ds.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, age);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(AgeStatsDto.builder()
                            .age(rs.getString("AGE"))
                            .fsbzAm(rs.getLong("fsbzAm"))
                            .trvlecAm(rs.getLong("trvlecAm"))
                            .distAm(rs.getLong("distAm"))
                            .insuHosAm(rs.getLong("insuHosAm"))
                            .clothGdsAm(rs.getLong("clothGdsAm"))
                            .autoAm(rs.getLong("autoAm"))
                            .interiorAm(rs.getLong("interiorAm"))
                            .offEduAm(rs.getLong("offEduAm"))
                            .plSanitAm(rs.getLong("plSanitAm"))
                            .build());
                }
            }
        }

        log.info("findStatsByAge age={}, resultSize={}", age, result.size());
        return result;
    }

    /**
     * 지역별 카드 이용금액 순위 조회
     * 총이용금액 평균 기준 내림차순 정렬
     */
    public List<RegionStatsDto> findStatsByRegion() throws SQLException {

        String sql = "SELECT HOUS_SIDO_NM, " +
                "  ROUND(AVG(TOT_USE_AM))   AS totUseAm, " +
                "  ROUND(AVG(CRDSL_USE_AM)) AS crdslUseAm, " +
                "  ROUND(AVG(CNF_USE_AM))   AS cnfUseAm " +
                "FROM CARD_TRANSACTION " +
                "WHERE HOUS_SIDO_NM IS NOT NULL " +
                "GROUP BY HOUS_SIDO_NM " +
                "ORDER BY totUseAm DESC";

        List<RegionStatsDto> result = new ArrayList<>();

        try (Connection con = ds.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(RegionStatsDto.builder()
                        .housSidoNm(rs.getString("HOUS_SIDO_NM"))
                        .totUseAm(rs.getLong("totUseAm"))
                        .crdslUseAm(rs.getLong("crdslUseAm"))
                        .cnfUseAm(rs.getLong("cnfUseAm"))
                        .build());
            }
        }

        log.info("findStatsByRegion resultSize={}", result.size());
        return result;
    }

    /**
     * 라이프스테이지별 소비 패턴 조회
     * 
     * @param lifeStage 라이프스테이지 코드 (예: "NEW_WED", "UNI" 등)
     */
    public List<LifestageStatsDto> findStatsByLifestage(String lifeStage) throws SQLException {

        String sql = "SELECT LIFE_STAGE, " +
                "  ROUND(AVG(FSBZ_AM))     AS fsbzAm, " +
                "  ROUND(AVG(TRVLEC_AM))   AS trvlecAm, " +
                "  ROUND(AVG(DIST_AM))     AS distAm, " +
                "  ROUND(AVG(INSUHOS_AM))  AS insuHosAm, " +
                "  ROUND(AVG(CLOTHGDS_AM)) AS clothGdsAm, " +
                "  ROUND(AVG(AUTO_AM))     AS autoAm, " +
                "  ROUND(AVG(INTERIOR_AM)) AS interiorAm, " +
                "  ROUND(AVG(OFFEDU_AM))   AS offEduAm, " +
                "  ROUND(AVG(PLSANIT_AM))  AS plSanitAm " +
                "FROM CARD_TRANSACTION " +
                "WHERE LIFE_STAGE = ? " +
                "GROUP BY LIFE_STAGE";

        List<LifestageStatsDto> result = new ArrayList<>();

        try (Connection con = ds.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, lifeStage);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(LifestageStatsDto.builder()
                            .lifeStage(rs.getString("LIFE_STAGE"))
                            .fsbzAm(rs.getLong("fsbzAm"))
                            .trvlecAm(rs.getLong("trvlecAm"))
                            .distAm(rs.getLong("distAm"))
                            .insuHosAm(rs.getLong("insuHosAm"))
                            .clothGdsAm(rs.getLong("clothGdsAm"))
                            .autoAm(rs.getLong("autoAm"))
                            .interiorAm(rs.getLong("interiorAm"))
                            .offEduAm(rs.getLong("offEduAm"))
                            .plSanitAm(rs.getLong("plSanitAm"))
                            .build());
                }
            }
        }

        log.info("findStatsByLifestage lifeStage={}, resultSize={}", lifeStage, result.size());
        return result;
    }
}
