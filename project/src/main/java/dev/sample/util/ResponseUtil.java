package dev.sample.util;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 객체를 JSON으로 직렬화하여 클라이언트에 응답하는 유틸리티 클래스
 */
public class ResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ResponseUtil() {
    }

    /**
     * 성공 응답 전송 (객체를 받아 JSON으로 변환)
     */
    public static void sendSuccess(HttpServletResponse response, Object data, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json; charset=UTF-8");

        String json = "{\"status\": \"success\", \"message\": \"" + escapeJson(message) + "\", \"data\": "
                + (data == null ? "null" : objectMapper.writeValueAsString(data)) + "}";

        response.getWriter().print(json);
    }

    /**
     * 에러 응답 전송
     */
    public static void sendError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json; charset=UTF-8");

        String json = "{\"status\": \"error\", \"message\": \"" + escapeJson(message) + "\"}";

        response.getWriter().print(json);
    }

    private static String escapeJson(String value) {
        if (value == null)
            return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
