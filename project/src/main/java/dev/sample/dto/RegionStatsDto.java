package dev.sample.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 지역별 카드 이용금액 통계 DTO
 */
@Getter
@Builder
@ToString
public class RegionStatsDto {

    private String housSidoNm;    // 거주지역
    private long totUseAm;        // 평균 총이용금액
    private long crdslUseAm;      // 평균 신용카드이용금액
    private long cnfUseAm;        // 평균 체크카드이용금액
}
