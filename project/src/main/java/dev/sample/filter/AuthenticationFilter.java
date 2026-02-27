package dev.sample.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dev.sample.util.ResponseUtil;

/**
 * 인증 필터
 * /api/stats/*, /api/customer/* 요청 시 세션(Redis)에 로그인 정보가 있는지 확인합니다.
 * 로그인하지 않은 사용자는 401 Unauthorized 응답을 받습니다.
 * /api/auth/* 경로는 필터를 거치지 않습니다 (로그인/로그아웃은 누구나 가능).
 */
@WebFilter(urlPatterns = { "/api/stats/*", "/api/customer/*" })
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 초기화 불필요
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        // 세션 확인 (없으면 새로 생성하지 않음)
        HttpSession session = httpReq.getSession(false);

        if (session != null && session.getAttribute("LOGIN_USER") != null) {
            // 로그인된 사용자 → 요청 통과
            chain.doFilter(request, response);
        } else {
            // 비로그인 사용자 → 401 Unauthorized
            ResponseUtil.sendError(httpResp, HttpServletResponse.SC_UNAUTHORIZED,
                    "로그인이 필요합니다. POST /api/auth/login 으로 먼저 로그인해주세요.");
        }
    }

    @Override
    public void destroy() {
        // 자원 해제 불필요
    }
}
