package dev.sample.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import dev.sample.dto.CustomerDto;
import dev.sample.dto.CustomerGradeDto;
import lombok.extern.slf4j.Slf4j;

/**
 * 고객 정보 DAO
 * - 쓰기(Master) DataSource를 사용
 * - 스프링 컨테이너가 싱글톤 빈으로 관리
 */
@Slf4j
@Repository
public class CustomerDao {

    private final DataSource ds;

    // 생성자가 1개이므로 @Autowired 생략 가능 (Spring 4.3+)
    // @Qualifier로 AppConfig의 masterDataSource 빈을 지정
    public CustomerDao(@Qualifier("masterDataSource") DataSource ds) {
        this.ds = ds;
    }

    /**
     * 고객 회원등급 변경
     * 
     * @param dto seq(고객번호), mbrRk(변경할 등급코드)
     * @return 업데이트된 행 수 (1이면 성공, 0이면 해당 고객 없음)
     */
    public int updateCustomerGrade(CustomerGradeDto dto) throws SQLException {

        String sql = "UPDATE CARD_TRANSACTION " +
                "SET MBR_RK = ? " +
                "WHERE SEQ = ?";

        try (Connection con = ds.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dto.getMbrRk());
            ps.setString(2, dto.getSeq());

            int updatedRows = ps.executeUpdate();
            log.info("updateCustomerGrade seq={}, mbrRk={}, updatedRows={}",
                    dto.getSeq(), dto.getMbrRk(), updatedRows);
            return updatedRows;
        }
    }

    /**
     * 로그인 - 아이디와 비밀번호로 고객 정보 조회
     * 
     * @param id       아이디
     * @param password 비밀번호
     * @return 조회된 고객 정보 (없으면 null)
     */
    public CustomerDto findCustomerByIdAndPassword(String id, String password) throws SQLException {
        String sql = "SELECT id, name, role FROM CUSTOMER WHERE id = ? AND password = ?";

        try (Connection con = ds.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.setString(2, password);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return CustomerDto.builder()
                            .id(rs.getString("id"))
                            .name(rs.getString("name"))
                            .role(rs.getString("role"))
                            .build();
                }
            }
        }
        return null;
    }
}
