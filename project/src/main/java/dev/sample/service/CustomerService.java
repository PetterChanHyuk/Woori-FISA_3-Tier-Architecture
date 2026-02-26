package dev.sample.service;

import java.sql.SQLException;

import javax.sql.DataSource;

import dev.sample.dao.CustomerDao;
import dev.sample.dto.CustomerGradeDto;
import lombok.extern.slf4j.Slf4j;

/**
 * 고객 서비스
 * 유효성 검사 후 DAO 호출
 */
@Slf4j
public class CustomerService {

    private final CustomerDao customerDao;

    // 유효한 회원등급 코드
    private static final java.util.Set<String> VALID_MBR_RK =
        new java.util.HashSet<>(java.util.Arrays.asList(
            "21", "22", "23", "24", "25"
        ));

    public CustomerService(DataSource ds) {
        this.customerDao = new CustomerDao(ds);
    }

    /**
     * 고객 회원등급 변경
     * @param dto seq(고객번호), mbrRk(변경할 등급코드)
     * @return 업데이트된 행 수
     */
    public int updateCustomerGrade(CustomerGradeDto dto) throws SQLException {
        if (dto.getSeq() == null || dto.getSeq().isBlank()) {
            throw new IllegalArgumentException("seq(고객번호) 파라미터는 필수입니다.");
        }
        if (dto.getMbrRk() == null || dto.getMbrRk().isBlank()) {
            throw new IllegalArgumentException("mbrRk(회원등급) 파라미터는 필수입니다.");
        }
        if (!VALID_MBR_RK.contains(dto.getMbrRk().trim())) {
            throw new IllegalArgumentException("유효하지 않은 mbrRk 값입니다: " + dto.getMbrRk()
                + " (21:VVIP, 22:VIP, 23:플래티넘, 24:골드, 25:해당없음)");
        }
        log.info("updateCustomerGrade seq={}, mbrRk={}", dto.getSeq(), dto.getMbrRk());
        return customerDao.updateCustomerGrade(dto);
    }
}
