package dev.sample.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 고객 회원등급 변경 DTO
 * MBR_RK 코드:
 *   21 = VVIP
 *   22 = VIP
 *   23 = 플래티넘
 *   24 = 골드
 *   25 = 해당없음
 */
@Getter
@Builder
@ToString
public class CustomerGradeDto {

    private String seq;           // 고객번호
    private String mbrRk;         // 변경할 회원등급 코드
}
