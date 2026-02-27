package dev.sample.controller.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dev.sample.dto.CustomerDto;
import dev.sample.util.ResponseUtil;

@WebServlet("/api/auth/me")
public class MeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 현재 세션 가져오기 (없으면 새로 생성하지 않음: false)
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("LOGIN_USER") != null) {
            CustomerDto loginUser = (CustomerDto) session.getAttribute("LOGIN_USER");
            ResponseUtil.sendSuccess(response, loginUser, "인증된 사용자입니다.");
        } else {
            ResponseUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
        }
    }
}
