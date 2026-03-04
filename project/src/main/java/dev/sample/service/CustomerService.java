package dev.sample.service;

import java.sql.SQLException;

import org.springframework.stereotype.Service;

import dev.sample.dao.CustomerDao;
import dev.sample.dto.CustomerDto;
import dev.sample.dto.CustomerGradeDto;
import lombok.extern.slf4j.Slf4j;

/**
 * 고객 서비스
 * - 유효성 검사 후 DAO 호출
 * - 스프링 컨테이너가 싱글톤 빈으로 관리
 */
@Slf4j
@Service
public class CustomerService {

    private final CustomerDao customerDao;

    // 유효한 회원등급 코드
    private static final java.util.Set<String> VALID_MBR_RK = new java.util.HashSet<>(java.util.Arrays.asList(
            "21", "22", "23", "24", "25"));

    // ★ 핵심 변경: new CustomerDao(ds) 삭제!
    // 스프링이 이미 만들어 둔 CustomerDao 빈을 생성자 주입(DI)으로 받습니다.
    // 생성자가 1개이므로 @Autowired 생략 가능 (Spring 4.3+)
    public CustomerService(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    /**
     * 고객 회원등급 변경
     * 
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

    /**
     * 고객 로그인
     * 
     * @param id       아이디
     * @param password 비밀번호
     * @return 성공 시 CustomerDto, 실패 시 null
     */
    public CustomerDto login(String id, String password) throws SQLException {
        if (id == null || id.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("아이디와 비밀번호를 모두 입력해주세요.");
        }

        CustomerDto customer = customerDao.findCustomerByIdAndPassword(id, password);
        if (customer != null) {
            log.info("로그인 성공: {}", id);
        } else {
            log.warn("로그인 실패: {}", id);
        }
        return customer;
    }
}
