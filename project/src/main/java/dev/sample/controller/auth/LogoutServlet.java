package dev.sample.controller.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dev.sample.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebServlet("/api/auth/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            // 세션 무효화 (Redis에서도 즉시 삭제됨)
            session.invalidate();
            log.info("사용자 로그아웃 완료");
        }
        ResponseUtil.sendSuccess(response, null, "로그아웃 되었습니다.");
    }
}
