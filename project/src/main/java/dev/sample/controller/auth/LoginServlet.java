package dev.sample.controller.auth;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.sample.ApplicationContextListener;
import dev.sample.dto.CustomerDto;
import dev.sample.dto.LoginRequestDto;
import dev.sample.service.CustomerService;
import dev.sample.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebServlet("/api/auth/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CustomerService customerService;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // ★ 핵심 변경: new CustomerService(ds) 삭제!
        // 스프링 컨테이너에서 이미 조립이 끝난 CustomerService 빈을 꺼내옵니다. (Service Locator 패턴)
        customerService = ApplicationContextListener
                .getSpringContext(config.getServletContext())
                .getBean(CustomerService.class);

        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            LoginRequestDto loginReq = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);

            CustomerDto customer = customerService.login(loginReq.getId(), loginReq.getPassword());

            if (customer != null) {
                // 로그인 성공 -> 세션 생성 (이 정보가 Redis로 들어감!)
                HttpSession session = request.getSession(true);
                session.setAttribute("LOGIN_USER", customer);

                ResponseUtil.sendSuccess(response, customer, "로그인 성공");
            } else {
                ResponseUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다.");
            }
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            log.error("로그인 중 DB 에러 발생", e);
            ResponseUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "데이터베이스 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("로그인 중 서버 에러 발생", e);
            ResponseUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
        }
    }
}
