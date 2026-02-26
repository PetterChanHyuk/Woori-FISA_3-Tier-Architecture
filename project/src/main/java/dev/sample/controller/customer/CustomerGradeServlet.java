package dev.sample.controller.customer;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import dev.sample.ApplicationContextListener;
import dev.sample.dto.CustomerGradeDto;
import dev.sample.service.CustomerService;
import dev.sample.util.JsonResponseUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 고객 회원등급 변경 API
 * PUT /api/customer/grade
 * Body: seq=123456789&mbrRk=22
 */
@WebServlet("/api/customer/grade")
@Slf4j
public class CustomerGradeServlet extends HttpServlet {

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        // PUT 요청은 getParameter()가 동작하지 않으므로 Body를 직접 파싱
        Map<String, String> params = parseBody(req);
        String seq   = params.get("seq");
        String mbrRk = params.get("mbrRk");
        log.info("PUT /api/customer/grade - seq={}, mbrRk={}", seq, mbrRk);

        try {
            DataSource ds = ApplicationContextListener.getDataSource(getServletContext());
            CustomerService service = new CustomerService(ds);

            CustomerGradeDto dto = CustomerGradeDto.builder()
                    .seq(seq)
                    .mbrRk(mbrRk)
                    .build();

            int updatedRows = service.updateCustomerGrade(dto);

            if (updatedRows == 0) {
                JsonResponseUtil.sendError(resp, 404, "해당 고객번호를 찾을 수 없습니다: " + seq);
            } else {
                JsonResponseUtil.sendSuccess(resp,
                    "{\"updatedRows\":" + updatedRows + ",\"seq\":\"" + seq + "\",\"mbrRk\":\"" + mbrRk + "\"}");
            }

        } catch (IllegalArgumentException e) {
            log.warn("잘못된 요청 - {}", e.getMessage());
            JsonResponseUtil.sendError(resp, 400, e.getMessage());
        } catch (SQLException e) {
            log.error("DB 오류", e);
            JsonResponseUtil.sendError(resp, 500, "DB 오류가 발생했습니다.");
        }
    }

    /**
     * PUT 요청 Body 파싱 (key=value&key=value 형식)
     */
    private Map<String, String> parseBody(HttpServletRequest req) throws IOException {
        Map<String, String> params = new HashMap<>();
        StringBuilder body = new StringBuilder();

        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }

        if (body.length() > 0) {
            for (String pair : body.toString().split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    params.put(
                        URLDecoder.decode(kv[0], "UTF-8"),
                        URLDecoder.decode(kv[1], "UTF-8")
                    );
                }
            }
        }

        return params;
    }
}
