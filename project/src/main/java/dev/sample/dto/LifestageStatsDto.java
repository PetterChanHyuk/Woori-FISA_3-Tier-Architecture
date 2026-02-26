package dev.sample.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 라이프스테이지별 소비 패턴 DTO
 */
@Getter
@Builder
@ToString
public class LifestageStatsDto {

    private String lifeStage;     // 라이프스테이지

    // 대분류 업종별 평균 이용금액
    private long fsbzAm;          // 요식업
    private long trvlecAm;        // 여행/레져/문화
    private long distAm;          // 유통
    private long insuHosAm;       // 보험/병원
    private long clothGdsAm;      // 의류/신변잡화
    private long autoAm;          // 자동차/연료/정비
    private long interiorAm;      // 가전/가구/주방용품
    private long offEduAm;        // 사무통신/서적/학원
    private long plSanitAm;       // 보건위생
}
