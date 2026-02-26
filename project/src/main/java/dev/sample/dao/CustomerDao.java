package dev.sample.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import dev.sample.dto.CustomerGradeDto;
import lombok.extern.slf4j.Slf4j;

/**
 * 고객 정보 DAO
 */
@Slf4j
public class CustomerDao {

    private final DataSource ds;

    public CustomerDao(DataSource ds) {
        this.ds = ds;
    }

    /**
     * 고객 회원등급 변경
     * @param dto seq(고객번호), mbrRk(변경할 등급코드)
     * @return 업데이트된 행 수 (1이면 성공, 0이면 해당 고객 없음)
     */
    public int updateCustomerGrade(CustomerGradeDto dto) throws SQLException {

        String sql =
            "UPDATE CARD_TRANSACTION " +
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
}
