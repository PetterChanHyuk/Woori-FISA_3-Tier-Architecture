package dev.sample.service;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import dev.sample.dao.StatsDao;
import dev.sample.dto.AgeStatsDto;
import dev.sample.dto.LifestageStatsDto;
import dev.sample.dto.RegionStatsDto;
import lombok.extern.slf4j.Slf4j;

/**
 * 통계 서비스
 * 유효성 검사 후 DAO 호출
 */
@Slf4j
public class StatsService {

    private final StatsDao statsDao;

    // 유효한 연령대 코드 (20~85, 5단위)
    private static final java.util.Set<String> VALID_AGE =
        new java.util.HashSet<>(java.util.Arrays.asList(
            "20","25","30","35","40","45","50","55","60","65","70","75","80","85"
        ));

    // 유효한 라이프스테이지 코드
    private static final java.util.Set<String> VALID_LIFESTAGE =
        new java.util.HashSet<>(java.util.Arrays.asList(
            "UNI","NEW_JOB","NEW_WED","CHILD_BABY",
            "CHILD_TEEN","CHILD_UNI","GOLLIFE","SECLIFE","RETIR"
        ));

    public StatsService(DataSource ds) {
        this.statsDao = new StatsDao(ds);
    }

    /**
     * 연령대별 업종 소비 통계 조회
     * @param age 연령대 코드 (예: "30")
     */
    public List<AgeStatsDto> getStatsByAge(String age) throws SQLException {
        if (age == null || age.isBlank()) {
            throw new IllegalArgumentException("age 파라미터는 필수입니다.");
        }
        if (!VALID_AGE.contains(age.trim())) {
            throw new IllegalArgumentException("유효하지 않은 age 값입니다: " + age);
        }
        log.info("getStatsByAge age={}", age);
        return statsDao.findStatsByAge(age.trim());
    }

    /**
     * 지역별 카드 이용금액 순위 조회
     */
    public List<RegionStatsDto> getStatsByRegion() throws SQLException {
        log.info("getStatsByRegion");
        return statsDao.findStatsByRegion();
    }

    /**
     * 라이프스테이지별 소비 패턴 조회
     * @param lifeStage 라이프스테이지 코드 (예: "NEW_WED")
     */
    public List<LifestageStatsDto> getStatsByLifestage(String lifeStage) throws SQLException {
        if (lifeStage == null || lifeStage.isBlank()) {
            throw new IllegalArgumentException("lifeStage 파라미터는 필수입니다.");
        }
        if (!VALID_LIFESTAGE.contains(lifeStage.trim())) {
            throw new IllegalArgumentException("유효하지 않은 lifeStage 값입니다: " + lifeStage);
        }
        log.info("getStatsByLifestage lifeStage={}", lifeStage);
        return statsDao.findStatsByLifestage(lifeStage.trim());
    }
}
