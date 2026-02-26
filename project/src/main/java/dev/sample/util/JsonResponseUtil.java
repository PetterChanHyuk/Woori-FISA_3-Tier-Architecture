package dev.sample.util;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

/**
 * JSON 응답 공통 유틸리티
 */
public class JsonResponseUtil {

    private JsonResponseUtil() {}

    /**
     * 성공 응답 전송
     * @param resp   HttpServletResponse
     * @param json   data 부분 JSON 문자열
     */
    public static void sendSuccess(HttpServletResponse resp, String json) throws IOException {
        resp.setStatus(200);
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print("{\"status\":\"success\",\"data\":" + json + "}");
    }

    /**
     * 에러 응답 전송
     * @param resp       HttpServletResponse
     * @param statusCode HTTP 상태코드
     * @param message    에러 메시지
     */
    public static void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print("{\"status\":\"error\",\"message\":\"" + escapeJson(message) + "\"}");
    }

    /**
     * JSON 문자열 이스케이프 처리
     */
    public static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }
}
